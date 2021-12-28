package gen;

import ast.*;
import gen.asm.AssemblyItem;
import gen.asm.AssemblyProgram;
import gen.asm.AssemblyItem.Label;
import gen.asm.AssemblyItem.Directive;

/**
 * This visitor should produce a program. Its job is simply to handle the global variable declaration by allocating
 * these in the data section. Then it should call the FunGen function generator to process each function declaration.
 * The label corresponding to each global variable can either be stored in the VarDecl AST node (simplest solution)
 * or store in an ad-hoc data structure (i.e. a Map) that can be passed to the other visitors.
 */
public class ProgramGen implements ASTVisitor<Void> {

    private final AssemblyProgram asmProg;

    public ProgramGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl st) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitBlock(Block b)  {
        throw new ShouldNotReach();
    }


    @Override
    public Void visitFunDecl(FunDecl fd) {
        // call the visitor specialized for handling function declaration
        return fd.accept(new FunGen(asmProg));
//        return new FunGen(asmProg).visitFunDecl(fd);
    }

    @Override
    public Void visitProgram(Program p) {
        p.varDecls.forEach(vd -> vd.accept(this));
        p.funDecls.forEach(fd -> fd.accept(this));
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd) {
        // Global VarDecl
        Label label = new Label(vd.varName);
        vd.setLabel(label);
        Directive directive = new Directive.StaticAllocation(vd);
        asmProg.getDataSection().emit(directive);
        // TODO: to complete: declare the variable globally in the data section and remember its label somewhere (e.g. in the VarDecl AST node directly).
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitAddressOfExpr(AddressOfExpr ao) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aa) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitArrayType(ArrayType at) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitAssign(Assign as) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitChrLiteral(ChrLiteral cl) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitExprStmt(ExprStmt es) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fa) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitFunCallExpr(FunCallExpr fc) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitIf(If iff) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitIntLiteral(IntLiteral il) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitPointerType(PointerType pt) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitReturn(Return re) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr so) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitStrLiteral(StrLiteral sl) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitStructType(StructType st) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitTypecaseExpr(TypecastExpr tc) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr va) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitWhile(While wh) {
        throw new ShouldNotReach();
    }
}
