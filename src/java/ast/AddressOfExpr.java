package ast;

/// &Expr (e.g. &(a[i]))
public class AddressOfExpr extends Expr {
    public Expr expr;

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public AddressOfExpr(Expr expr) {
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitAddressOfExpr(this);
    }
}
