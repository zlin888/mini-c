package gen.asm;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AssemblyProgram {

    public static class Section {

        public enum Type {TEXT, DATA}
        public final Type type;

        public Section(Type type) {
            this.type = type;
        }

        public final List<AssemblyItem> items = new ArrayList<AssemblyItem>();


        public void emit(AssemblyItem.Instruction instruction) {
            assert this.type == Type.TEXT;
            items.add(instruction);
        }

        public void emit(String opcode, Register dst, Register src1, Register src2) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.RInstruction(opcode, dst, src1, src2));
        }

        public void emit(String opcode, Register src1, Register src2, AssemblyItem.Label label) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.Branch(opcode, src1, src2, label));
        }

        public void emit(String opcode, Register dst, Register src, int imm) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.IInstruction(opcode, dst, src, imm));
        }

        public void emitLA(Register dst, AssemblyItem.Label label) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.LA(dst, label));
        }

        public void emitLI(Register dst, int imm) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.LI(dst, imm));
        }

        public void emitLW(Register dst, AssemblyItem.Label label) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.LW(dst, label));
        }


        public void emitMOVE(Register dst, Register src) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.MOVE(dst, src));
        }

        public void emitJR() {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.JR());
        }

        public void emit(String opcode, AssemblyItem.Label label) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.SingleBranchInstruction(opcode, label));
        }

        public void emitLoad(String opcode, Register dst, AssemblyItem.Label label) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.LabelLoad(opcode, dst, label));
        }

        public void emitLoad(String opcode, Register val, Register addr, int imm) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.Load(opcode, val, addr, imm));
        }

        public void emitStore(String opcode, Register src, AssemblyItem.Label label) {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.LabelStore(opcode, src, label));
        }

        public void emitStore(String opcode, Register val, Register addr, int imm) {
            assert this.type == Type.TEXT;
            if (val == null) {
                throw new NullPointerException();
            }
            items.add(new AssemblyItem.Instruction.Store(opcode, val, addr, imm));
        }

        public void emitSyscall() {
            assert this.type == Type.TEXT;
            items.add(new AssemblyItem.Instruction.Syscall());
        }


        public void emit(AssemblyItem.Label label){
            items.add(label);
        }

        public void emit(AssemblyItem.Comment comment) {
            items.add(comment);
        }

        public void emit(String comment) {
            items.add(new AssemblyItem.Comment(comment));
        }

        public void emit(AssemblyItem.Directive directive) {
            items.add(directive);
        }

        public void print(final PrintWriter writer) {
            switch(type) {
                case DATA : writer.println(".data"); break;
                case TEXT : writer.println(".text"); break;
            }
            items.forEach(item ->
                    item.accept(new AssemblyItemVisitor<Void>() {

                        public Void visitComment(AssemblyItem.Comment comment) {
                            writer.println("# "+comment);
                            return null;
                        }
                        public Void visitLabel(AssemblyItem.Label label) {
                            writer.println(label + ":");
                            return null;
                        }

                        public Void visitDirective(AssemblyItem.Directive directive) {
                            writer.println(directive);
                            return null;
                        }

                        public Void visitInstruction(AssemblyItem.Instruction instruction) {
                            writer.println(instruction);
                            return null;
                        }
                    })
            );
        }
    }


    private Section currSection;
    private Section dataSection;
    public final List<Section> sections = new LinkedList<Section>();

    public AssemblyProgram() {
        this.dataSection = new Section(Section.Type.DATA);
        sections.add(dataSection);
        this.currSection = dataSection;
    }

    public Section getCurrSection() {
        return currSection;
    }
    public Section getDataSection() {return dataSection; }

    public void emitSection(Section section) {
        currSection = section;
        sections.add(currSection);
    }

    public Section newSection(Section.Type type) {
        currSection = new Section(type);
        sections.add(currSection);
        return currSection;
    }

    public Section newMainSection(Section.Type type) {
        currSection = new Section(type);
        sections.add(0, currSection);
        return currSection;
    }

    public void print(final PrintWriter writer) {
        sections.forEach(section -> {
                section.print(writer);
                writer.println();
        });
        writer.close();
    }

}
