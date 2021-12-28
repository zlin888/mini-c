package ast;

import gen.asm.AssemblyItem.Label;
import gen.asm.Register;
import util.SizeHelper;

public class VarDecl implements ASTNode {
    public final Type type;
    public final String varName;
    public Label label;
    public boolean isGlobal = false; // statically allocated
    public int offset = -1;
    public boolean isRegister; // register allocated
    public Register register;

    public VarDecl(Type type, String varName) {
        this.type = type;
        this.varName = varName;
    }

    public void setLabel(Label label) {
        this.label = label;
        this.isGlobal = true;
    }

    public int getOffset() {
        if (offset == -1) {
            throw new NullPointerException("offset is not set");
        }
        return offset;
    }

    public void setOffset(int offset) {
        this.isGlobal = false;
        this.offset = offset;
    }

    public int size() {
        return SizeHelper.sizeOf(type);
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitVarDecl(this);
    }

    public void setRegisterFlag(boolean register) {
        isRegister = register;
    }

    public void setRegisterFlag() {
        if (type instanceof ArrayType || type instanceof StructType) {
            setRegisterFlag(false);
        } else {
            setRegisterFlag(true);
        }
    }

    public void setRegister(Register register) {
        this.register = register;
    }
}
