package gen.asm;


import ast.*;

import java.util.*;

public abstract class AssemblyItem {
    public abstract void accept(AssemblyItemVisitor v);

    public static class Comment extends AssemblyItem {
        String comment;

        Comment(String comment) {
            this.comment = comment;
        }

        public String toString() {
            return comment;
        }

        public void accept(AssemblyItemVisitor v) {
            v.visitComment(this);
        }
    }

    public static class Directive extends AssemblyItem {

        // TODO: replace this with global space allocation
        protected final String name;

        private Directive(String name) {
            this.name = name;
        }

        public String toString() {
            return "." + name;
        }

        public void accept(AssemblyItemVisitor v) {
            v.visitDirective(this);
        }

        static public class StaticAllocation extends Directive {
            private final Space space;

            public StaticAllocation(VarDecl vd) {
                super(vd.label.toString());
                space = new Space(vd.size());
            }

            public String toString() {
                return this.name + ":   " + this.space.toString();
            }
        }

        static public class StringAllocation extends Directive {
            private final String s;

            public StringAllocation(StrLiteral sl) {
                super(sl.getLabel().toString());
                this.s = sl.s;
            }

            @Override
            public String toString() {
                String padded = s;
                padded += "\\0";
                int length = getStringLength(padded);
                while (length % 4 != 0) {
                    padded += "\\0"; // add \0 for padding
                    length += 1;
                }
                return this.name + ":   .ascii    \"" + padded + "\"";
            }

            // "\\n" etc. in Java String need to be count as "\n" in ascii
            public int getStringLength(String s) {
                int count = 0;
                int ln = s.length();
                for (int i = 0; i < s.length(); i++) {
                    if (i + 1 < s.length()) {
                        if (s.charAt(i) == '\\' && (s.charAt(i + 1) == 'n' || s.charAt(i + 1) == '0')) {
                            count -= 1;
                        }
                    }
                    count += 1;
                }
                return count;
            }
        }

        static public class Space extends Directive {
            private final int size;

            public Space(int size) {
                super("space");
                this.size = size;
            }

            public String toString() {
                return super.toString() + " " + size;
            }
        }
    }

    public abstract static class Instruction extends AssemblyItem {

        public final String opcode;

        public Instruction(String opcode) {
            this.opcode = opcode;
        }

        /**
         * This "fake" instruction should push all the registers used inside a function onto the stack.
         */
        public static final Instruction pushRegisters = new Instruction("pushReg") {
            @Override
            public Register def() {
                return null;
            }

            @Override
            public List<Register> uses() {
                return new LinkedList<>();
            }

            @Override
            public gen.asm.AssemblyItem.Instruction rebuild(Map<Register, Register> regMap) {
                return this;
            }

            @Override
            public String toString() {
                return opcode;
            }
        };

        /**
         * This "fake" instruction should pop all the registers used inside a function from the stack.
         */
        public static final Instruction popRegisters = new Instruction("popReg") {
            @Override
            public Register def() {
                return null;
            }

            @Override
            public List<Register> uses() {
                return new LinkedList<>();
            }

            @Override
            public gen.asm.AssemblyItem.Instruction rebuild(Map<Register, Register> regMap) {
                return this;
            }

            @Override
            public String toString() {
                return opcode;
            }
        };


        /**
         * @return register that this instructions modifies (if none, returns null)
         */
        public abstract Register def();

        /**
         * @return list of registers that this instruction uses
         */
        public abstract List<Register> uses();

        /**
         * @return list of registers that are used as operands for this instruction
         */
        public List<Register> registers() {
            List<Register> regs = new ArrayList<>(uses());
            if (def() != null)
                regs.add(def());
            return regs;
        }

        /**
         * @param regMap replacement map for register
         * @return a new instruction where the registers have been replaced based on the regMap
         */
        public abstract Instruction rebuild(Map<Register, Register> regMap);

        public void accept(AssemblyItemVisitor v) {
            v.visitInstruction(this);
        }


        public static class RInstruction extends Instruction {
            public final Register dst;
            public final Register src1;
            public final Register src2;

            public RInstruction(String opcode, Register dst, Register src1, Register src2) {
                super(opcode);
                this.dst = dst;
                this.src1 = src1;
                this.src2 = src2;
            }

            public String toString() {
                return opcode + " " + dst + "," + src1 + "," + src2;
            }


