package parser;

import ast.*;
import lexer.Token;
import lexer.Token.TokenClass;
import lexer.Tokeniser;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/*
exp    ::=  L2 ("||" exp | ε)            =     L2 ("||" L2)*
L2     ::=  L3 ("&&" L2 | ε)             =     L3 ("&&" L3)*
L3     ::=  L4 (("=="| "!=") L3 | ε)     =     L4 ("=="|"!=" L4)*
L4     ::=  L5 ((<|<=|>|>=) L4 | ε)      =     L5 ((<|<=|>|>=) L5)*
L5     ::=  L6 (("+"|"-") L5 | ε)        =     L6 (("+"|"-") L6)*
L6     ::=  C (("*"|"/"|"%") L6 | ε)     =     C (("*"|"/"|"%") C)*           # left-associate
C  ::=  ("(" A | IDENT B | others) C'
A      ::=  exp ")" | type ")" exp                 # (exp) & type casting
B      ::=  "(" [ exp ("," exp)* ] ")" | ε         # IDENT & function call
C' ::=  "[" exp "]" C' | "." IDENT C' | ε         # arrayaccess & fieldaccess
others ::=  INT_LITERAL | CHAR_LITERAL | STRING_LITERAL | ("-" | "+") exp | valueat | addressof | sizeof
 */
/**
 * @author zhitao
 */
public class Parser {

    private Token token;

    // use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
    private final Queue<Token> buffer = new LinkedList<>();

    private final Tokeniser tokeniser;


    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public Program parse() {
        // get the first token
        nextToken();
        return parseProgram();
    }

    public int getErrorCount() {
        return error;
    }

    private int error = 0;
    private Token lastErrorToken;

