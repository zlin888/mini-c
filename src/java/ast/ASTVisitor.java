package ast;

public interface ASTVisitor<T> {

    class ShouldNotReach extends Error {
        public ShouldNotReach() {
            super("Current visitor should never reach this node");
        }
    }

    public T visitBaseType(BaseType bt);
    public T visitStructTypeDecl(StructTypeDecl st);
    public T visitBlock(Block b);
    public T visitFunDecl(FunDecl p);
    public T visitProgram(Program p);
    public T visitVarDecl(VarDecl vd);
    public T visitVarExpr(VarExpr v);
    public T visitAddressOfExpr(AddressOfExpr ao);
    public T visitArrayAccessExpr(ArrayAccessExpr aa);
    public T visitArrayType(ArrayType at);
    public T visitAssign(Assign as);
    public T visitBinOp(BinOp bo);
    public T visitChrLiteral(ChrLiteral cl);
    public T visitExprStmt(ExprStmt es);
    public T visitFieldAccessExpr(FieldAccessExpr fa);
    public T visitFunCallExpr(FunCallExpr fc);
    public T visitIf(If iff);
    public T visitIntLiteral(IntLiteral il);
    public T visitPointerType(PointerType pt);
    public T visitReturn(Return re);
    public T visitSizeOfExpr(SizeOfExpr so);
    public T visitStrLiteral(StrLiteral sl);
    public T visitStructType(StructType st);
    public T visitTypecaseExpr(TypecastExpr tc);
    public T visitValueAtExpr(ValueAtExpr va);
    public T visitWhile(While wh);
    // to complete ... (should have one visit method for each concrete AST node class)
}
