package gen;

import ast.*;
import gen.asm.AssemblyItem;
import gen.asm.AssemblyProgram;
import gen.asm.Register;
import util.SizeHelper;


/**
 * Generates code to evaluate an expression and return the result in a register.
 */
public class ExprGen implements ASTVisitor<Register> {

    private AssemblyProgram asmProg;

    public ExprGen(AssemblyProgram asmProg) {
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
        Register resReg = new Register.Virtual();
        AssemblyProgram.Section text = asmProg.getCurrSection();
        if (v.varDecl.isGlobal) {
            text.emitLoad("lw", resReg, v.varDecl.label);
        } else if (v.varDecl.isRegister) {
            return v.varDecl.register;
        } else {
            text.emitLoad("lw", resReg, Register.Arch.fp, v.varDecl.getOffset());
        }
        return resReg;
    }

    @Override
    public Register visitAddressOfExpr(AddressOfExpr ao) {
        return ao.expr.accept(new AddrGen(asmProg));
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr aa) {
        AssemblyProgram.Section text = asmProg.getCurrSection();
        Register elemAddr = aa.accept(new AddrGen(this.asmProg));
        Register resReg = new Register.Virtual();
        text.emit("ArrayAccess");
        text.emitLoad("lw", resReg, elemAddr, 0);
        return resReg;
    }

    @Override
    public Register visitArrayType(ArrayType at) {
        return null;
    }

    @Override
    public Register visitAssign(Assign as) {
        AssemblyProgram.Section text = asmProg.getCurrSection();
        if (as.left.type instanceof StructType && as.right.type instanceof StructType) {
            // Handle struct assignment
            text.emit("Assign for struct");
            StructTypeDecl std = ((StructType) as.left.type).getStd();
            for (VarDecl varDecl : std.varDecls) {
                new Assign(
                        new FieldAccessExpr(as.left, varDecl.varName),
                        new FieldAccessExpr(as.right, varDecl.varName)).accept(this);
            }
        } else {
            // if this is not a struct assignment
            // the only lvalues are: VarExpr, FieldAccessExpr, ArrayAccessExpr or ValueAtExpr
            if (as.left instanceof VarExpr && ((VarExpr) as.left).varDecl.isRegister) {
                Register valReg = as.right.accept(this);
                text.emit("Assign");
                text.emitMOVE(((VarExpr) as.left).varDecl.register, valReg);
            } else {
                Register addrReg = as.left.accept(new AddrGen(asmProg));
                Register valReg = as.right.accept(this);
                text.emit("Assign");
                text.emitStore("sw", valReg, addrReg, 0);
            }
        }
        return null;
    }

    @Override
    public Register visitBinOp(BinOp bo) {
        AssemblyProgram.Section text = asmProg.getCurrSection();
        Register lhsReg = bo.left.accept(this);
        Register resReg = new Register.Virtual();
        Register rhsReg;
        AssemblyItem.Label trueLabel;
        AssemblyItem.Label endLabel;
        switch (bo.op) {
            case LT:
                rhsReg = bo.right.accept(this);
                text.emit("slt", resReg, lhsReg, rhsReg);
                break;
            case LE:
                rhsReg = bo.right.accept(this);
                text.emit("sle", resReg, lhsReg, rhsReg);
                break;
            case GT:
                rhsReg = bo.right.accept(this);
                text.emit("slt", resReg, rhsReg, lhsReg);
                break;
            case GE:
                rhsReg = bo.right.accept(this);
                text.emit("sle", resReg, rhsReg, lhsReg);
                break;
            case NE:
                rhsReg = bo.right.accept(this);
                text.emit("sne", resReg, lhsReg, rhsReg);
                break;
            case EQ:
                text.emit("EQ");
                rhsReg = bo.right.accept(this);
                text.emit("seq", resReg, lhsReg, rhsReg);
                break;
            case OR:
                text.emit("OR");
                trueLabel = new AssemblyItem.Label();
                endLabel = new AssemblyItem.Label();
                // lhsReg != 0, and this OR is true
                text.emit("bne", lhsReg, Register.Arch.zero, trueLabel);
                // else
                rhsReg = bo.right.accept(this);
                // rhsReg != 0, and this OR is true
                text.emit("bne", rhsReg, Register.Arch.zero, trueLabel);
                // else
                text.emitLI(resReg, 0);
                text.emit("j", endLabel);

                text.emit(trueLabel);
                text.emitLI(resReg, 1);

                text.emit(endLabel);
                break;
            case AND:
                AssemblyItem.Label falseLabel = new AssemblyItem.Label();
                endLabel = new AssemblyItem.Label();
                text.emit("beq", lhsReg, Register.Arch.zero, falseLabel);

                rhsReg = bo.right.accept(this);
                text.emit("beq", rhsReg, Register.Arch.zero, falseLabel);
                // else
                text.emitLI(resReg, 1);
                text.emit("j", endLabel);

                text.emit(falseLabel);
                text.emitLI(resReg, 0);

                text.emit(endLabel);
                break;
            case ADD:
                rhsReg = bo.right.accept(this);
                text.emit("add", resReg, lhsReg, rhsReg);
                break;
            case MUL:
                rhsReg = bo.right.accept(this);
                text.emit("mul", resReg, lhsReg, rhsReg);
                break;
            case SUB:
                rhsReg = bo.right.accept(this);
                text.emit("sub", resReg, lhsReg, rhsReg);
                break;
            case DIV:
                rhsReg = bo.right.accept(this);
                text.emit("div", resReg, lhsReg, rhsReg);
                break;
            case MOD:
                rhsReg = bo.right.accept(this);
                text.emit("rem", resReg, lhsReg, rhsReg);
                break;
        }
        return resReg;
    }

