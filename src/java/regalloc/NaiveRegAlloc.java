package regalloc;

import gen.asm.Register;
import gen.asm.*;

import java.util.*;

/**
 * A very naive register allocator which allocates each virtual registers in the data section with a label.
 * The allocator assumes that each function has a single corresponding text section.
 */
public class NaiveRegAlloc {

    // map from virtual register to corresponding uniquely created label
    private static Map<Register.Virtual, AssemblyItem.Label>  collectVirtualRegisters(AssemblyProgram.Section section) {
        final Map<Register.Virtual, AssemblyItem.Label> vrMap = new HashMap<Register.Virtual, AssemblyItem.Label>();

        section.items.forEach(item ->
                item.accept(new AssemblyItemVisitor<Void>() {
                    public Void visitComment(AssemblyItem.Comment comment) {return null;}
                    public Void visitLabel(AssemblyItem.Label label) {return null;}
                    public Void visitDirective(AssemblyItem.Directive directive) {return null;}

                    public Void visitInstruction(AssemblyItem.Instruction insn) {
                        insn.registers().forEach(reg -> {
                            if (reg instanceof Register.Virtual) {
                                Register.Virtual vr = (Register.Virtual) reg;
                                AssemblyItem.Label l = new AssemblyItem.Label(vr.toString());
                                vrMap.put(vr, l);
                            }
                        });
                        return null;
                    }
                }));
        return vrMap;
    }

    private static void emitInstructionWithoutVirtualRegister(AssemblyItem.Instruction insn, Map<Register.Virtual, AssemblyItem.Label> vrMap, AssemblyProgram.Section section) {

        section.emit("Original instruction: "+insn);

        final Map<Register, Register> vrToAr = new HashMap<Register, Register>();
        Register[] tempRegs = {Register.Arch.t0, Register.Arch.t1, Register.Arch.t2, Register.Arch.t3, Register.Arch.t4, Register.Arch.t5}; // 6 temporaries should be more than enough
        final Stack<Register> freeTempRegs = new Stack<Register>();
        freeTempRegs.addAll(Arrays.asList(tempRegs));

        // creates a map from virtual register to temporary architecture register for all registers appearing in the instructions
        insn.registers().forEach(reg -> {
            if (reg.isVirtual()) {
                Register tmp = freeTempRegs.pop();
                AssemblyItem.Label label = vrMap.get(reg);
                vrToAr.put(reg, tmp);
            }
        });

        // load the values of any virtual registers used by the instruction from memory into a temporary architectural register
        insn.uses().forEach(reg -> {
            if (reg.isVirtual()) {
                Register tmp = vrToAr.get(reg);
                AssemblyItem.Label label = vrMap.get(reg);
                section.emitLA(tmp, label);
                section.emitLoad("lw", tmp, tmp, 0);
            }
        });

        // emit new instructions where all virtual register have been replaced by architectural ones
        section.emit(insn.rebuild(vrToAr));

        if (insn.def() != null) {
            if (insn.def().isVirtual()) {
                Register tmpVal = vrToAr.get(insn.def());
                Register tmpAddr = freeTempRegs.remove(0);
                AssemblyItem.Label label = vrMap.get(insn.def());

                section.emitLA(tmpAddr, label);
                section.emitStore("sw", tmpVal, tmpAddr, 0);
            }
        }
    }

    public static AssemblyProgram run(AssemblyProgram prog) {

        AssemblyProgram newProg = new AssemblyProgram();

        // we assume that each function has a single corresponding text section
        prog.sections.forEach(section -> {
            if (section.type == AssemblyProgram.Section.Type.DATA)
                newProg.emitSection(section);
            else {
                assert (section.type == AssemblyProgram.Section.Type.TEXT);

                // map from virtual register to corresponding uniquely created label
                final Map<Register.Virtual, AssemblyItem.Label> vrMap = collectVirtualRegisters(section);

                // allocate one label for each virtual register in a new data section
                AssemblyProgram.Section dataSec = newProg.newSection(AssemblyProgram.Section.Type.DATA);
                dataSec.emit("Allocated labels for virtual registers");
                vrMap.forEach((vr, lbl) -> {
                    dataSec.emit(lbl);
                    dataSec.emit(new AssemblyItem.Directive.Space(4));
                });

                // emit new instructions that don't use any virtual registers and transform push/pop registers instructions into real sequence of instructions
                // When dealign with push/pop registers, we assume that if a virtual register is used in the section, then it must be written into.
                final AssemblyProgram.Section newSection = newProg.newSection(AssemblyProgram.Section.Type.TEXT);
                List<AssemblyItem.Label> vrLabels = new LinkedList<>(vrMap.values());
                List<AssemblyItem.Label> reverseVrLabels = new LinkedList<>(vrLabels);
                Collections.reverse(reverseVrLabels);

                section.items.forEach(item ->
                        item.accept(new AssemblyItemVisitor<Void>() {
                            public Void visitComment(AssemblyItem.Comment comment) {
                                newSection.emit(comment);
                                return null;
                            }
                            public Void visitLabel(AssemblyItem.Label label) {
                                newSection.emit(label);
                                return null;
                            }
                            public Void visitDirective(AssemblyItem.Directive directive) {
                                newSection.emit(directive);
                                return null;
                            }
                            public Void visitInstruction(AssemblyItem.Instruction insn) {

                                if (insn == AssemblyItem.Instruction.pushRegisters) {
                                    newSection.emit("Original instruction: pushRegisters");
                                    for (AssemblyItem.Label l : vrLabels) {
                                        // load content of memory at label into $t0
                                        newSection.emitLA(Register.Arch.t0, l);
                                        newSection.emitLoad("lw", Register.Arch.t0, Register.Arch.t0, 0);

                                        // push $t0 onto stack
                                        newSection.emit("addi", Register.Arch.sp, Register.Arch.sp, -4);
                                        newSection.emitStore("sw", Register.Arch.t0, Register.Arch.sp, 0);
                                    }
                                } else if (insn == AssemblyItem.Instruction.popRegisters) {
                                    newSection.emit("Original instruction: popRegisters");
                                    for (AssemblyItem.Label l : reverseVrLabels) {
                                        // pop from stack into $t0
                                        newSection.emitLoad("lw", Register.Arch.t0, Register.Arch.sp, 0);
                                        newSection.emit("addi", Register.Arch.sp, Register.Arch.sp, 4);

                                        // store content of $t0 in memory at label
                                        newSection.emitLA(Register.Arch.t1, l);
                                        newSection.emitStore("sw", Register.Arch.t0, Register.Arch.t1, 0);
                                    }
                                } else
                                    emitInstructionWithoutVirtualRegister(insn, vrMap, newSection);
                                return null;
                            }
                        }));


            }
        });


        return newProg;
    }

}
