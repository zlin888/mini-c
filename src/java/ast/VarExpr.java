package ast;

public class VarExpr extends Expr {
    public final String varName;
    public VarDecl varDecl; // to be filled in by the name analyser

    public VarExpr(String name) {
        this.varName = name;
    }

    public VarExpr(VarDecl varDecl, String varName) {
        this.varName = varName;
        this.varDecl = varDecl;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitVarExpr(this);
    }
}
