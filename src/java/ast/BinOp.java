package ast;

public class BinOp extends Expr {
    public Expr left;
    public Expr right;
    public Op op;

    public Expr getLeft() {
        return left;
    }

    public void setLeft(Expr left) {
        this.left = left;
    }

    public Expr getRight() {
        return right;
    }

    public void setRight(Expr right) {
        this.right = right;
    }

    public BinOp(Expr left, Op op, Expr right) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBinOp(this);
    }
}
