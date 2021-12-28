package ast;

// Typecast expression : (Type)Expr (e.g. (int*) malloc(4))
public class TypecastExpr extends Expr{
    public Type t;
    public Expr expr;

    public TypecastExpr(Type type, Expr expr) {
        this.t = type;
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitTypecaseExpr(this);
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }
}
