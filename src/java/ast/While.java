package ast;

public class While extends Stmt {
    public Expr condition;
    public Stmt stmt;

    public While(Expr expr, Stmt stmt) {
        this.condition = expr;
        this.stmt = stmt;
    }

    public Expr getCondition() {
        return condition;
    }

    public void setCondition(Expr condition) {
        this.condition = condition;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitWhile(this);
    }
}
