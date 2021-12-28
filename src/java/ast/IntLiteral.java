package ast;

public class IntLiteral extends Expr {
    public int i;

    public IntLiteral(int i) {
        this.i = i;
    }

    public IntLiteral(String sInt) {
        this.i = Integer.parseInt(sInt);
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIntLiteral(this);
    }
}

