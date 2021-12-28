package ast;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ArrayTypeTest {

    @Test
    public void testEqual() {
        Type a = new ArrayType(BaseType.INT, 3);
        Type b = new ArrayType(BaseType.INT, 3);
        assertEquals(a, b);
    }
}