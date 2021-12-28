package ast;

import java.util.List;

public class FunCallExpr extends Expr {
    // (the String corresponds to the name of the function to call and the Expr* is the list of arguments)
    public String funcName;
    public List<Expr> args;
    public FunDecl funDecl; // to be fill by name analysis

    public FunCallExpr(String funcName, List<Expr> args) {
        this.funcName = funcName;
        this.args = args;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFunCallExpr(this);
    }
}
