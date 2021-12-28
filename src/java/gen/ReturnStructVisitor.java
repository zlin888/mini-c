package gen;

import ast.*;
import sem.BaseSemanticVisitor;

import java.util.Stack;

public class ReturnStructVisitor implements ASTVisitor<Expr> {
    static int uniqueId = 0;

    static String getUniqueName(String prefix) {
        return prefix + "_return_struct_p" + uniqueId++;
    }

    Stack<Block> blocks = new Stack<>();
    Stack<Integer> blockIdxs = new Stack<>();
    FunDecl curFd;

    @Override
    public Expr visitBaseType(BaseType bt) {
        return null;
    }

    @Override
    public Expr visitStructTypeDecl(StructTypeDecl st) {
        return null;
    }

    @Override
    public Expr visitBlock(Block b) {
//        Block nb = new Block(b);
        int idx = 0;
        blocks.push(b);
        blockIdxs.push(idx);
        while (blockIdxs.peek() < b.stmts.size()) {
            b.getStmts().get(blockIdxs.peek()).accept(this);
            blockIdxs.push(blockIdxs.pop() + 1);
        }
        blocks.pop();
        blockIdxs.pop();
        return null;
    }

    @Override
    public Expr visitFunDecl(FunDecl p) {
        curFd = p;
        if (p.type instanceof StructType) {
            p.params.add(new VarDecl(new PointerType(p.type), getUniqueName("decl_vd")));
            p.setType(BaseType.VOID);
        }
        p.block.accept(this);
        return null;
    }

    @Override
    public Expr visitProgram(Program p) {
        p.funDecls.forEach(fd -> fd.accept(this));
        return null;
    }

    @Override
    public Expr visitVarDecl(VarDecl vd) {
        return null;
    }

    @Override
    public Expr visitVarExpr(VarExpr v) {
        return v;
    }

    @Override
    public Expr visitAddressOfExpr(AddressOfExpr ao) {
        ao.setExpr(ao.getExpr().accept(this));
        return ao;
    }

    @Override
    public Expr visitArrayAccessExpr(ArrayAccessExpr aa) {
        aa.setArray(aa.getArray().accept(this));
        aa.setIdx(aa.getIdx().accept(this));
        return aa;
    }

    @Override
    public Expr visitArrayType(ArrayType at) {
        return null;
    }

    @Override
    public Expr visitAssign(Assign as) {
        as.setLeft(as.getLeft().accept(this));
        as.setRight(as.getRight().accept(this));
        return null;
    }

    @Override
    public Expr visitBinOp(BinOp bo) {
        bo.setLeft(bo.getLeft().accept(this));
        bo.setRight(bo.getRight().accept(this));
        return bo;
    }

    @Override
    public Expr visitChrLiteral(ChrLiteral cl) {
        return cl;
    }

    @Override
    public Expr visitExprStmt(ExprStmt es) {
        es.setExpr(es.getExpr().accept(this));
        return null;
    }

    @Override
    public Expr visitFieldAccessExpr(FieldAccessExpr fa) {
        fa.setStructure(fa.getStructure().accept(this));
        return fa;
    }

    @Override
    public Expr visitFunCallExpr(FunCallExpr fc) {
        if (fc.type instanceof StructType) {
            VarDecl vd = new VarDecl(fc.type, getUniqueName("funcall_vd"));
            VarExpr ve = new VarExpr(vd, getUniqueName("funcall_ve"));
//            ve.type = fc.funDecl.type; // manually add type info
            fc.args.add(new AddressOfExpr(ve));

            blocks.peek().varDecls.add(vd);
            blocks.peek().stmts.add(blockIdxs.peek(), new ExprStmt(fc));
            blockIdxs.push(blockIdxs.pop() + 1);

            return ve;
        }
        return fc;
    }

    @Override
    public Expr visitIf(If iff) {
        iff.setCondition(iff.getCondition().accept(this));
        iff.ifBranch.accept(this);
        if (iff.elseBranch != null) {
            iff.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Expr visitIntLiteral(IntLiteral il) {
        return il;
    }

    @Override
    public Expr visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Expr visitReturn(Return re) {
        if (re.getExpr() != null) {
            if (re.getExpr().type instanceof StructType) {
                Expr returnExpr = re.getExpr();
                re.setExpr(null); // return nothing

                VarExpr ve = new VarExpr(curFd.params.get(curFd.params.size() - 1), getUniqueName("return_ve"));
                blocks.peek().getStmts().add(blockIdxs.peek(), new Assign(new ValueAtExpr(ve), returnExpr));
                blockIdxs.push(blockIdxs.pop() + 1);
            }
        }
        return null;
    }

    @Override
    public Expr visitSizeOfExpr(SizeOfExpr so) {
        return so;
    }

    @Override
    public Expr visitStrLiteral(StrLiteral sl) {
        return sl;
    }

    @Override
    public Expr visitStructType(StructType st) {
        return null;
    }

    @Override
    public Expr visitTypecaseExpr(TypecastExpr tc) {
        tc.setExpr(tc.getExpr().accept(this));
        return tc;
    }

    @Override
    public Expr visitValueAtExpr(ValueAtExpr va) {
        va.setExpr(va.getExpr().accept(this));
        return va;
    }

    @Override
    public Expr visitWhile(While wh) {
        wh.setCondition(wh.getCondition().accept(this));
        wh.stmt.accept(this);
        return null;
    }
}
