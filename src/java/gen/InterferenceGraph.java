package gen;

import gen.asm.AssemblyItem;
import gen.asm.Register;

import java.util.*;

public class InterferenceGraph {
    public Map<Register, Node> regNodeMap = new HashMap<>();
    public List<Node> nodes = new LinkedList<>();
    public final static int K = Register.Arch.allocableArchs.length;
    public List<Node> spillNodes = new LinkedList<>();

    public InterferenceGraph(CFGNode entry) {
        new BaseCFGNodeVisitor<Void>() {
            @Override
            public Void job(CFGNode node) {
                if (!node.isEntryNode()) {
                    addToGraph(node.getLiveIn().toArray(new Register[0]));
                    addToGraph(node.getLiveOut().toArray(new Register[0]));
                }
                node.getSuccs().forEach(this::visitNode);
                return null;
            }
        }.visitNode(entry);
        coloring();
    }

    public void coloring() {
        Stack<Node> stack = new Stack<>();
        List<Node> nodesCopy = new LinkedList<>(nodes);
        while (!nodesCopy.isEmpty()) {
            // get the node that has less than k degree
            boolean found = false;
            for (int i = 0; i < nodesCopy.size(); i++) {
                if (nodesCopy.get(i).getDegree(stack, spillNodes) < K) {
                    stack.push(nodesCopy.get(i));
                    nodesCopy.remove(i);
                    found = true;
                    break;
                }
            }

            // spill
            if (!found) {
                int degree = -1;
                int idx = -1;
                for (int i = 0; i < nodesCopy.size(); i++) {
                    if (nodesCopy.get(i).getDegree(stack, spillNodes) > degree) {
                        idx = i;
                        degree = nodesCopy.get(i).getDegree(stack, spillNodes);
                    }
                }

                spillNodes.add(nodesCopy.get(idx));
                nodesCopy.remove(idx);
//                throw new RuntimeException("need to spill");
            }
        }

        while (!stack.empty()) {
            Node node = stack.pop();
            node.setAllocReg();
        }

        spillNodes.forEach(node -> node.setLabel(new AssemblyItem.Label(node.vreg.toString())));
    }

    public void addToGraph(Register[] regs) {
        for (int i = 0; i < regs.length; i++) {
            for (int j = i + 1; j < regs.length; j++) {
                // only add the virtual reg to the graph
                if (regs[i].isVirtual() && regs[j].isVirtual()) {
                    populate(regs[i]);
                    populate(regs[j]);
                    regNodeMap.get(regs[i]).biConnectTo(regNodeMap.get(regs[j]));
                } else if (regs[i].isVirtual()) {
                    populate(regs[i]);
                } else if (regs[j].isVirtual()) {
                    populate(regs[j]);
                }
            }
        }
    }

    public void populate(Register reg) {
        if (!regNodeMap.containsKey(reg)) {
            Node node = new Node(reg);
            regNodeMap.put(reg, node);
            nodes.add(node);
        }
    }

    public Register seekRegister(Register vReg) {
        if (!vReg.isVirtual()) return vReg;
        if (!containsRegister(vReg)) return null;
        if (isSpill(vReg)) return null;
        return regNodeMap.get(vReg).getAllocReg();
    }

    public Boolean containsRegister(Register vReg) {
        return regNodeMap.containsKey(vReg);
    }

    public boolean isSpill(Register vReg) {
        if (containsRegister(vReg)) return spillNodes.contains(regNodeMap.get(vReg));
        return false;
    }

    public AssemblyItem.Label seekLabel(Register vReg) {
        if (!containsRegister(vReg)) return null;
        if (!isSpill(vReg)) return null;
        return regNodeMap.get(vReg).getLabel();
    }

    public boolean isEmittbale(List<Register> regs) {
        boolean result = true;
        for (Register reg : regs) {
            if (!containsRegister(reg) && reg.isVirtual()) {
                result = false;
            }
        }
        return result;
    }

    public class Node {
        private Register vreg;
        private Set<Node> connects = new HashSet<>();
        private boolean isEntry;
        private Register allocReg = null;
        private AssemblyItem.Label label;

        public Node(Register reg) {
            this.vreg = reg;
        }

        public Node(boolean isEntry) {
            this.isEntry = isEntry;
        }

        public void biConnectTo(Node node) {
            connectTo(node);
            node.connectTo(this);
        }

        public void connectTo(Node node) {
            this.connects.add(node);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(vreg, node.vreg);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vreg);
        }

        public int getDegree(Stack<Node> nodes, List<Node> spillNodes) {
            return (int) this.connects.stream().filter(connect -> (!nodes.contains(connect)) && (!spillNodes.contains(connect))).count();
        }

        public Register getVreg() {
            return vreg;
        }

        public void setVreg(Register vreg) {
            this.vreg = vreg;
        }


        public Register getAllocReg() {
            return allocReg;
        }

        public void setAllocReg(Register reg) {
            this.allocReg = reg;
        }

        public void setAllocReg() {
            for (Register arch : Register.Arch.allocableArchs) {
                if (!adjacentTo(arch)) {
                    setAllocReg(arch);
                    break;
                }
            }
        }

        public boolean adjacentTo(Register reg) {
            boolean result = false;
            for (Node conn : this.connects) {
                if (conn.getAllocReg() != null && conn.getAllocReg().equals(reg)) {
                    result = true;
                    break;
                }
            }
            return result;
        }

        public AssemblyItem.Label getLabel() {
            return label;
        }

        public void setLabel(AssemblyItem.Label label) {
            this.label = label;
        }
    }

    abstract private class BaseInfGraphVisitor<T> {
        public Set<CFGNode> visited = new HashSet();

        abstract T job(Node node);

        private T visitNode(Node node) {
            if (!visited.contains(node)) {
                return job(node);
            } else {
                return null;
            }
        }
    }

}
