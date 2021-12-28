package ast;

public class If extends Stmt {
    public Expr condition;
    public Stmt ifBranch;
    public Stmt elseBranch; // optional

    public If(Expr condition, Stmt ifBranch) {
        this.condition = condition;
        this.ifBranch = ifBranch;
        this.elseBranch = null;
    }

    public If(Expr condition, Stmt ifBranch, Stmt elseBranch) {
        this.condition = condition;
        this.ifBranch = ifBranch;
        this.elseBranch = elseBranch;
    }

    public Expr getCondition() {
        return condition;
    }

    public void setCondition(Expr condition) {
        this.condition = condition;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitIf(this);
    }
}