    private void error(TokenClass... expected) {

        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected (" + sb + ") found (" + token + ") at " + token.position);

        error++;
        lastErrorToken = token;
    }

    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());
        assert buffer.size() >= i;

        int cnt = 1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }

        assert false; // should never reach this
        return null;
    }


    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     * Returns the expected token or null if an error occurred.
     */
    private Token expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                Token cur = token;
                nextToken();
                return cur;
            }
        }

        error(expected);
        return null;
    }

    /*
     * Returns true if the current token is equals to any of the expected ones.
     */
    private boolean accept(TokenClass... expected) {
        boolean result = false;
        for (TokenClass e : expected)
            result |= (e == token.tokenClass);
        return result;
    }


    private Program parseProgram() {
        parseIncludes();
        List<StructTypeDecl> stds = parseStructDecls();
        List<VarDecl> vds = parseVarDecls();
        List<FunDecl> fds = parseFunDecls();
        expect(TokenClass.EOF);
        return new Program(stds, vds, fds);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private List<StructTypeDecl> parseStructDecls() {
        // structdecl ::= structtype "{" (vardecl)+ "}" ";"    # structure declaration
        // notice that a varDecls could also start with a structType
        List<StructTypeDecl> structTypeDecls = new LinkedList<StructTypeDecl>();
        if (isStructType() && lookAhead(2).tokenClass == TokenClass.LBRA) // structType {
        {
            StructType st = parseStructType();
            expect(TokenClass.LBRA);
            List<VarDecl> varDecls = parseVarDecls();
            expect(TokenClass.RBRA);
            expect(TokenClass.SC);
            structTypeDecls.add(new StructTypeDecl(st, varDecls));
            structTypeDecls.addAll(parseStructDecls()); // continue
        }
        return structTypeDecls;
    }

    private List<VarDecl> parseVarDecls() {
        List<VarDecl> varDecls = new LinkedList<VarDecl>();
        if (isVarDecls()) {
            // normal declaration, e.g. int a;
            Type type = parseType();
            String name = "";
            if (accept(TokenClass.IDENTIFIER)) {
                name = token.data;
                nextToken();
            } else {
                error(TokenClass.IDENTIFIER);
            }
            if (accept(TokenClass.SC)) {
                nextToken();
                varDecls.add(new VarDecl(type, name));
                varDecls.addAll(parseVarDecls()); // continue
            } else if (accept(TokenClass.LSBR)) {
                // array declaration, e.g. int a[2];
                nextToken();
                int n = Integer.parseInt(expect(TokenClass.INT_LITERAL).data);
                expect(TokenClass.RSBR);
                expect(TokenClass.SC);
                Type arrayType = new ArrayType(type, n);
                varDecls.add(new VarDecl(arrayType, name));
                varDecls.addAll(parseVarDecls()); // continue
            } else {
                error(TokenClass.SC, TokenClass.LSBR);
            }
        }
        return varDecls; // return an empty LinkedList
    }

    private boolean isVarDecls() {
        // notice that function also starts with "type IDENT"
        if (isType()) {
            int offset = lookAheadAType();
            return lookAhead(offset).tokenClass == TokenClass.IDENTIFIER &&
                    (lookAhead(offset + 1).tokenClass == TokenClass.SC || lookAhead(offset + 1).tokenClass == TokenClass.LSBR);
        } else return false;
    }

    private boolean isVarDecls(int lookAhead) {
        // notice that function also starts with "type IDENT"
        return isType(lookAhead) &&
                lookAhead(1 + lookAhead).tokenClass == TokenClass.IDENTIFIER &&
                (lookAhead(2 + lookAhead).tokenClass == TokenClass.SC || lookAhead(2 + lookAhead).tokenClass == TokenClass.LSBR);
    }

    private List<FunDecl> parseFunDecls() {
        List<FunDecl> funcDecls = new LinkedList<FunDecl>();
        if (isFunDecls()) {
            Type type = parseType();
            String name = expect(TokenClass.IDENTIFIER).data;
            expect(TokenClass.LPAR);
            List<VarDecl> varDecls = parseParams();
            expect(TokenClass.RPAR);
            Block block = parseBlock();
            funcDecls.add(new FunDecl(type, name, varDecls, block));
            funcDecls.addAll(parseFunDecls());
        }
        return funcDecls;
    }

    private boolean isFunDecls() {
        if (isStructType() &&
                lookAhead(2).tokenClass == TokenClass.IDENTIFIER &&
                lookAhead(3).tokenClass == TokenClass.LPAR)
        return true;
        else {
            return isType() &&
                    lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(2).tokenClass == TokenClass.LPAR;
        }
    }

    private List<VarDecl> parseParams() {
        List<VarDecl> varDecls = new LinkedList<VarDecl>();
        if (isType()) {
            Type type = parseType();
            String name = expect(TokenClass.IDENTIFIER).data;
            varDecls.add(new VarDecl(type, name));
        }
        if (accept(TokenClass.COMMA)) {
            nextToken(); // consume COMMA
            varDecls.addAll(parseParams());
        }
        return varDecls;
    }

    private StructType parseStructType() {
        if (accept(TokenClass.STRUCT)) {
            nextToken();
            return new StructType(expect(TokenClass.IDENTIFIER).data);
        }
        return null;
    }

    private boolean isStructType() {
        return accept(TokenClass.STRUCT) && lookAhead(1).tokenClass == TokenClass.IDENTIFIER;
    }

    private boolean isStructType(int lookAhaed) {
        return accept(TokenClass.STRUCT) && lookAhead(1 + lookAhaed).tokenClass == TokenClass.IDENTIFIER;
    }

    private Type parseType() {
        // BasicType
        Type type = null;
        if (accept(TokenClass.INT)) {
            nextToken();
            type = BaseType.INT;
        } else if (accept(TokenClass.CHAR)) {
            nextToken();
            type = BaseType.CHAR;
        } else if (accept(TokenClass.VOID)) {
            nextToken();
            type = BaseType.VOID;
        } else if (accept(TokenClass.STRUCT)) {
            type = parseStructType();
        } else error(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT);
        if (accept(TokenClass.ASTERIX)) {
            nextToken(); // consume asterix
            return new PointerType(type);
        } else {
            return type;
        }
    }

    // can if current token can be parsed as Type; no side affect
    private boolean isType() {
        return accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID) || isStructType();
    }

    private boolean isType(int lookAhaed) {
        return accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID) || isStructType(lookAhaed);
    }

    // offset is used to lookAhead behind a type
    private int lookAheadAType() {
        int offset = 0;
        if (isType()) {
            if (isStructType()) {
                offset += 2;
                if (lookAhead(2).tokenClass == TokenClass.ASTERIX) offset += 1;
            } else {
                offset += 1;
                if (lookAhead(1).tokenClass == TokenClass.ASTERIX) offset += 1;
            }
        }
        return offset;
    }

    private Block parseBlock() {
        expect(TokenClass.LBRA);
        List<VarDecl> varDecls = parseVarDecls();
        List<Stmt> stmts = parseStmtsForBlock();
        expect(TokenClass.RBRA);
        return new Block(varDecls, stmts);
    }

    private boolean isBlock() {
        return accept(TokenClass.LBRA) && isVarDecls(1);

    }

    private List<Stmt> parseStmtsForBlock() {
        // no Stmt until reach an '}'
        List<Stmt> stmts = new LinkedList<Stmt>();
        if (!accept(TokenClass.RBRA)) {
            Stmt stmt = parseStmt();
            if (stmt == null) {
                assert false;
                return stmts; // TODO: should not reach here
            } else {
                stmts.add(stmt);
                stmts.addAll(parseStmtsForBlock());
                return stmts;
            }

        } else { // reach the end here
            return stmts; // empty list
        }
    }

    private Stmt parseStmt() {
        if (accept(TokenClass.LBRA)) { // BLOCK
            return parseBlock();
        } else if (isWhileLoop()) {
            return parseWhileLoop();
        } else if (isIf()) {
            return parseIf();
        } else if (isReturn()) {
            return parseReturn();
        } else {
            Expr expr = parseExp();
            if (accept(TokenClass.ASSIGN)) { //ASSIGNMENT
                nextToken(); // consume ASSIGN
                Expr right = parseExp();
                expect(TokenClass.SC);
                return new Assign(expr, right);
            } else {
                if (!accept(TokenClass.SC)) { // NOT A STMT
                    expect(TokenClass.SC);
                    return null;
                } else {
                    expect(TokenClass.SC); // EXPRESSION STATEMENT
                    return new ExprStmt(expr);
                }
            }
        }
    }

