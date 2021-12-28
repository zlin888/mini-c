package regalloc;

import gen.InterferenceGraph;
import gen.asm.AssemblyItem;
import gen.asm.AssemblyItemVisitor;
import gen.asm.AssemblyProgram;
import gen.asm.Register;

import java.util.*;

public class SmartRegAlloc {

    // map from virtual register to corresponding uniquely created label
    private static Map<Register.Virtual, AssemblyItem.Label> collectVirtualRegisters(AssemblyProgram.Section section, InterferenceGraph infGraph) {
        final Map<Register.Virtual, AssemblyItem.Label> vrMap = new HashMap<Register.Virtual, AssemblyItem.Label>();

        section.items.forEach(item ->
                item.accept(new AssemblyItemVisitor<Void>() {
                    public Void visitComment(AssemblyItem.Comment comment) {
                        return null;
                    }

                    public Void visitLabel(AssemblyItem.Label label) {
                        return null;
                    }

                    public Void visitDirective(AssemblyItem.Directive directive) {
                        return null;
                    }

                    public Void visitInstruction(AssemblyItem.Instruction insn) {
                        insn.registers().forEach(reg -> {
                            if (infGraph.isSpill(reg)) {
                                AssemblyItem.Label label = infGraph.seekLabel(reg);
                                vrMap.put((Register.Virtual) reg, label);
                            }
                        });
                        return null;
                    }
                }));
        return vrMap;
    }

    public static AssemblyProgram run(AssemblyProgram prog, List<InterferenceGraph> infGraphs) {

        AssemblyProgram newProg = new AssemblyProgram();
        Queue<InterferenceGraph> infGraphsQueue = new ArrayDeque<>(infGraphs);

        // we assume that each function has a single corresponding text section
        for (AssemblyProgram.Section section : prog.sections) {
            if (section.type == AssemblyProgram.Section.Type.DATA)
                newProg.emitSection(section);
            else {
                assert (section.type == AssemblyProgram.Section.Type.TEXT);
                if (infGraphs.isEmpty()) {
                    throw new NullPointerException("infGraphs is empty");
                }
                InterferenceGraph infGraph = infGraphsQueue.poll();

                Map<Register.Virtual, AssemblyItem.Label> vrMap = new HashMap<>();

                // handle SPILL registers
                final AssemblyProgram.Section data = newProg.newSection(AssemblyProgram.Section.Type.DATA);
                data.emit("Allocated labels for spill registers");
                for (InterferenceGraph.Node node : infGraph.spillNodes) {
                    data.emit(node.getLabel());
                    data.emit(new AssemblyItem.Directive.Space(4));
                    vrMap.put((Register.Virtual) node.getVreg(), node.getLabel());
                }

                List<AssemblyItem.Label> vrLabels = new LinkedList<>(vrMap.values());
                List<AssemblyItem.Label> reverseVrLabels = new LinkedList<>(vrLabels);
                Collections.reverse(reverseVrLabels);

                // emit new instructions that don't use any virtual registers and transform push/pop registers instructions into real sequence of instructions
                // When dealing with push/pop registers, we assume that if a virtual register is used in the section, then it must be written into.
                final AssemblyProgram.Section text = newProg.newSection(AssemblyProgram.Section.Type.TEXT);

                for (AssemblyItem item : section.items) {
                    item.accept(new AssemblyItemVisitor<Void>() {
                        public Void visitComment(AssemblyItem.Comment comment) {
                            text.emit(comment);
                            return null;
                        }

                        public Void visitLabel(AssemblyItem.Label label) {
                            text.emit(label);
                            return null;
                        }

                        public Void visitDirective(AssemblyItem.Directive directive) {
                            text.emit(directive);
                            return null;
                        }

                        public Void visitInstruction(AssemblyItem.Instruction insn) {
                            if (insn == AssemblyItem.Instruction.pushRegisters) {
                                text.emit("Original instruction: pushRegisters");
                                // push all allocArch
                                for (Register allocableArch : Register.Arch.allocableArchs) {
                                    // push it onto stack
                                    text.emit("addi", Register.Arch.sp, Register.Arch.sp, -4);
                                    text.emitStore("sw", allocableArch, Register.Arch.sp, 0);
                                }

                                for (AssemblyItem.Label l : vrLabels) {
                                    // load content of memory at label into $t0
                                    text.emitLA(Register.Arch.spillArchs[0], l);
                                    text.emitLoad("lw", Register.Arch.spillArchs[0], Register.Arch.spillArchs[0], 0);

                                    // push $t0 onto stack
                                    text.emit("addi", Register.Arch.sp, Register.Arch.sp, -4);
                                    text.emitStore("sw", Register.Arch.spillArchs[0], Register.Arch.sp, 0);
                                }
                            } else if (insn == AssemblyItem.Instruction.popRegisters) {
                                text.emit("Original instruction: popRegisters");
                                for (AssemblyItem.Label l : reverseVrLabels) {
                                    // pop from stack into $t0
                                    text.emitLoad("lw", Register.Arch.spillArchs[0], Register.Arch.sp, 0);
                                    text.emit("addi", Register.Arch.sp, Register.Arch.sp, 4);

                                    // store content of $t0 in memory at label
                                    text.emitLA(Register.Arch.spillArchs[0], l);
                                    text.emitStore("sw", Register.Arch.spillArchs[0], Register.Arch.spillArchs[0], 0);
                                }

                                // pop from stack into arch register
                                for (int i = Register.Arch.allocableArchs.length - 1; i >= 0; i--) {
                                    text.emit("addi", Register.Arch.sp, Register.Arch.sp, 4);
                                    text.emitLoad("lw",
                                            Register.Arch.allocableArchs[i],
                                            Register.Arch.sp, 0);
                                }
                            } else {
                                Map<Register, Register> regMap = new HashMap<>();
                                // if infGraph don't have a register, it means the the register only def but never used
                                ArrayDeque<Register> spillArchs = new ArrayDeque<>(Arrays.asList(Register.Arch.spillArchs));
                                if (infGraph.isEmittbale(insn.registers())) {
                                    for (Register reg : insn.uses()) {
                                        if (infGraph.isSpill(reg)) {
                                            Register spillArch = spillArchs.poll();
                                            AssemblyItem.Label lable = infGraph.seekLabel(reg);
                                            text.emitLoad("lw", spillArch, lable);
                                            regMap.put(reg, spillArch);
                                        } else {
                                            regMap.put(reg, infGraph.seekRegister(reg));
                                        }
                                    }

                                    Register defReg = insn.def();
                                    Register archForDef = null;
                                    AssemblyItem.Label labelForDef = null;
                                    if (defReg != null) {
                                        if (infGraph.isSpill(defReg)) {
                                            archForDef = spillArchs.poll();
                                            labelForDef = infGraph.seekLabel(defReg);
                                            text.emitLoad("lw", archForDef, labelForDef);
                                            regMap.put(defReg, archForDef);
                                        } else {
                                            regMap.put(defReg, infGraph.seekRegister(defReg));
                                        }
                                    }

                                    text.emit(insn.rebuild(regMap));

                                    if (defReg != null && archForDef != null && labelForDef != null) {
                                        text.emitStore("sw", archForDef, labelForDef);
                                    }
                                }
                            }
                            return null;
                        }
                    });
                }
            }
        }
        return newProg;
    }
}
