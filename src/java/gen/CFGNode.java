package gen;

import gen.asm.AssemblyItem;
import gen.asm.Register;

import java.util.*;
import java.util.stream.Collectors;

public class CFGNode {
    private static long id = 0;
    private long myid = id++;
    private List<CFGNode> succs = new LinkedList();
    private List<CFGNode> preds = new LinkedList();
    private List<AssemblyItem.Instruction> ins;
    private List<AssemblyItem.Instruction> outs;
    private List<AssemblyItem.Label> preLabels = new LinkedList<>(); // labels in front of a instruction
    private AssemblyItem.Instruction instruction = null;
    private Set<Register> liveIn = new HashSet<>();
    private Set<Register> liveOut = new HashSet<>();
    private boolean entryNode = false;

    public CFGNode(AssemblyItem.Instruction instruction) {
        this.instruction = instruction;
    }

    public CFGNode(Boolean entryNode) {
        this.entryNode = entryNode;
    }


    public List<CFGNode> getSuccs() {
        return succs;
    }

    public List<CFGNode> getPreds() {
        return preds;
    }

    public void addPred(CFGNode pred) {
        this.preds.add(pred);
    }

    public void addSucc(CFGNode succ) {
        this.succs.add(succ);
    }

    public List<AssemblyItem.Instruction> getIns() {
        return ins;
    }

    public void setIns(List<AssemblyItem.Instruction> ins) {
        this.ins = ins;
    }

    public List<AssemblyItem.Instruction> getOuts() {
        return outs;
    }

    public void setOuts(List<AssemblyItem.Instruction> outs) {
        this.outs = outs;
    }

    public List<AssemblyItem.Label> getPreLabels() {
        return Objects.requireNonNullElseGet(preLabels, LinkedList::new);
    }

    public void setPreLabels(List<AssemblyItem.Label> preLabels) {
        this.preLabels.addAll(preLabels);
    }

    public void accept(BaseCFGNodeVisitor v) {
        v.visitNode(this);
    }

    public AssemblyItem.Instruction getInstruction() {
        return instruction;
    }

    public void setInstruction(AssemblyItem.Instruction instruction) {
        this.instruction = instruction;
    }

    @Override
    public String toString() {
        if (getInstruction() == null) {
            return "entry";
        } else {
            return myid + ": " + instruction.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CFGNode cfgNode = (CFGNode) o;
        return myid == cfgNode.myid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(myid);
    }

    public boolean isEntryNode() {
        return entryNode;
    }

    public void addLiveIn(Register reg) {
        liveIn.add(reg);
    }

    public void addLiveOut(Register reg) {
        liveOut.add(reg);
    }

    public Set<Register> getLiveInCopy() {
        return new HashSet<>(liveIn);
    }

    public Set<Register> getLiveOutCopy() {
        return new HashSet<>(liveOut);
    }

    public Set<Register> getLiveIn() {
        return liveIn;
    }

    public Set<Register> getLiveOut() {
        return liveOut;
    }

    public boolean updateLiveIn() {
        Set<Register> liveInCopy = getLiveInCopy();
        getLiveIn().addAll(this.getInstruction().uses());
        getLiveIn().addAll(this.liveOut.stream().filter(reg ->
                !reg.equals(this.getInstruction().def())).collect(Collectors.toList()));
        boolean ans = !liveInCopy.equals(getLiveIn());
        return ans;
    }

    public boolean updateLiveOut() {
        Set<Register> liveOutCopy = getLiveOutCopy();
        getSuccs().forEach(succ -> liveOut.addAll(succ.getLiveIn()));
        boolean ans = !liveOutCopy.equals(getLiveOut());
        return ans;
    }
}
