package gen.asm;

import java.util.Objects;

/**
 * @author cdubach
 */
public abstract class Register {

    abstract public boolean isVirtual();

    static public class Virtual extends Register {
        private static int cnt = 0;
        private final int id;

        public Virtual() {
            this.id = cnt++;
        }

        public String toString() {
            return "v" + id;
        }

        public boolean isVirtual() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Virtual virtual = (Virtual) o;
            return id == virtual.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        public int getId() {
            return id;
        }
    }

    /**
     * Architectural registers.
     */
    static public class Arch extends Register {

        public static final Arch zero = new Arch(0, "zero");
        public static final Arch v0 = new Arch(2, "v0");
        public static final Arch v1 = new Arch(3, "v1");
        public static final Arch a0 = new Arch(4, "a0");
        public static final Arch a1 = new Arch(5, "a1");
        public static final Arch a2 = new Arch(6, "a2");
        public static final Arch a3 = new Arch(7, "a3");
        public static final Arch t0 = new Arch(8, "t0");
        public static final Arch t1 = new Arch(9, "t1");
        public static final Arch t2 = new Arch(10, "t2");
        public static final Arch t3 = new Arch(11, "t3");
        public static final Arch t4 = new Arch(12, "t4");
        public static final Arch t5 = new Arch(13, "t5");
        public static final Arch t6 = new Arch(14, "t6");
        public static final Arch t7 = new Arch(15, "t7");
        public static final Arch s0 = new Arch(16, "s0");
        public static final Arch s1 = new Arch(17, "s1");
        public static final Arch s2 = new Arch(18, "s2");
        public static final Arch s3 = new Arch(19, "s3");
        public static final Arch s4 = new Arch(20, "s4");
        public static final Arch s5 = new Arch(21, "s5");
        public static final Arch s6 = new Arch(22, "s6");
        public static final Arch s7 = new Arch(23, "s7");
        public static final Arch t8 = new Arch(24, "t8");
        public static final Arch t9 = new Arch(25, "t9");
        public static final Arch gp = new Arch(28, "gp");
        public static final Arch sp = new Arch(29, "sp");
        public static final Arch fp = new Arch(30, "fp");
        public static final Arch ra = new Arch(31, "ra");
        public static final Arch[] allocableArchs = {
                s0, s1, s2, s3, s4, s5, s6, s7, t0, t1, t2, t3, t4, t5, t6
        };
        public static final Arch[] spillArchs = {
                t7, t8, t9
        };

        private final int num;
        private final String name;

        private Arch(int num, String name) {
            this.num = num;
            this.name = name;
        }

        public String toString() {
            return "$" + name;
        }

        public boolean isVirtual() {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Arch arch = (Arch) o;
            return num == arch.num && Objects.equals(name, arch.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(num, name);
        }
    }

}
