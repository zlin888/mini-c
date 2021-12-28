package gen;

import gen.asm.AssemblyItem;
import gen.asm.AssemblyProgram;
import gen.asm.Register;

import java.util.*;

public class LivenessAnalyzer {

    private AssemblyProgram asmProg;
    private LinkedList<CFGNode> CFGNodes = new LinkedList<>();
    private LinkedList<String> CFGNames = new LinkedList<>();

    public LivenessAnalyzer(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
    }

    public LinkedList<CFGNode> run() {
        // build the basic CFG
        for (AssemblyProgram.Section section : asmProg.sections) {
            if (section.type == AssemblyProgram.Section.Type.TEXT) {
                assert section.items.get(0) instanceof AssemblyItem.Label;
                CFGNames.add(section.items.get(0).toString());
                CFGNodes.add(buildCFG(section));
            }
        }

        // draw the line for branch and jump
        CFGNodes.forEach(this::handleBranchAndJump);

        //output the CFG in dot-lang
        for (int i = 0; i < CFGNodes.size(); i++) {
            new CFGDotPrinter("dot/" + CFGNames.get(i)).run(CFGNodes.get(i));
        }

        CFGNodes.forEach(this::livenessAnalyze);
        
        return CFGNodes;
    }

    private void handleBranchAndJump(CFGNode entry) {
        BaseCFGNodeVisitor<Void> BranchAndJumpVisitor = new BaseCFGNodeVisitor<Void>() {
            @Override
            public Void job(CFGNode node) {
                LinkedList<CFGNode> succs = new LinkedList<>(node.getSuccs());
                if (node.getInstruction() instanceof AssemblyItem.Instruction.Branch &&
                        ((AssemblyItem.Instruction.Branch) node.getInstruction()).opcode.equals("beq")) {
                    AssemblyItem.Instruction.Branch branch = (AssemblyItem.Instruction.Branch) node.getInstruction();
                    CFGNode found = findLabelNode(entry, branch.label);
                    if (found != null) {
                        found.addPred(node);
                        node.addSucc(found);
                    }
                } else if (node.getInstruction() instanceof AssemblyItem.Instruction.SingleBranchInstruction &&
                        ((AssemblyItem.Instruction.SingleBranchInstruction) node.getInstruction()).opcode.equals("j")) {
                    AssemblyItem.Instruction.SingleBranchInstruction branch = (AssemblyItem.Instruction.SingleBranchInstruction) node.getInstruction();
                    CFGNode found = findLabelNode(entry, branch.label);
                    if (found != null) {
                        found.addPred(node);
                        node.addSucc(found);
                    }
                }

                succs.forEach(this::visitNode);
                return null;
            }
        };
        BranchAndJumpVisitor.visitNode(entry);
    }

    private CFGNode findLabelNode(CFGNode entry, AssemblyItem.Label label) {
        return new BaseCFGNodeVisitor<CFGNode>() {
            @Override
            public CFGNode job(CFGNode node) {
                if (node.getPreLabels().contains(label)) {
                    return node;
                } else {
                    for (CFGNode succ : node.getSuccs()) {
                        CFGNode ans = visitNode(succ);
                        if (ans != null) {
                            return ans;
                        }
                    }
                    return null;
                }
            }
        }.visitNode(entry);
    }

    private CFGNode buildCFG(AssemblyProgram.Section section) {
        CFGNode entry = new CFGNode(true);
        CFGNode pred = entry;
        List<AssemblyItem.Label> accLabels = new LinkedList<>();
        assert section.type == AssemblyProgram.Section.Type.TEXT;
        for (int i = 0; i < section.items.size(); i++) {
            AssemblyItem item = section.items.get(i);
            if (item instanceof AssemblyItem.Instruction) {
                CFGNode cur = new CFGNode((AssemblyItem.Instruction) item);
                cur.addPred(pred);
                pred.addSucc(cur);
                pred = cur;

                if (accLabels.size() != 0) {
                    cur.setPreLabels(accLabels);
                    accLabels.clear();
                }
            } else if (item instanceof AssemblyItem.Label) {
                accLabels.add((AssemblyItem.Label) item);
            }
        }
        return entry;
    }

    private void livenessAnalyze(CFGNode entry) {
        while (true) {
            final boolean[] updated = {false};
            new BaseCFGNodeVisitor<Void>() {
                @Override
                public Void job(CFGNode node) {
                    if (!node.isEntryNode()) {
                        Boolean updatedIn = node.updateLiveIn();
                        Boolean updatedOut = node.updateLiveOut();
                        if (updatedIn || updatedOut) {
                            updated[0] = true;
                        }
                    }
                    node.getSuccs().forEach(this::visitNode);
                    return null;
                }
            }.visitNode(entry);
            if (!updated[0]) break;
        }
    }

    private static class MoreThanOneLabelNodeFound extends Error {
        public MoreThanOneLabelNodeFound() {
            super("find more than one label node");
        }
    }
}
