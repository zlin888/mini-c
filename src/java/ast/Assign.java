package ast;

public class Assign extends Stmt {
    public Expr left;

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

    public Expr right;

    public Assign(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitAssign(this);
    }
}
