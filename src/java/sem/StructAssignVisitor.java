package sem;

import ast.*;

import java.util.LinkedList;
import java.util.List;

// Use it after type analysis
public class StructAssignVisitor { //extends BaseSemanticVisitor<List<Stmt>> {
//    @Override
//    public List<Stmt> visitAssign(Assign as) {
//        if (as.left.type instanceof StructType && as.right.type instanceof StructType) {
//            StructTypeDecl std = ((StructType) as.left.type).std;
//            List<Assign> assigns = new LinkedList<>();
//            for (VarDecl varDecl : std.varDecls) {
//                assigns.add(new Assign(
//                        new FieldAccessExpr(as.left, varDecl.varName),
//                        new FieldAccessExpr(as.right, varDecl.varName)
//                ));
//            }
//        }
//        return null;
//    }
}
