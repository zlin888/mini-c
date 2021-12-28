package sem;

public abstract class Symbol {
	public String name;
	
	public Symbol(String name) {
		this.name = name;
	}

	abstract public boolean isVarSymbol();
	abstract public boolean isFunSymbol();
}
