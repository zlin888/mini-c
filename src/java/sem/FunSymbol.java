package sem;

import ast.FunDecl;

public class FunSymbol extends Symbol {
    public FunDecl funDecl;

    public FunSymbol(FunDecl funDecl) {
        super(funDecl.name);
        this.funDecl = funDecl;
    }

    @Override
    public boolean isVarSymbol() {
        return false;
    }

    @Override
    public boolean isFunSymbol() {
        return true;
    }
}
