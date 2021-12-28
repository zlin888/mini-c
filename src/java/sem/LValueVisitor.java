package sem;

import ast.*;

public class LValueVisitor extends BaseSemanticVisitor<Void> {
    @Override
    public Void visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl st) {
        return null;
    }

    @Override
    public Void visitBlock(Block b) {
        b.stmts.forEach(stmt -> stmt.accept(this));
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl p) {
        p.block.accept(this);
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        p.funDecls.forEach(funDecl -> funDecl.accept(this));
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd) {
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        return null;
    }

    @Override
    public Void visitAddressOfExpr(AddressOfExpr ao) {
        if (!(ao.expr instanceof VarExpr || ao.expr instanceof FieldAccessExpr ||
                ao.expr instanceof ArrayAccessExpr || ao.expr instanceof ValueAtExpr)) {
            error("[lvalue] not a lvalue in addressof: " + ao.expr);
        } else {
            ao.expr.accept(this);
        }
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aa) {
        aa.array.accept(this);
        aa.idx.accept(this);
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {
        return null;
    }

    @Override
    public Void visitAssign(Assign as) {
        if (!(as.left instanceof VarExpr || as.left instanceof FieldAccessExpr ||
                as.left instanceof ArrayAccessExpr || as.left instanceof ValueAtExpr)) {
            error("[lvalue] not a lvalue in assign: " + as.left);
        } else {
            as.left.accept(this);
            as.right.accept(this);
        }
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        bo.left.accept(this);
        bo.right.accept(this);
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral cl) {
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt es) {
        es.expr.accept(this);
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fa) {
        return null;
    }

    @Override
    public Void visitFunCallExpr(FunCallExpr fc) {
        return null;
    }

    @Override
    public Void visitIf(If iff) {
        iff.condition.accept(this);
        iff.ifBranch.accept(this);
        if (iff.elseBranch != null) {
            iff.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Void visitIntLiteral(IntLiteral il) {
        return null;
    }

    @Override
    public Void visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Void visitReturn(Return re) {
        if (re.expr != null) {
            re.expr.accept(this);
        }
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr so) {
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral sl) {
        return null;
    }

    @Override
    public Void visitStructType(StructType st) {
        return null;
    }

    @Override
    public Void visitTypecaseExpr(TypecastExpr tc) {
        tc.expr.accept(this);
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr va) {
        va.expr.accept(this);
        return null;
    }

    @Override
    public Void visitWhile(While wh) {
        wh.condition.accept(this);
        wh.stmt.accept(this);
        return null;
    }
}
