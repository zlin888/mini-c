package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import regalloc.SmartRegAlloc;
import sem.TypeCheckVisitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class CodeGenerator {


    public void emitProgram(Program astProgram, File outputFile) throws FileNotFoundException {
        // DPS for return struct
        astProgram.accept(new ReturnStructVisitor());
        astProgram.accept(new AllocationLocationVisitor());
        astProgram.accept(new TypeCheckVisitor());

        // generate an assembly program with the code generator
        AssemblyProgram asmProgWithVirtualRegs = new AssemblyProgram();


        ProgramGen progGen = new ProgramGen(asmProgWithVirtualRegs); // global variable declaration
        progGen.visitProgram(astProgram);

        // run liveness analysis
        List<CFGNode> entrys = new LivenessAnalyzer(asmProgWithVirtualRegs).run();
        // Inf Graph
        List<InterferenceGraph> infGraphs = new LinkedList<>();
        for (CFGNode entry : entrys) {
            infGraphs.add(new InterferenceGraph(entry));
        }

        // run the register naive allocator which remove the virtual registers
//        AssemblyProgram asmProgNoVirtualRegs = NaiveRegAlloc.run(asmProgWithVirtualRegs);
        AssemblyProgram asmProgNoVirtualRegs = SmartRegAlloc.run(asmProgWithVirtualRegs, infGraphs);

        // print the assembly program
        PrintWriter writer = new PrintWriter(outputFile);
        asmProgNoVirtualRegs.print(writer);
//        asmProgWithVirtualRegs.print(writer);
        writer.close();
    }


}