    @Override
    public Register visitChrLiteral(ChrLiteral cl) {
        // put it in the data section
        AssemblyProgram.Section text = asmProg.getCurrSection();
        Register resReg = new Register.Virtual();
        int charAsciiCode = (int) cl.c;
        text.emitLI(resReg, charAsciiCode);

        return resReg;
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fa) {
        AssemblyProgram.Section text = asmProg.getCurrSection();
        Register resReg = new Register.Virtual();

        Register addrReg = fa.accept(new AddrGen(this.asmProg));
        text.emitLoad("lw", resReg, addrReg, 0);
        return resReg;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr fc) {
        if (fc.funDecl.name.equals("print_i")) {
            // inline print_i
            AssemblyProgram.Section text = asmProg.getCurrSection();
            text.emit("print_i");
            Register v = fc.args.get(0).accept(this);
            text.emitMOVE(Register.Arch.a0, v);
            text.emitLI(Register.Arch.v0, 1);
            text.emitSyscall();
        } else if (fc.funDecl.name.equals("print_s")) {
            AssemblyProgram.Section text = asmProg.getCurrSection();
            text.emit("print_s");
            Register v = fc.args.get(0).accept(this);
            text.emitMOVE(Register.Arch.a0, v);
            text.emitLI(Register.Arch.v0, 4);
            text.emitSyscall();
        } else if (fc.funDecl.name.equals("print_c")) {
            AssemblyProgram.Section text = asmProg.getCurrSection();
            text.emit("print_c");
            Register v = fc.args.get(0).accept(this);
            text.emitMOVE(Register.Arch.a0, v);
            text.emitLI(Register.Arch.v0, 11);
            text.emitSyscall();
        } else if (fc.funDecl.name.equals("read_c")) {
            AssemblyProgram.Section text = asmProg.getCurrSection();
            text.emit("read_c");
            text.emitLI(Register.Arch.v0, 12);
            text.emitSyscall();
            Register resReg = new Register.Virtual();
            text.emitMOVE(resReg, Register.Arch.v0);
            return resReg;
        } else if (fc.funDecl.name.equals("read_i")) {
            AssemblyProgram.Section text = asmProg.getCurrSection();
            text.emit("read_i");
            text.emitLI(Register.Arch.v0, 5);
            text.emitSyscall();
            Register resReg = new Register.Virtual();
            text.emitMOVE(resReg, Register.Arch.v0);
            return resReg;
        } else {
            AssemblyProgram.Section text = asmProg.getCurrSection();
            /*  precall
            1) pass the arguments via registers or push on the stack
            2) reserve space on stack for return value(if needed)
            3) push return address on the stack
            */
            // 1) pass the arguments via registers or push on the stack
            // push reversely
            text.emit("precall");
            text.emit("pass the arguments via registers or push on the stack");
            for (int i = fc.args.size() - 1; i >= 0; i--) {
                if (fc.args.get(i).type instanceof StructType) {
                    // push it to stack directly
                    Register addrReg = fc.args.get(i).accept(new AddrGen(asmProg));
                    int size = SizeHelper.sizeOf(fc.args.get(i).type);
                    text.emit("addi", Register.Arch.sp, Register.Arch.sp, -size);
                    for (int j = 0; j < size; j += 1) {
                        Register v = new Register.Virtual();
                        text.emitLoad("lb", v, addrReg, j + 4 - size);
                        text.emitStore("sb", v, Register.Arch.sp, j);
                    }
                } else if (fc.args.get(i).type instanceof ArrayType) {
                    Register argReg = fc.args.get(i).accept(new AddrGen(asmProg));
                    text.emit("addi", Register.Arch.sp, Register.Arch.sp, -4);
                    text.emitStore("sw", argReg, Register.Arch.sp, 0);
                } else {
                    Register argReg = fc.args.get(i).accept(this);
                    text.emit("addi", Register.Arch.sp, Register.Arch.sp, -4);
                    text.emitStore("sw", argReg, Register.Arch.sp, 0);
                }
            }
            // 2) reserve space on stack for return value(if needed)
            text.emit("2) reserve space on stack for return value(if needed)");
            text.emit("Space for return value");
            text.emit("addi", Register.Arch.sp, Register.Arch.sp, -4);
            // 3) push return address on the stack
            text.emit("3) push return address on the stack");
            text.emit("addi", Register.Arch.sp, Register.Arch.sp, -4);
            text.emitStore("sw", Register.Arch.ra, Register.Arch.sp, 0);

            text.emit("precall done");

            // function call
            text.emit("jal", fc.funDecl.label);

            /* postreturn
              1) restore return address from the stack
              2) read the return value from dedicated register or stack
              3) reset stack pointer
             */

            text.emit("postreturn");
            Register resReg = new Register.Virtual();
            // 1) restore return address from the stack
            text.emitLoad("lw", Register.Arch.ra, Register.Arch.sp, 0);
            // 2) read the return value from dedicated register or stack
            text.emitLoad("lw", resReg, Register.Arch.sp, 4);

            int totalSize = 0;
            for (Expr arg : fc.args) {
                if (arg.type instanceof ArrayType) {
                    // treat array as pointer
                    totalSize += 4;
                } else {
                    totalSize += SizeHelper.sizeOf(arg.type);
                }
            }
            // 3) reset stack pointer
            text.emit("addi", Register.Arch.sp, Register.Arch.sp, 8 + totalSize);
            text.emit("postreturn done");
            return resReg;
        }
        return null;
    }


