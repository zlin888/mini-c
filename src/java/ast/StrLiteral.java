package ast;

import gen.asm.AssemblyItem;

public class StrLiteral extends Expr{
    public String s;
    public AssemblyItem.Label label;

    public StrLiteral(String s) {
        this.s = s;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStrLiteral(this);
    }

    public AssemblyItem.Label getLabel() {
        return label;
    }

    public void setLabel(AssemblyItem.Label label) {
        this.label = label;
    }
}