            public Register def() {
                return dst;
            }


            public List<Register> uses() {
                Register[] uses = {src1, src2};
                return Arrays.asList(uses);
            }

            public RInstruction rebuild(Map<Register, Register> regMap) {
                return new RInstruction(opcode, regMap.getOrDefault(dst, dst), regMap.getOrDefault(src1, src1), regMap.getOrDefault(src2, src2));
            }

        }


        public static class Branch extends Instruction {
            public final Label label;
            public final Register src1;
            public final Register src2;

            public Branch(String opcode, Register src1, Register src2, Label label) {
                super(opcode);
                this.label = label;
                this.src1 = src1;
                this.src2 = src2;
            }

            public String toString() {
                return opcode + " " + src1 + "," + src2 + "," + label;
            }


            public Register def() {
                return null;
            }


            public List<Register> uses() {
                Register[] uses = {src1, src2};
                return Arrays.asList(uses);
            }

            public Branch rebuild(Map<Register, Register> regMap) {
                return new Branch(opcode, regMap.getOrDefault(src1, src1), regMap.getOrDefault(src2, src2), label);
            }
        }


        public static class IInstruction extends Instruction {
            public final int imm;
            public final Register dst;
            public final Register src;

            public IInstruction(String opcode, Register dst, Register src, int imm) {
                super(opcode);
                this.imm = imm;
                this.src = src;
                this.dst = dst;
            }

            public String toString() {
                return opcode + " " + dst + "," + src + "," + imm;
            }


            public Register def() {
                return dst;
            }


            public List<Register> uses() {
                Register[] uses = {src};
                return Arrays.asList(uses);
            }

            public IInstruction rebuild(Map<Register, Register> regMap) {
                return new IInstruction(opcode, regMap.getOrDefault(dst, dst), regMap.getOrDefault(src, src), imm);
            }
        }


        public abstract static class MemIndirect extends Instruction {
            public final Register op1;
            public final Register op2;
            public final int imm;

            public MemIndirect(String opcode, Register op1, Register op2, int imm) {
                super(opcode);
                this.op1 = op1;
                this.op2 = op2;
                this.imm = imm;
            }

            public String toString() {
                return opcode + " " + op1 + "," + imm + "(" + op2 + ")";
            }
        }

        public static class Store extends MemIndirect {
            public Store(String opcode, Register op1, Register op2, int imm) {
                super(opcode, op1, op2, imm);
            }

            public Store rebuild(Map<Register, Register> regMap) {
                return new Store(opcode, regMap.getOrDefault(op1, op1), regMap.getOrDefault(op2, op2), imm);
            }

            public Register def() {
                return null;
            }

            public List<Register> uses() {
                Register[] uses = {op1, op2};
                return Arrays.asList(uses);
            }
        }

        public static class Load extends MemIndirect {
            public Load(String opcode, Register op1, Register op2, int imm) {
                super(opcode, op1, op2, imm);
            }

            public Load rebuild(Map<Register, Register> regMap) {
                return new Load(opcode, regMap.getOrDefault(op1, op1), regMap.getOrDefault(op2, op2), imm);
            }

            public Register def() {
                return op1;
            }

            public List<Register> uses() {
                Register[] uses = {op2};
                return Arrays.asList(uses);
            }
        }


        public static class LA extends Instruction {
            public final Label label;
            public final Register dst;

            public LA(Register dst, Label label) {
                super("la");
                this.label = label;
                this.dst = dst;
            }

            public String toString() {
                return "la " + dst + "," + label;
            }

            public Register def() {
                return dst;
            }

            public List<Register> uses() {
                Register[] uses = {};
                return Arrays.asList(uses);
            }

            public LA rebuild(Map<Register, Register> regMap) {
                return new LA(regMap.getOrDefault(dst, dst), label);
            }
        }

        public static class LI extends Instruction {
            public final int imm;
            public final Register dst;

            public LI(Register dst, int imm) {
                super("li");
                this.imm = imm;
                this.dst = dst;
            }

            public String toString() {
                return "li " + dst + "," + imm;
            }

            public Register def() {
                return dst;
            }

            public List<Register> uses() {
                Register[] uses = {};
                return Arrays.asList(uses);
            }

            public LI rebuild(Map<Register, Register> regMap) {
                return new LI(regMap.getOrDefault(dst, dst), imm);
            }
        }

