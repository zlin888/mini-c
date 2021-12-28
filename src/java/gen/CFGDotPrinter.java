package gen;

import util.ToFile;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static util.ToFile.writeTo;

public class CFGDotPrinter extends BaseCFGNodeVisitor<Void> {

    public DotGraph dg = new DotGraph();
    public String path;

    public CFGDotPrinter(String path) {
        this.path = path;
    }

    public CFGDotPrinter() {
        this.path = "dot.gv";
    }

    public void run(CFGNode node) {
        visitNode(node);
        writeTo(path, dg.toString());
    }

    @Override
    public Void job(CFGNode node) {
        DotText dt = new DotText(node.toString());
        node.getSuccs().forEach(n -> {
                    dg.addChild(new DotArrow(dt, new DotText(n.toString())));
                    visitNode(n);
                }
        );
        return null;
    }

    static abstract class DotNode {
        abstract public String toString();
    }

    private class DotGraph extends DotNode {
        public List<DotNode> children;

        public DotGraph(List<DotNode> children) {
            this.children = children;
        }

        public DotGraph() {
            this.children = new LinkedList<>();
        }

        public void addChild(DotNode dn) {
            children.add(dn);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("digraph {\n");
            children.forEach(dotNode -> {
                sb.append("  ");
                sb.append(dotNode.toString());
                sb.append("\n");
            });
            sb.append("}");
            return sb.toString();
        }
    }

    private class DotArrow extends DotNode {
        public DotText left;
        public DotText right;

        public DotArrow(DotText left, DotText right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return left.toString() + " -> " + right.toString();
        }
    }

    private class DotText extends DotNode {
        public String content;

        public DotText(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "\"" + content + "\"";
        }
    }
}