//    private boolean isStmt() {
//        return isBlock() || isWhileLoop() || isIf() || isReturn() || V
//    }

    private Stmt parseWhileLoop() {
        expect(TokenClass.WHILE);
        expect(TokenClass.LPAR);
        Expr condition = parseExp();
        expect(TokenClass.RPAR);
        Stmt stmt = parseStmt();
        return new While(condition, stmt);
    }

    private boolean isWhileLoop() {
        return accept(TokenClass.WHILE);
    }

    private If parseIf() {
        expect(TokenClass.IF);
        expect(TokenClass.LPAR);
        Expr condition = parseExp();
        expect(TokenClass.RPAR);
        Stmt ifBranch = parseStmt();
        Stmt elseBranch = null;
        if (accept(TokenClass.ELSE)) {
            nextToken();
            elseBranch = parseStmt();
        }
        return new If(condition, ifBranch, elseBranch);
    }

    private boolean isIf() {
        return accept(TokenClass.IF);
    }

    private Return parseReturn() {
        expect(TokenClass.RETURN);
        Expr expr = null;
        if (!accept(TokenClass.SC)) expr = parseExp();
        expect(TokenClass.SC);
        return new Return(expr);

    }

    private boolean isReturn() {
        return accept(TokenClass.RETURN);
    }

    // exp    ::=  L2 ("||" exp | ε)            =     L2 ("||" L2)*
    private Expr parseExp() {
        return parseLorRest(parseLand());
    }

    private Expr parseLorRest(Expr pre) {
        if (accept(TokenClass.LOGAND)) {
            nextToken();
            Expr cur = parseLand();
            return parseLorRest(new BinOp(pre, Op.OR, cur));
        } else {
            return pre;
        }
    }

    //  L2     ::=  L3 ("&&" L2 | ε)             =     L3 ("&&" L3)*
    private Expr parseLand() {
        return parseLandRest(parseEq());
    }

    private Expr parseLandRest(Expr pre) {
        if (accept(TokenClass.LOGAND)) {
            nextToken();
            Expr cur = parseEq();
            return parseLandRest(new BinOp(pre, Op.AND, cur));
        } else {
            return pre;
        }
    }

    // L3     ::=  L4 (("=="| "!=") L3 | ε)     =     L4 ("=="|"!=" L4)*
    private Expr parseEq() {
        return parseEqRest(parseCompare());
    }

    private Expr parseEqRest(Expr pre) {
        if (accept(TokenClass.EQ)) {
            nextToken();
            Expr cur = parseCompare();
            return parseEqRest(new BinOp(pre, Op.EQ, cur));
        } else if (accept(TokenClass.NE)) {
            nextToken();
            Expr cur = parseCompare();
            return parseEqRest(new BinOp(pre, Op.NE, cur));
        } else {
            return pre;
        }
    }

    // L4     ::=  L5 ((< |<= | >| >=) L4 | ε)      =     L5 ((< |<= | >| >=) L5)*
    private Expr parseCompare() {
        return parseCompareRest(parseAddSub());
    };

    private Expr parseCompareRest(Expr pre) {
        if (accept(TokenClass.LT)) {
            nextToken();
            Expr cur = parseAddSub();
            return parseCompareRest(new BinOp(pre, Op.LT, cur));
        } else if (accept(TokenClass.LE)) {
            nextToken();
            Expr cur = parseAddSub();
            return parseCompareRest(new BinOp(pre, Op.LE, cur));
        } else if (accept(TokenClass.GT)) {
            nextToken();
            Expr cur = parseAddSub();
            return parseCompareRest(new BinOp(pre, Op.GT, cur));
        } else if (accept(TokenClass.GE)) {
            nextToken();
            Expr cur = parseAddSub();
            return parseCompareRest(new BinOp(pre, Op.GE, cur));
        } else {
            return pre;
        }
    }

    // L5     ::=  L6 (("+"|"-") L5 | ε)        =     L6 (("+"|"-") L6)*
    private Expr parseAddSub() {
        return parseAddSubRest(parseMulDivMod());
    };

    private Expr parseAddSubRest(Expr pre) {
        if (accept(TokenClass.PLUS)) {
            nextToken();
            Expr cur = parseMulDivMod();
            return parseAddSubRest(new BinOp(pre, Op.ADD, cur));
        } else if (accept(TokenClass.MINUS)) {
            nextToken();
            Expr cur = parseMulDivMod();
            return parseAddSubRest(new BinOp(pre, Op.SUB, cur));
        } else {
            return pre;
        }
    }

    // L6     ::=  C (("*"|"/"|"%") L6 | ε)     =     C (("*"|"/"|"%") C)*           # left-associate
    private Expr parseMulDivMod() {
        return parseMulDivModRest(parseC());
    }

    private Expr parseMulDivModRest(Expr pre) {
        if (accept(TokenClass.DIV)) {
            nextToken();
            Expr cur = parseC();
            return parseMulDivModRest(new BinOp(pre, Op.DIV, cur));
        } else if (accept(TokenClass.ASTERIX)) {
            nextToken();
            Expr cur = parseC();
            return parseMulDivModRest(new BinOp(pre, Op.MUL, cur));
        } else if (accept(TokenClass.REM)) {
            nextToken();
            Expr cur = parseC();
            return parseMulDivModRest(new BinOp(pre, Op.MOD, cur));
        } else {
            return pre;
        }
    }

    // A      ::=  exp ")" | type ")" exp                 # (exp) & type casting
    private Expr parseA() {
        if (isType()) {
            Type type = parseType(); // TYPE CASE: ( TYPE ) EXP
            expect(TokenClass.RPAR);
            Expr expr = parseExp();
            return new TypecastExpr(type, expr);
        } else { // ( exp )
            Expr expr = parseExp();
            expect(TokenClass.RPAR);
            return expr;
        }
    }

    // B      ::=  "(" [ exp ("," exp)* ] ")" | ε         # IDENT & function call
    private Expr parseB(String ident) {
        if (accept(TokenClass.LPAR)) { // FunCallExpr functioncall
            nextToken(); // consume (
            List<Expr> exprs = new LinkedList<Expr>();
            if (isExp()) {
                exprs.add(parseExp());
            }
            while (accept(TokenClass.COMMA)) {
                nextToken();
                exprs.add(parseExp());
            }
            nextToken(); // consume )
            return new FunCallExpr(ident, exprs);
        } else {
            return new VarExpr(ident);
        }
    }

    // C  ::=  ("(" A | IDENT B | others) '
    private Expr parseC() {
        Expr preExpr;
        if (accept(TokenClass.LPAR)) { // (exp) & type casting
            nextToken(); // consume (
            preExpr = parseA();
        } else if (accept(TokenClass.IDENTIFIER)) { // IDENT & function call
            String ident = token.data;
            nextToken();
            preExpr = parseB(ident);
        } else {
            boolean hi = accept(TokenClass.IDENTIFIER);
            Token t = token;
            preExpr = parseOthers();
        }
        return parseCRest(preExpr);
    }

    // C' ::=  "[" exp "]" C' | "." IDENT C' | ε         # arrayaccess & fieldaccess
    private Expr parseCRest(Expr preExpr) {
        if (accept(TokenClass.LSBR)) { // arrayaccess
            nextToken();
            Expr right = parseExp();
            expect(TokenClass.RSBR);
            Expr curExpr = new ArrayAccessExpr(preExpr, right);
            return parseCRest(curExpr); // left-asscociate
        } else if (accept(TokenClass.DOT)) { // fieldaccess
            nextToken();
            String fieldName = token.data;
            nextToken(); // consume IDENT
            Expr curExpr = new FieldAccessExpr(preExpr, fieldName);
            return parseCRest(curExpr);
        } else { // ε
            return preExpr;
        }
    }

    // others ::=  INT_LITERAL | CHAR_LITERAL | STRING_LITERAL | ("-" | "+") exp | valueat | addressof | sizeof
    private Expr parseOthers() {
        if (accept(TokenClass.INT_LITERAL)) {
            String data = token.data;
            nextToken();
            return new IntLiteral(data);
        } else if (accept(TokenClass.CHAR_LITERAL)) {
            String data = token.data;
            nextToken();
            return new ChrLiteral(data);
        } else if (accept(TokenClass.STRING_LITERAL)) {
            String data = token.data;
            nextToken();
            return new StrLiteral(data);
        } else if (accept(TokenClass.MINUS)) { // unary minus
            nextToken();
            return new BinOp(new IntLiteral(0), Op.SUB, parseC()); // right-associate
        } else if (accept(TokenClass.PLUS)) { // unary plus
            nextToken();
            return parseC();
        } else if (accept(TokenClass.ASTERIX)) {  // VALUE AT
            nextToken();
            return new ValueAtExpr(parseC());
        } else if (accept(TokenClass.AND)) { // ADDRESS OF
            nextToken();
            return new AddressOfExpr(parseC());
        } else {// SIZEOF
            nextToken(); // consume sizeof
            expect(TokenClass.LPAR);
            Type type = parseType();
            expect(TokenClass.RPAR);
            return new SizeOfExpr(type);
        }
    }

    private boolean isExp() {
        return accept(TokenClass.IDENTIFIER, TokenClass.LPAR, TokenClass.INT_LITERAL, TokenClass.CHAR_LITERAL
                , TokenClass.STRING_LITERAL, TokenClass.MINUS, TokenClass.PLUS, TokenClass.ASTERIX, TokenClass.AND,
                TokenClass.SIZEOF);
    }
}
