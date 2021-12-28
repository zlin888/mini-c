package ast;

public class ValueAtExpr extends Expr {
    // *Expr
    public Expr expr;

    public ValueAtExpr(Expr expr) {
        this.expr = expr;
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitValueAtExpr(this);
    }
}
