package ast;

public class ArrayAccessExpr extends Expr {
    public Type type;

    public Expr getArray() {
        return array;
    }

    public void setArray(Expr array) {
        this.array = array;
    }

    public Expr getIdx() {
        return idx;
    }

    public void setIdx(Expr idx) {
        this.idx = idx;
    }

    public Expr array;
    public Expr idx;

    public ArrayAccessExpr(Expr array, Expr idx) {
        this.array = array;
        this.idx = idx;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayAccessExpr(this);
    }
}
