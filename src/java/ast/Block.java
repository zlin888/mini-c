package ast;

import java.util.LinkedList;
import java.util.List;

public class Block extends Stmt {

    public List<VarDecl> varDecls;
    public List<Stmt> stmts;

    public Block(List<VarDecl> varDecls, List<Stmt> stmts) {
        this.varDecls = varDecls;
        this.stmts = stmts;
    }
    
    public Block(Block block) {
        this.varDecls = new LinkedList<>(block.getVarDecls());
        this.stmts = new LinkedList<>(block.getStmts());
    }

    public <T> T accept(ASTVisitor<T> v) {
	    return v.visitBlock(this);
    }

    public List<VarDecl> getVarDecls() {
        return varDecls;
    }

    public List<Stmt> getStmts() {
        return stmts;
    }

    public void setVarDecls(List<VarDecl> varDecls) {
        this.varDecls = varDecls;
    }

    public void setStmts(List<Stmt> stmts) {
        this.stmts = stmts;
    }

    public void setAll(List<VarDecl> varDecls, List<Stmt> stmts) {
        this.setVarDecls(varDecls);
        this.setStmts(stmts);
    }

    public void setAll(Block block) {
        this.setVarDecls(block.getVarDecls());
        this.setStmts(block.getStmts());
    }
}
