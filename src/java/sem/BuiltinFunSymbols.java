package sem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ast.*;

public class BuiltinFunSymbols {
    // init method from https://www.baeldung.com/java-initialize-hashmap
    // TODO: fill in params and block (func's body)
    static Map<String, Symbol> builtinSymbolTable = Stream.of(new Object[][]{
            {"print_s", new FunSymbol(new FunDecl(
                    BaseType.VOID,
                    "print_s",
                    new LinkedList<>(Arrays.asList(new VarDecl(new PointerType(BaseType.CHAR), "s"))),
                    new Block(new LinkedList<>(), new LinkedList<>())))},
            {"print_i", new FunSymbol(new FunDecl(
                    BaseType.VOID,
                    "print_i",
                    new LinkedList<>(Arrays.asList(new VarDecl(BaseType.INT, "i"))),
                    new Block(new LinkedList<>(), new LinkedList<>())))},
            {"print_c", new FunSymbol(new FunDecl(
                    BaseType.VOID,
                    "print_c",
                    new LinkedList<>(Arrays.asList(new VarDecl(BaseType.CHAR, "c"))),
                    new Block(new LinkedList<>(), new LinkedList<>())))},
            {"read_c", new FunSymbol(new FunDecl(
                    BaseType.CHAR,
                    "read_c",
                    new LinkedList<>(),
                    new Block(new LinkedList<>(), new LinkedList<>())))},
            {"read_i", new FunSymbol(new FunDecl(
                    BaseType.INT,
                    "read_i",
                    new LinkedList<>(),
                    new Block(new LinkedList<>(), new LinkedList<>())))},
            {"mcmalloc", new FunSymbol(new FunDecl(
                    new PointerType(BaseType.VOID),
                    "mcmalloc",
                    new LinkedList<>(Arrays.asList(new VarDecl(BaseType.INT, "size"))),
                    new Block(new LinkedList<>(), new LinkedList<>())))},
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Symbol) data[1]));
}
