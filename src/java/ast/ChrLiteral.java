package ast;

public class ChrLiteral extends Expr{
    public char c;

    public ChrLiteral(char c) {
        this.c = c;
    }

    public ChrLiteral(String sc) {
        if (sc.equals("\\n")) {
            this.c = '\n';
        } else if (sc.equals("\\t")) {
            this.c = '\t';
        } else if (sc.equals("\\b")) {
            this.c = '\b';
        } else if (sc.equals("\\m")) {
            this.c = '\b';
        } else if (sc.equals("\\r")) {
            this.c = '\b';
        } else if (sc.equals("\\f")) {
            this.c = '\b';
        } else if (sc.equals("\\\'")) {
            this.c = '\'';
        } else if (sc.equals("\\\"")) {
            this.c = '\"';
        } else if (sc.equals("\\\\")) {
            this.c = '\\';
        } else {
            this.c = sc.charAt(0);
        }
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitChrLiteral(this);
    }
}
