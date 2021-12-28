package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author cdubach
 */
public class Tokeniser {

    private Scanner scanner;

    private int error = 0;

    public int getErrorCount() {
        return this.error;
    }

    public Tokeniser(Scanner scanner) {
        this.scanner = scanner;
    }

    private void error(char c, int line, int col) {
        System.out.println("Lexing error: unrecognised character (" + c + ") at " + line + ":" + col);
        error++;
    }


    public Token nextToken() {
        Token result;
        try {
            result = next();
        } catch (EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // something went horribly wrong, abort
            System.exit(-1);
            return null;
        }
        return result;
    }

    /*
     * To be completed
     */
    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        // COMMENTS (need to be put in front of DIV and ASTERIX 
        // single line comment
        if (c == '/' && scanner.peek() == '/') {
            scanner.next(); // consume '/'
            while(scanner.peek() != '\n' &&
                  scanner.peek() != '\r') {
                scanner.next(); // consume '\n'|'\r'
            }   
            // handle newline as "\r\n"
            if (scanner.peek() == '\n') scanner.next();
            return next();
        }
        // multiple line comment
        if (c == '/' && scanner.peek() == '*') {
            scanner.next(); // consume '*'
            while(true) {
                c = scanner.next();
                if(c == '*' && scanner.peek() == '/') {
                    scanner.next(); // consume '/'
                    break;
                }
            }
            return next();
        }

        // delimiters
        if (c == '{') return new Token(TokenClass.LBRA, line, column);
        else if (c == '}') return new Token(TokenClass.RBRA, line, column);
        else if (c == '(') return new Token(TokenClass.LPAR, line, column);
        else if (c == ')') return new Token(TokenClass.RPAR, line, column);
        else if (c == '[') return new Token(TokenClass.LSBR, line, column);
        else if (c == ']') return new Token(TokenClass.RSBR, line, column);
        else if (c == ';') return new Token(TokenClass.SC, line, column);
        else if (c == ',') return new Token(TokenClass.COMMA, line, column);

        // logical operators
        if (c == '&' && scanner.peek() == '&') {
            scanner.next();
            return new Token(TokenClass.LOGAND, line, column);
        } else if (c == '|' && scanner.peek() == '|') {
            scanner.next();
            return new Token(TokenClass.LOGOR, line, column);
        }

        // comparisons
        if (c == '=' && scanner.peek() == '=') {
            scanner.next();
            return new Token(TokenClass.EQ, line, column);
        } else if (c == '!' && scanner.peek() == '=') {
            scanner.next();
            return new Token(TokenClass.NE, line, column);
        } else if (c == '<' && scanner.peek() == '=') {
            scanner.next();
            return new Token(TokenClass.LE, line, column);
        } else if (c == '>' && scanner.peek() == '=') {
            scanner.next();
            return new Token(TokenClass.GE, line, column);
        } else if (c == '>') {
            return new Token(TokenClass.GT, line, column);
        } else if (c == '<') {
            return new Token(TokenClass.LT, line, column);
        }

        // assign
        if (c == '=') {
            return new Token(TokenClass.ASSIGN, line, column);
        }


        // operators
        if (c == '+') return new Token(TokenClass.PLUS, line, column);
        else if (c == '-') return new Token(TokenClass.MINUS, line, column);
        else if (c == '*') return new Token(TokenClass.ASTERIX, line, column);
        else if (c == '/') return new Token(TokenClass.DIV, line, column);
        else if (c == '%') return new Token(TokenClass.REM, line, column);
        else if (c == '&') return new Token(TokenClass.AND, line, column); // need to placed behind the LOGAND

        // struct member access
        if (c == '.')
            return new Token(TokenClass.DOT, line, column);

