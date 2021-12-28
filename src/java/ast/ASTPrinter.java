package ast;

import java.io.PrintWriter;

public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;
    private int indentLevel = 0;

    public ASTPrinter(PrintWriter writer) {
        this.writer = writer;
    }

    private void indent() {
        for (int i = 0; i < indentLevel * 4; i++) {
            writer.print(' ');
        }
    }

    private void inprint(String s) {
        indent();
        writer.print(s);
    }

    @Override
    public Void visitBlock(Block b) {
        writer.print("Block(");
        for (int i = 0; i < b.varDecls.size(); i++) {
            b.varDecls.get(i).accept(this);
            if(i != b.varDecls.size() - 1) writer.print(",");
        }
        if (b.varDecls.size() != 0 && b.stmts.size() != 0) writer.print(",");
        for (int i = 0; i < b.stmts.size(); i++) {
            b.stmts.get(i).accept(this);
            if(i != b.stmts.size() - 1) writer.print(",");
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        writer.print("FunDecl(");
        fd.type.accept(this);
        writer.print("," + fd.name + ",");
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(",");
        }
        fd.block.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.print("Program(");
        String delimiter = "";
        for (StructTypeDecl std : p.structTypeDecls) {
            writer.print(delimiter);
            delimiter = ",";
            std.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for (FunDecl fd : p.funDecls) {
            writer.print(delimiter);
            delimiter = ",";
            fd.accept(this);
        }
        writer.print(")");
        writer.flush();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd) {
        writer.print("VarDecl(");
        vd.type.accept(this);
        writer.print("," + vd.varName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        writer.print("VarExpr(");
        writer.print(v.varName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        writer.print(bt.toString());
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl st) {
        writer.print("StructTypeDecl(");
        st.type.accept(this);
        for (VarDecl varDecl : st.varDecls) {
            writer.print(",");
            varDecl.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitAddressOfExpr(AddressOfExpr ao) {
        writer.print("AddressOfExpr(");
        ao.expr.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aa) {
        writer.print("ArrayAccessExpr(");
        aa.array.accept(this);
        writer.print(",");
        aa.idx.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {
        writer.print("ArrayType(");
        at.elementType.accept(this);
        writer.print(",");
        writer.print(at.n);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitAssign(Assign as) {
        writer.print("Assign(");
        as.left.accept(this);
        writer.print(",");
        as.right.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        writer.print("BinOp(");
        bo.left.accept(this);
        writer.print(",");
        writer.print(bo.op.toString());
        writer.print(",");
        bo.right.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral cl) {
        writer.print("ChrLiteral(");
        writer.print(cl.c);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt es) {
        writer.print("ExprStmt(");
        es.expr.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fa) {
        writer.print("FieldAccessExpr(");
        fa.structure.accept(this);
        writer.print(",");
        writer.print(fa.fieldName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunCallExpr(FunCallExpr fc) {
        writer.print("FunCallExpr(");
        writer.print(fc.funcName);
        for (Expr param: fc.args) {
            writer.print(",");
            param.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIf(If iff) {
        writer.print("If(");
        iff.condition.accept(this);
        writer.print(",");
        iff.ifBranch.accept(this);
        if (iff.elseBranch != null) {
            writer.print(",");
            iff.elseBranch.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIntLiteral(IntLiteral il) {
        writer.print("IntLiteral(");
        writer.print(il.i);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitPointerType(PointerType pt) {
        writer.print("PointerType(");
        pt.elementType.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitReturn(Return re) {
        writer.print("Return(");
        if (re.expr != null) {
            re.expr.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr so) {
        writer.print("SizeOfExpr(");
        so.t.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStrLiteral(StrLiteral sl) {
        writer.print("StrLiteral(");
        writer.print(sl.s);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStructType(StructType st) {
        writer.print("StructType(");
        writer.print(st.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitTypecaseExpr(TypecastExpr tc) {
        writer.print("TypecastExpr(");
        tc.t.accept(this);
        writer.print(",");
        tc.expr.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr va) {
        writer.print("ValueAtExpr(");
        va.expr.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitWhile(While wh) {
        writer.print("While(");
        wh.condition.accept(this);
        writer.print(",");
        wh.stmt.accept(this);
        writer.print(")");
        return null;
    }
}
