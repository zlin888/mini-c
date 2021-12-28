package gen.asm;


public interface AssemblyItemVisitor<T> {
    public T visitLabel(AssemblyItem.Label label);
    public T visitDirective(AssemblyItem.Directive directive);
    public T visitInstruction(AssemblyItem.Instruction instruction);
    public T visitComment(AssemblyItem.Comment comment);
}
