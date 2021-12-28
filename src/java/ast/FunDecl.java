package ast;

import gen.asm.AssemblyItem;

import java.util.List;

public class FunDecl implements ASTNode {
    public Type type;
    public final String name;
    public final List<VarDecl> params;
    public final Block block;
    public AssemblyItem.Label label;

    public FunDecl(Type type, String name, List<VarDecl> params, Block block) {
        this.type = type;
        this.name = name;
        this.params = params;
        this.block = block;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFunDecl(this);
    }

    public void setType(Type type) {
        this.type = type;
    }
}
