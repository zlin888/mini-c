package sem;

import ast.VarDecl;

public class VarSymbol extends Symbol {
    public VarDecl varDecl;

    public VarSymbol(VarDecl varDecl) {
        super(varDecl.varName);
        this.varDecl = varDecl;
    }

    @Override
    public boolean isFunSymbol() {
        return false;
    }

    @Override
    public boolean isVarSymbol() {
        return true;
    }
}
