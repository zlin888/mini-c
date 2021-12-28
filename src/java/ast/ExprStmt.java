package ast;

public class ExprStmt extends Stmt {
    public Expr expr;

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public ExprStmt(Expr expr) {
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitExprStmt(this);
    }
}
