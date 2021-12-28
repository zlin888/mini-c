package ast;

public class ArrayType implements Type {
    public Type elementType;
    public int n;

    public ArrayType(Type elementType, int n) {
        this.elementType = elementType;
        this.n = n;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitArrayType(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ArrayType other = (ArrayType) obj;
        return other.elementType.equals(this.elementType) && this.n == other.n;
    }
}
