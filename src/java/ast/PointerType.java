package ast;

public class PointerType implements Type {
    public Type elementType;

    public PointerType(Type elementType) {
        this.elementType = elementType;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitPointerType(this);
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
        PointerType other = (PointerType) obj;
        return other.elementType.equals(this.elementType);
    }
}
