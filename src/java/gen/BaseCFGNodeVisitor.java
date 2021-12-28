package gen;

import java.util.HashSet;
import java.util.Set;

abstract class BaseCFGNodeVisitor<T> {
    //    T visitNode(CFGNode node);
    public Set<CFGNode> visited = new HashSet();

    abstract public T job(CFGNode node);

    public T visitNode(CFGNode node) {
        if (!visited.contains(node)) {
            visited.add(node);
            return job(node);
        } else {
            return null;
        }
    }
}
