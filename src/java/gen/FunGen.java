package gen;

import ast.*;
import gen.asm.AssemblyItem;
import gen.asm.AssemblyProgram;
import gen.asm.Register;
import util.SizeHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * A visitor that produces code for a function declaration
 */
public class FunGen implements ASTVisitor<Void> {

    private AssemblyProgram asmProg;
    private Map<VarDecl, Integer> vdOffsets;
    private int offset;
    private AssemblyProgram.Section text;
    private FunDecl currentFd;
    private Stack<Integer> offsets = new Stack<>();

    public FunGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
        this.vdOffsets = new HashMap<VarDecl, Integer>();
        this.offset = 0;
//        this.offsets.push(-Register.Arch.allocableArchs.length * 4);
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
    public Void visitBlock(Block b) {
        offsets.push(offsets.peek());
        b.varDecls.forEach(varDecl -> varDecl.accept(this));
        b.stmts.forEach(stmt -> stmt.accept(this));
        int offset = offsets.pop();
        text.emit("addi", Register.Arch.sp, Register.Arch.sp, -(offset - offsets.peek()));
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl p) {
        currentFd = p;
        // pop argument in order
        int preOffset = 8; // space for return value and return address
        for (int i = 0; i < p.params.size(); i++) {
            int size = SizeHelper.sizeOf(p.params.get(i).type);
            preOffset += size;
            p.params.get(i).setOffset(preOffset);
        }

        // Each function should be produced in its own section.
        // This is is necessary for the register allocator.
        if (p.name.equals("main")) {
            text = asmProg.newMainSection(AssemblyProgram.Section.Type.TEXT);
            p.label = new AssemblyItem.MainLabel();
        } else {
            text = asmProg.newSection(AssemblyProgram.Section.Type.TEXT);
            p.label = new AssemblyItem.Label(p.name);
        }
        text.emit(p.label);

        // prolog:
        text.emit("PROLOG");
        // 1) push the fp onto the stack
        text.emit("addi", Register.Arch.sp, Register.Arch.sp, -4);
        text.emitStore("sw", Register.Arch.fp, Register.Arch.sp, 0);
        // 2) initialise the fp
        text.emitMOVE(Register.Arch.fp, Register.Arch.sp);
        // 3) reserve space on the stack for local variables
//        p.block.varDecls.forEach(varDecl -> varDecl.accept(this));

        text.emit("4) save all saved registers onto the stack");
        // 4) save all saved registers onto the stack .... Naive Allocator decides what to save
        // function body
//        p.block.accept(this);
//        p.block.stmts.forEach(stmt -> stmt.accept(this));
        offsets.push(0);
        text.emit(AssemblyItem.Instruction.pushRegisters);

        p.block.varDecls.forEach(varDecl -> varDecl.accept(this));
        p.block.stmts.forEach(stmt -> stmt.accept(this));

        epilog(); // add epilog at the end of functioncall anyway
        return null;
    }

    public void epilog() {
        // epilog:
        int totalOffset = 0;

        for (int offset : offsets) {
            totalOffset += offset;
        }
        text.emit("addi", Register.Arch.sp, Register.Arch.sp, -4-totalOffset);

        text.emit("EPILOG");
        // 1) restore saved registers from the stack
        text.emit(AssemblyItem.Instruction.popRegisters);
        // 2) restore the stack pointer
//        text.emit("addi", Register.Arch.sp, Register.Arch.sp, 4 - offset);
        text.emitMOVE(Register.Arch.sp, Register.Arch.fp);
        text.emit("addi", Register.Arch.sp, Register.Arch.sp, 4);
        // reset offset
        offset = 0;
        // 3) restore the frame pointer from the stack
        text.emitLoad("lw", Register.Arch.fp, Register.Arch.fp, 0);

        if (!currentFd.name.equals("main")) {
            text.emitJR();
        } else {
            /* EXIT:
               li $v0, 10
                syscall
             */
            text.emitLI(Register.Arch.v0, 10);
            text.emitSyscall();
        }
    }

    @Override
    public Void visitProgram(Program p) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitVarDecl(VarDecl vd) {
        if (vd.isRegister) {
            vd.setRegister(new Register.Virtual());
        } else {
            text.emit("STACK ALLOCATE");
            text.emit("addi", Register.Arch.sp, Register.Arch.sp, -vd.size());
            offsets.push(offsets.pop() - vd.size());
            vd.setOffset(offsets.peek());
            // should allocate local variables on the stack and rember the offset from the frame pointer where they are stored (e.g. in the VarDecl AST node)
        }
        return null;
    }

    @Override
    public Void visitAssign(Assign as) {
        as.accept(new ExprGen(asmProg));
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt es) {
        es.expr.accept(new ExprGen(asmProg));
        return null;
    }

    @Override
    public Void visitWhile(While wh) {
        /*
         begin_branch:

         condition
         compare with zero, if is zero, go to end branch
         body
         go back to begin_branch

         end_branch:
         .....
         */
        AssemblyItem.Label endLabel = new AssemblyItem.Label("while_end");
        AssemblyItem.Label beginLabel = new AssemblyItem.Label("while_begin");
        text.emit(beginLabel);
        Register condReg = wh.condition.accept(new ExprGen(asmProg)); // condition
        text.emit("beq", Register.Arch.zero, condReg, endLabel); //if condition is zero to go end
        // else: execute body
        wh.stmt.accept(this);
        // go to begin, to check if condition is zero
        text.emit("j", beginLabel);
        text.emit(endLabel);
        return null;
    }

    @Override
    public Void visitIf(If iff) {
        /*
        condition
        compare with zero, if is zero, go to end branch or else branch
        if branch's body
        go to end_label

        else_label:
        else branch's body if it exists

        end_label:
         */
        AssemblyItem.Label elseLabel = new AssemblyItem.Label("if_else");
        AssemblyItem.Label endLabel = new AssemblyItem.Label("if_end");
        Register condReg = iff.condition.accept(new ExprGen(asmProg)); // condition
        text.emit("beq", Register.Arch.zero, condReg, elseLabel); //if condition is zero go to else_label
        // if branch's body
        iff.ifBranch.accept(this);
        text.emit("j", endLabel); // go to end

        //eles_branch:
        text.emit(elseLabel);
        if (iff.elseBranch != null) {
            iff.elseBranch.accept(this);
        }

        //end
        text.emit(endLabel);
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        // expression should be visited with the ExprGen when they appear in a statement (e.g. If, While, Assign ...)
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
    public Void visitBinOp(BinOp bo) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitChrLiteral(ChrLiteral cl) {
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
    public Void visitIntLiteral(IntLiteral il) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitPointerType(PointerType pt) {
        throw new ShouldNotReach();
    }

    @Override
    public Void visitReturn(Return re) {
        text.emit("Return");
        if (re.expr != null) {
            Register resReg = re.expr.accept(new ExprGen(asmProg));
            // sw$t0 ,  8($fp )# copy  the  r e t u r n  value  on  stack
            text.emitStore("sw", resReg, Register.Arch.fp, 8);
        }
        epilog(); // add epilog behind return anyway
        return null;
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


    // TODO: to complete (should only deal with statements, expressions should be handled by the ExprGen or AddrGen)
}
