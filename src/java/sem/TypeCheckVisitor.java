package sem;

import ast.*;
import util.SizeHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

    private final Stack<FunDecl> funDeclStack = new Stack<>();
    private final Map<StructType, StructTypeDecl> structMap = new HashMap<StructType, StructTypeDecl>();

    private FunDecl getCurFunDecl() {
        return funDeclStack.peek();
    }

    private void pushFunDecl(FunDecl funDecl) {
        funDeclStack.push(funDecl);
    }

    private void popFunDecl() {
        funDeclStack.pop();
    }

    @Override
    public Type visitBaseType(BaseType bt) {
        // To be completed...
        return null;
    }

    @Override
    public Type visitStructTypeDecl(StructTypeDecl st) {
        structMap.put(st.type, st);
        st.varDecls.forEach(varDecl -> varDecl.accept(this));
        return null;
    }

    @Override
    public Type visitBlock(Block b) {
        b.varDecls.forEach(varDecl -> varDecl.accept(this));
        b.stmts.forEach(stmt -> stmt.accept(this));
        return null;
    }

    @Override
    public Type visitFunDecl(FunDecl funDecl) {
        pushFunDecl(funDecl);
        funDecl.params.forEach(param -> param.accept(this));
        funDecl.block.accept(this);
        popFunDecl();
        return null;
    }


    @Override
    public Type visitProgram(Program p) {
        try {
            p.structTypeDecls.forEach(std -> std.accept(this));
            p.varDecls.forEach(varDecl -> varDecl.accept(this));
            p.funDecls.forEach(funDecl -> funDecl.accept(this));
        } catch (NullPointerException e) {
            e.printStackTrace();
            error("[type] FAIL");
        }
        return null;
    }

    @Override
    public Type visitVarDecl(VarDecl vd) {
        if (vd.type == BaseType.VOID) {
            error("[Type] Var cannot be VOID");
        }
        StructType st = findStructType(vd.type);
        if (st != null) {
            st.setStd(structMap.get((StructType) st));
        }
        return vd.type;
    }

    public StructType findStructType(Type t) {
        if (t instanceof StructType) {
            return (StructType) t;
        } else if (t instanceof ArrayType) {
            return findStructType(((ArrayType) t).elementType);
        } else if (t instanceof PointerType) {
            return findStructType(((PointerType) t).elementType);
        } else {
            return null;
        }
    }

    @Override
    public Type visitVarExpr(VarExpr v) {
        v.type = v.varDecl.accept(this);
        return v.type;
    }

    @Override
    public Type visitAddressOfExpr(AddressOfExpr ao) {
        ao.type = new PointerType(ao.expr.accept(this));
        return ao.type;
    }

    @Override
    public Type visitArrayAccessExpr(ArrayAccessExpr aa) {
        Type arrayType = aa.array.accept(this);
        Type idxType = aa.idx.accept(this);
        if (idxType != BaseType.INT) {
            error("[Type] arrayaccess idx != INT, get " + idxType);
        } else {
            if (arrayType instanceof ArrayType) {
                aa.type = ((ArrayType) arrayType).elementType;
                return ((ArrayType) arrayType).elementType;
            } else if (arrayType instanceof PointerType) {
                aa.type = ((PointerType) arrayType).elementType;
                return ((PointerType) arrayType).elementType;
            } else {
                error("[Type] arrayaccess array should be arraytype or pointertype, get " + arrayType);
            }
        }
        return null;
    }

    @Override
    public Type visitArrayType(ArrayType at) {
        return null;
    }

    @Override
    public Type visitAssign(Assign as) {
        Type leftType = as.left.accept(this);
        Type rightType = as.right.accept(this);
        if (leftType == BaseType.VOID || leftType instanceof ArrayType) {
            error("[Type] assign should not be void or arraytype, get " + leftType);
        } else {
            if (!rightType.equals(leftType)) {
                error("[Type] lefttype != righttype, get " + leftType + "," + rightType);
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public Type visitBinOp(BinOp bo) {
        Type leftType = bo.left.accept(this);
        Type rightType = bo.right.accept(this);
        if (bo.op == Op.NE || bo.op == Op.EQ) {
            if (leftType instanceof StructType || leftType instanceof ArrayType || leftType == BaseType.VOID) {
                error("[Type] BinOp NE EQ left type should not be StructType,ArrayType,Void");
            }
            if (!rightType.equals(leftType)) {
                error("[Type] BinOp right type should equal to left type");
            }
        } else {
            if (leftType != BaseType.INT) {
                error("[Type] BinOp left type should be int, get: " + leftType);
            }
            if (!rightType.equals(leftType)) {
                error("[Type] BinOp right type should equal to left type");
            }
        }
        bo.type = BaseType.INT;
        return bo.type;
    }

    @Override
    public Type visitChrLiteral(ChrLiteral cl) {
        cl.type = BaseType.CHAR;
        return cl.type;
    }

    @Override
    public Type visitExprStmt(ExprStmt es) {
        es.expr.accept(this);
        return null;
    }

    @Override
    public Type visitFieldAccessExpr(FieldAccessExpr fa) {
        StructType structType = (StructType) fa.structure.accept(this);
        StructTypeDecl structTypeDecl = structType.getStd();
        if (structTypeDecl == null) {
            error("[Type] struct has not been declared: " + ((StructType) structType).name);
        } else {
            VarDecl varDecl = structTypeDecl.getVarDecl(fa.fieldName);
            if (varDecl == null) {
                error("[Type] struct doesn't has field " + fa.fieldName);
            } else {
                fa.type = varDecl.accept(this);
                return fa.type;
            }
        }
        return null;
    }

    @Override
    public Type visitFunCallExpr(FunCallExpr fc) {
        if (fc.args.size() != fc.funDecl.params.size()) {
            error("[type] funcall arg and param mismatch, args: " + fc.args.size() + " params: " + fc.funDecl.params.size());
            return null;
        }
        for (int i = 0; i < fc.args.size(); i++) {
            Type argType = fc.args.get(i).accept(this);
            Type paramType = fc.funDecl.params.get(i).accept(this);
            if (paramType instanceof PointerType && argType instanceof ArrayType &&
                    ((PointerType) paramType).elementType.equals(((ArrayType) argType).elementType)) {
                // ok
            } else if (!argType.equals(paramType)) {
                error("[type] funcall arg param type not match, should be " + paramType + ", get " + argType);
            }
        }
        fc.type = fc.funDecl.type;
        return fc.type;
    }

    @Override
    public Type visitIf(If iff) {
        Type conditionType = iff.condition.accept(this);
        iff.ifBranch.accept(this);
        if (iff.elseBranch != null) {
            iff.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Type visitIntLiteral(IntLiteral il) {
        il.type = BaseType.INT;
        return il.type;
    }

    @Override
    public Type visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Type visitReturn(Return re) {
        if (re.expr == null) {
            if (getCurFunDecl().type != BaseType.VOID) {
                error("[type] void function should not return anything");
            }
        } else if (!re.expr.accept(this).equals(getCurFunDecl().type)) {
            error("[type] return's expr's type should be equal to the fun type");
        }
        return null;
    }

    @Override
    public Type visitSizeOfExpr(SizeOfExpr so) {
        so.type = BaseType.INT;
        return so.type;
    }

    @Override
    public Type visitStrLiteral(StrLiteral sl) {
        sl.type = new ArrayType(BaseType.CHAR, sl.s.length() + 1);
        return sl.type;
    }

    @Override
    public Type visitStructType(StructType st) {
        return null;
    }

    @Override
    public Type visitTypecaseExpr(TypecastExpr tc) {
        Type eType = tc.expr.accept(this);
        if (eType == BaseType.CHAR) {
            if (tc.t != BaseType.INT) {
                error("[Type] char can only be casted to Int");
                return null;
            }
            tc.type = BaseType.INT;
            return tc.type;
        } else if (eType instanceof ArrayType) {
            Type elementType = ((ArrayType) eType).elementType;
            if (!(tc.t instanceof PointerType)) {
                error("[Type] array can only be casted to pointer");
                return null;
            }
            if (!((PointerType) tc.t).elementType.equals(elementType)) {
                error("[Type] array cast to pointer, element type not match");
                return null;
            }
            tc.type = tc.t;
        } else if (eType instanceof PointerType) {
            if (!(tc.t instanceof PointerType)) {
                error("[Type] pointer can only be casted to pointer");
                return null;
            }
            tc.type = tc.t;
        }
        return tc.type;
    }

    @Override
    public Type visitValueAtExpr(ValueAtExpr va) {
        Type addrType = va.expr.accept(this);
        if (!(addrType instanceof PointerType)) {
            error("[Type] ValueAt only works on Pointer");
        }
        va.type = ((PointerType) addrType).elementType;
        return va.type;
    }

    @Override
    public Type visitWhile(While wh) {
        Type conditionType = wh.condition.accept(this);
        if (conditionType != BaseType.INT) {
            error("[Type] while's condition should be an int, get " + conditionType);
        }
        wh.stmt.accept(this);
        return null;
    }
}
