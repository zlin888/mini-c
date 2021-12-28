package ast;

import java.util.HashMap;
import java.util.Map;

public class StructType implements Type {
    // represent a struct type (the String is the name of the declared struct type)
    public String name;
    public StructTypeDecl std;
    public static final Map<StructType, StructTypeDecl> structMap = new HashMap<StructType, StructTypeDecl>();

    public StructType(String name) {
        this.name = name;
    }

    public StructTypeDecl getStd() {
        return structMap.get(this);
    }

    public void setStd(StructTypeDecl std) {
        structMap.put(this, std);
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructType(this);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
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
        StructType other = (StructType) obj;
        return other.name.equals(this.name);
    }
}
