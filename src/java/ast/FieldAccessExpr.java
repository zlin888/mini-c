package ast;

//// Field access expression : Expr.String (e.g. *a.b)
public class FieldAccessExpr extends Expr{
    // the Expr represents the structure, the String represents the name of the field
    public Expr structure;
    public String fieldName;

    public FieldAccessExpr(Expr structure, String fieldName) {
        this.structure = structure;
        this.fieldName = fieldName;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitFieldAccessExpr(this);
    }

    public Expr getStructure() {
        return structure;
    }

    public void setStructure(Expr structure) {
        this.structure = structure;
    }
}
