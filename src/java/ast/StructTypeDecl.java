package ast;

import java.util.List;

public class StructTypeDecl implements ASTNode {

    public StructType type;
    public List<VarDecl> varDecls;

    public StructTypeDecl(StructType type, List<VarDecl> varDecls) {
        this.type = type;
        this.varDecls = varDecls;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructTypeDecl(this);
    }

    public VarDecl getVarDecl(String fieldName) {
        for(VarDecl varDecl : varDecls) {
            if (varDecl.varName.equals(fieldName)) {
                return varDecl;
            }
        }
        return null;
    }
}