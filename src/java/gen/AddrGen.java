package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.Register;
import util.SizeHelper;

/**
 * Generates code to calculate the address of an expression and return the result in a register.
 */
public class AddrGen implements ASTVisitor<Register> {


    private AssemblyProgram asmProg;

    public AddrGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
    }

    @Override
    public Register visitBaseType(BaseType bt) {
        throw new ShouldNotReach();
    }

    @Override
    public Register visitStructTypeDecl(StructTypeDecl st) {
        throw new ShouldNotReach();
    }

    @Override
    public Register visitBlock(Block b) {
        throw new ShouldNotReach();
    }

    @Override
    public Register visitFunDecl(FunDecl p) {
        throw new ShouldNotReach();
    }

    @Override
    public Register visitProgram(Program p) {
        throw new ShouldNotReach();
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        throw new ShouldNotReach();
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        // TODO: to complete
        Register resReg = new Register.Virtual();
        AssemblyProgram.Section text = asmProg.getCurrSection();
        if (v.varDecl.isRegister) {
            throw new RuntimeException("try to get the address of a register allocated var");
        }
        if (v.varDecl.isGlobal) {
            text.emitLoad("la", resReg, v.varDecl.label);
        } else {
            text.emitLoad("la", resReg, Register.Arch.fp, v.varDecl.getOffset());
        }
        return resReg;
    }

    @Override
    public Register visitAddressOfExpr(AddressOfExpr ao) {
        return null;
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr aa) {
        Register resReg = new Register.Virtual();
        AssemblyProgram.Section text = asmProg.getCurrSection();

        Register arrayLocReg = null;
        if (aa.array.type instanceof ArrayType) {
            arrayLocReg = aa.array.accept(this);
        } else if (aa.array.type instanceof PointerType) {
            arrayLocReg = aa.array.accept(new ExprGen(asmProg));
        } else {
            throw new RuntimeException("arrayaccess works on array or pointer");
        }
        Register idxReg = aa.idx.accept(new ExprGen(this.asmProg));

        // load elementsize into a register
        int elementSize = SizeHelper.sizeOf(aa.type);
        Register elemSizeReg = new Register.Virtual();
        text.emitLI(elemSizeReg, elementSize);

        // cal the offset and put it in a regiter
        // offset = elementsize * idx
        Register offsetReg = new Register.Virtual();
        text.emit("mul", offsetReg, elemSizeReg, idxReg);

        text.emit("add", resReg, arrayLocReg, offsetReg); // get the elem location
        return resReg;
    }

    @Override
    public Register visitArrayType(ArrayType at) {
        return null;
    }

    @Override
    public Register visitAssign(Assign as) {
        return null;
    }

    @Override
    public Register visitBinOp(BinOp bo) {
        return null;
    }

    @Override
    public Register visitChrLiteral(ChrLiteral cl) {
        return null;
    }

    @Override
    public Register visitExprStmt(ExprStmt es) {
        return null;
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fa) {
        Register resReg = new Register.Virtual();
        AssemblyProgram.Section text = asmProg.getCurrSection();

        // get the basic address of the struct
        Register structAddrReg = fa.structure.accept(this);

        // cal the offset
        StructTypeDecl std = ((StructType) fa.structure.type).getStd();
        int offset = 0;
//        for (int i = std.varDecls.size() - 1; i >= 0; i--) {
//            if (fa.fieldName.equals(std.varDecls.get(i).varName)) {
//                break;
//            } else {
//                offset += std.varDecls.get(i).size();
//            }
//        }
        for (VarDecl varDecl : std.varDecls) {
            if (fa.fieldName.equals(varDecl.varName)) {
                break;
            } else {
                offset -= varDecl.size();
            }
        }
        text.emit("field access address");
        text.emit("add", resReg, structAddrReg, offset);
        return resReg;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr fc) {
        return null;
    }

    @Override
    public Register visitIf(If iff) {
        return null;
    }

    @Override
    public Register visitIntLiteral(IntLiteral il) {
        return null;
    }

    @Override
    public Register visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Register visitReturn(Return re) {
        return null;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr so) {
        return null;
    }

    @Override
    public Register visitStrLiteral(StrLiteral sl) {
        return null;
    }

    @Override
    public Register visitStructType(StructType st) {
        return null;
    }

    @Override
    public Register visitTypecaseExpr(TypecastExpr tc) {
        return null;
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr va) {
        AssemblyProgram.Section text = asmProg.getCurrSection();
        Register addrOfPointer = va.expr.accept(this);
        Register resReg = new Register.Virtual();
        text.emitLoad("lw", resReg, addrOfPointer, 0);
        return resReg;
    }

    @Override
    public Register visitWhile(While wh) {
        return null;
    }

    // TODO: to complete (only deal with Expression nodes, anything else should throw ShouldNotReach)
}
