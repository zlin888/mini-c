package ast;

public class Return extends Stmt {
    public Expr expr; // optional

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public Return() {
        this.expr = null;
    }

    public Return(Expr expr) {
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitReturn(this);
    }
}