        // literals
        if (Character.isDigit(c)) { // INT_LITERAL
            StringBuilder sb = new StringBuilder().append(c);
            while (Character.isDigit(scanner.peek())) {
                sb.append(scanner.next());
            }
            return new Token(TokenClass.INT_LITERAL, sb.toString(), line, column);
        } else if (c == '\'') { // CHAR
            StringBuilder sb = new StringBuilder();
            if (scanner.peek() == '\\') { // check escape
                scanner.next(); // move, current will be on '\\'
                char peek = scanner.peek();
                if (peek == 't' || peek == 'b' ||peek == 'n' ||peek == 'r' ||peek == 'f') {
                    sb.append("\\").append(scanner.next()); // backslash should be added
                } else if (peek == '\'' || peek == '\"' || peek == '\\') {
                    sb.append(scanner.next());
                } else {
                    error(c, line, column);
                }
            } else { // no escape
                sb.append(scanner.next());
            }
            if (scanner.next() != '\'') {
//                throw new AssertionError("a char should be enclose by two '");
                error(c, line, column);
            }
            return new Token(TokenClass.CHAR_LITERAL, sb.toString(), line, column);
        } else if (c == '\"') { // STRING
            c = scanner.next(); // consume '\"'
            StringBuilder sb = new StringBuilder();
            while (c != '\"') {
                if (c == '\\') { // check escape
                    char peek = scanner.peek();
                    if (peek == 't' || peek == 'b' ||peek == 'n' ||peek == 'r' ||peek == 'f') {
                        sb.append("\\").append(scanner.next());
                    } else if (peek == '\'' || peek == '\"' || peek == '\\') {
                        sb.append(scanner.next()); // backslash should not be added
                    } else {
                        error(c, line, column);
                    }
                } else {
                    sb.append(c);
                }
                c = scanner.next();
            }
            return new Token(TokenClass.STRING_LITERAL, sb.toString(), line, column);
        }

        // IDENTIFIER & TYPES $ KEYWORDS
        if (Character.isLetter(c) || c == '_') {
            StringBuilder sb = new StringBuilder().append(c);
            while (Character.isLetterOrDigit(scanner.peek()) || scanner.peek() == '_') {
                sb.append(scanner.next());
            }
            // TYPES
            if (sb.toString().equals("int"))
                return new Token(TokenClass.INT, sb.toString(), line, column);
            else if (sb.toString().equals("void"))
                return new Token(TokenClass.VOID, sb.toString(), line, column);
            else if (sb.toString().equals("char"))
                return new Token(TokenClass.CHAR, sb.toString(), line, column);
            //KEYWORDS
            if (sb.toString().equals("if"))
                return new Token(TokenClass.IF, sb.toString(), line, column);
            else if (sb.toString().equals("else"))
                return new Token(TokenClass.ELSE, sb.toString(), line, column);
            else if (sb.toString().equals("while"))
                return new Token(TokenClass.WHILE, sb.toString(), line, column);
            else if (sb.toString().equals("return"))
                return new Token(TokenClass.RETURN, sb.toString(), line, column);
            else if (sb.toString().equals("struct"))
                return new Token(TokenClass.STRUCT, sb.toString(), line, column);
            else if (sb.toString().equals("sizeof"))
                return new Token(TokenClass.SIZEOF, sb.toString(), line, column);

            // if this is neither a type nor keyword, it is an identifier
            return new Token(TokenClass.IDENTIFIER, sb.toString(), line, column);
        }

        // #INCLUDE
        if (c == '#') {
            String s = "include";
            for (int i = 0; i < s.length(); i++) {
                if (scanner.next() != s.charAt(i)) error(c, line, column);
            }
            return new Token(TokenClass.INCLUDE, line, column);
        }

        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }

//    private Token tokeniseStringLiteral(char c) {
//        assert c == '"';
//        c = scanner.next(); // consume '"'
//        if (c == '\\') {
//            char peek = scanner.peek();
//            if (peek == 't' || peek == 'b' ||peek == 'n' ||peek == 'r' ||peek == 'f') {
//                sb.append("\\").append(scanner.next());
//            } else if (peek == '\'' || peek == '\"' || peek == '\\') {
//                sb.append(scanner.next()); // backslash should not be added
//            } else {
//                error(c, line, column);
//            }
//        }
//        scanner.next();
//    }
}
