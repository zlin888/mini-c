package util;

import ast.*;

public class SizeHelper {
    public static int sizeOf(Type type) {
        if (type == BaseType.INT) {
            return 4;
        } else if (type == BaseType.CHAR) {
            return 4; // TODO: CHAR size is 1
        } else if(type instanceof PointerType) {
            return 4;
        } else if(type instanceof StructType) {
            StructType st = (StructType) type;
            StructTypeDecl std = st.getStd();
            return std.varDecls.stream().mapToInt(varDecl -> sizeOf(varDecl.type)).sum();
        } else if(type instanceof ArrayType) {
            ArrayType at = (ArrayType) type;
            int elementSize = sizeOf(at.elementType);
            int arraySize = elementSize * at.n;
            if (arraySize % 4 != 0) {
                arraySize = arraySize - (arraySize % 4) + 4; // padding
            }
            return arraySize;
        }
        return 0;
    }
}