    @Override
    public Register visitIntLiteral(IntLiteral il) {
        Register v = new Register.Virtual();
        AssemblyProgram.Section text = asmProg.getCurrSection();
        text.emit("IntLiteral");
        text.emitLI(v, il.i);
        return v;
    }

    @Override
    public Register visitPointerType(PointerType pt) {
        return null;
    }

    @Override
    public Register visitReturn(Return re) {
        throw new ShouldNotReach();
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr so) {
        AssemblyProgram.Section text = asmProg.getCurrSection();
        Register resReg = new Register.Virtual();
        int size = SizeHelper.sizeOf(so.t);
        text.emitLI(resReg, size);
        return resReg;
    }

    @Override
    public Register visitStrLiteral(StrLiteral sl) {
        sl.setLabel(new AssemblyItem.Label());
        asmProg.getDataSection().emit(new AssemblyItem.Directive.StringAllocation(sl));
        asmProg.getCurrSection().emit("load strLiteral into register");
        Register resReg = new Register.Virtual();
        asmProg.getCurrSection().emitLoad("la", resReg, sl.getLabel());
        return resReg;
    }

    @Override
    public Register visitStructType(StructType st) {
        return null;
    }

    @Override
    public Register visitTypecaseExpr(TypecastExpr tc) {
        return tc.expr.accept(this);
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr va) {
        AssemblyProgram.Section text = asmProg.getCurrSection();
        Register resReg = new Register.Virtual();
        Register addrReg = va.expr.accept(this);
        text.emit("value at");
        text.emitLoad("lw", resReg, addrReg, 0);
        return resReg;
    }

    @Override
    public Register visitWhile(While wh) {
        throw new ShouldNotReach();
    }

    @Override
    public Register visitIf(If iff) {
        throw new ShouldNotReach();
    }

    @Override
    public Register visitExprStmt(ExprStmt es) {
        throw new ShouldNotReach();
    }

    // TODO: to complete (only deal with Expression nodes, anything else should throw ShouldNotReach)
}
