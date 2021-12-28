package ast;

// Sizeof expression : sizeof(Type) (e.g. sizeof(int*))
public class SizeOfExpr extends Expr {
    public Type t;

    public SizeOfExpr(Type t) {
        this.t = t;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitSizeOfExpr(this);
    }
}