        public static class LW extends Instruction {
            public final Register dst;
            public final Label label;

            public LW(Register dst, Label label) {
                super("lw");
                this.label = label;
                this.dst = dst;
            }

            public String toString() {
                return "lw " + dst + "," + label;
            }

            public Register def() {
                return dst;
            }

            public List<Register> uses() {
                Register[] uses = {};
                return Arrays.asList(uses);
            }

            public LW rebuild(Map<Register, Register> regMap) {
                return new LW(regMap.getOrDefault(dst, dst), label);
            }
        }

        public static class LabelLoad extends Instruction {
            public final Register dst;
            public final Label label;

            public LabelLoad(String opcode, Register dst, Label label) {
                super(opcode);
                this.dst = dst;
                this.label = label;
            }

            public String toString() {
                return opcode + " " + dst + "," + label;
            }

            public Register def() {
                return dst;
            }

            public List<Register> uses() {
                Register[] uses = {};
                return Arrays.asList(uses);
            }

            public LabelLoad rebuild(Map<Register, Register> regMap) {
                return new LabelLoad(opcode, regMap.getOrDefault(dst, dst), label);
            }
        }

        public static class LabelStore extends Instruction {
            public final Register src;
            public final Label label;

            public LabelStore(String opcode, Register src, Label label) {
                super(opcode);
                this.src = src;
                this.label = label;
            }

            public String toString() {
                return opcode + " " + src + "," + label;
            }

            public Register def() {
                return null;
            }

            public List<Register> uses() {
                Register[] uses = {src};
                return Arrays.asList(uses);
            }

            public LabelStore rebuild(Map<Register, Register> regMap) {
                return new LabelStore(opcode, regMap.getOrDefault(src, src), label);
            }
        }

        public static class MOVE extends Instruction {
            public final Register src;
            public final Register dst;

            public MOVE(Register dst, Register src) {
                super("move");
                this.dst = dst;
                this.src = src;
            }

            public String toString() {
                return opcode + " " + dst + "," + src;
            }

            public Register def() {
                return dst;
            }

            public List<Register> uses() {
                Register[] uses = {src};
                return Arrays.asList(uses);
            }

            public MOVE rebuild(Map<Register, Register> regMap) {
                return new MOVE(regMap.getOrDefault(dst, dst), regMap.getOrDefault(src, src));
            }
        }

        public static class JR extends Instruction {
            private final Register src = Register.Arch.ra;

            public JR() {
                super("jr");
            }

            public String toString() {
                return opcode + " " + src;
            }

            public Register def() {
                return null;
            }

            public List<Register> uses() {
                Register[] uses = {src};
                return Arrays.asList(uses);
            }

            public JR rebuild(Map<Register, Register> regMap) {
                return new JR();
            }
        }

        public static class Syscall extends Instruction {
            public Syscall() {
                super("syscall");
            }

            public String toString() {
                return opcode;
            }

            public Register def() {
                return null;
            }

            public List<Register> uses() {
                Register[] uses = {};
                return Arrays.asList(uses);
            }

            public Syscall rebuild(Map<Register, Register> regMap) {
                return new Syscall();
            }
        }

        public static class SingleBranchInstruction extends Instruction {
            public final Label label;

            public SingleBranchInstruction(String opcode, Label label) {
                super(opcode);
                this.label = label;
            }

            public String toString() {
                return opcode + " " + label;
            }

            public Register def() {
                return null;
            }

            public List<Register> uses() {
                Register[] uses = {};
                return Arrays.asList(uses);
            }

            public SingleBranchInstruction rebuild(Map<Register, Register> regMap) {
                return this;
            }
        }
        // TODO: to complete
    }

    public static class Label extends AssemblyItem {
        private static int cnt = 0;
        private final int id = cnt++;
        private final String name;

        public Label() {
            this.name = "";
        }

        public Label(String name) {
            this.name = name;
        }

        public String toString() {
            return "label_" + id + "_" + name;
        }

        public void accept(AssemblyItemVisitor v) {
            v.visitLabel(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Label label = (Label) o;
            return id == label.id && Objects.equals(name, label.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name);
        }
    }

    public static class MainLabel extends Label {
        public String toString() {
            return "main";
        }

        public void accept(AssemblyItemVisitor v) {
            v.visitLabel(this);
        }
    }
}
