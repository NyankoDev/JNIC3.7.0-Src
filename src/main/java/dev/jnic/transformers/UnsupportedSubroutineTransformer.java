package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.compiler.CompilationException;
import org.objectweb.asm.tree.AbstractInsnNode;

public class UnsupportedSubroutineTransformer extends OpcodeTransformer {
   private static final String UNSUPPORTED_MESSAGE = "Unsupported JSR/RET instruction";

   public UnsupportedSubroutineTransformer() {
      super(false, 168, 169);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      String var6 = UNSUPPORTED_MESSAGE;
      throw new CompilationException(var6);
   }
}
