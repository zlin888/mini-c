package sem;

import java.util.HashMap;
import java.util.Map;

public class Scope {
	private final Scope outer;
	private final Map<String, Symbol> symbolTable;
	
	public Scope(Scope outer) {
		this(outer, new HashMap<>());
	}

	public Scope(Scope outer, Map<String, Symbol> symbolTable) {
		this.outer = outer;
		this.symbolTable = symbolTable;
	}
	
	public Scope() { this(null); }
	
	public Symbol lookup(String name) {
	    Symbol s = lookupCurrent(name);
	    if (s != null) { // in current
	    	return s;
		} else {
	        if (outer != null) {
	        	return outer.lookup(name); // in outer or not exist
			} else {
	        	return null; // not exist
			}
		}
	}
	
	public Symbol lookupCurrent(String name) {
	    return symbolTable.get(name);
	}
	
	public void put(Symbol sym) {
		symbolTable.put(sym.name, sym);
	}
}
