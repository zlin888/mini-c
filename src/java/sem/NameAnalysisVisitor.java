package sem;

import ast.*;

import java.util.Stack;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {
	private final Stack<Scope> scopes;

	public NameAnalysisVisitor() {
		this(null);
	}

	public NameAnalysisVisitor(Scope outer) {
		this.scopes = new Stack<Scope>();
		this.scopes.push(new Scope(outer, BuiltinFunSymbols.builtinSymbolTable));
	}

	public Scope getScope() {
		return scopes.peek();
	}

	public Scope pushScope(Scope scope) {
	    scopes.push(scope);
	    return scope;
	}

	public Scope popScope() {
		return scopes.pop();
	}

	@Override
	public Void visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitStructTypeDecl(StructTypeDecl sts) {
	    // new scope inside a struct
		// not allowed to declare twice the same field in one struct type decl
	    pushScope(new Scope(getScope()));
		for(VarDecl varDecl: sts.varDecls) {
		    if (getScope().lookupCurrent(varDecl.varName) != null) {
		    	error("[name analysis] declare twice the same field in struct: " + varDecl.varName);
			} else {
		    	getScope().put(new VarSymbol(varDecl));
			}
		}
		popScope();
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
	    pushScope(new Scope(getScope()));
		for (VarDecl varDecl: b.varDecls) {
		    varDecl.accept(this);
		}
		for (Stmt stmt: b.stmts) {
		    stmt.accept(this);
		}
	    popScope();
		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl funDecl) {
		// add fundecl to the scope
	    if(getScope().lookupCurrent(funDecl.name) != null) {
	    	error("[name analysis] redecl function: " + funDecl.name);
		} else {
	    	getScope().put(new FunSymbol(funDecl));
		}

	    // handle the new scope inside the function's block
	    pushScope(new Scope(getScope()));
		for (VarDecl varDecl: funDecl.params) {
		    varDecl.accept(this);
		}
		funDecl.block.accept(this);
	    popScope();
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr fc) {
	    Symbol s = getScope().lookup(fc.funcName);
	    if (s == null || !s.isFunSymbol()) {
	    	error("[name analysis] function has not been decl: " + fc.funcName);
		} else {
	    	fc.funDecl = ((FunSymbol) s).funDecl;
		}
	    for(Expr arg: fc.args) {
	        arg.accept(this);
		}
		return null;
	}

	@Override
	public Void visitProgram(Program p) {
		try {
			// a scope has been pushed, when visitor is initialized.
			for (StructTypeDecl std: p.structTypeDecls) {
				std.accept(this);
			}
			for (VarDecl varDecl: p.varDecls) {
				varDecl.accept(this);
			}
			for (FunDecl funDecl: p.funDecls) {
				funDecl.accept(this);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			error("[name analysis] FAIL");
		}
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		// To be completed...
		Symbol s = getScope().lookupCurrent(vd.varName);
		if (s != null) {
		    error("[name analysis] variable decl multiple times: " + vd.varName);
		} else {
			getScope().put(new VarSymbol(vd));
		}
		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
	    Symbol s = getScope().lookup(v.varName);
	    if (s == null || !s.isVarSymbol()) {
	        error("[name analysis] variable has been decl: " + v.varName);
		} else {
	    	v.varDecl = ((VarSymbol) s).varDecl;
		}
		return null;
	}

	@Override
	public Void visitAddressOfExpr(AddressOfExpr ao) {
		ao.expr.accept(this);
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
		as.left.accept(this);
		as.right.accept(this);
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
		fa.structure.accept(this);
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
		if(re.expr != null) {
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
