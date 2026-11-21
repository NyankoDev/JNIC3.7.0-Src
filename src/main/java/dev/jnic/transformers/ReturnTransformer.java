package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

public class ReturnTransformer extends OpcodeTransformer {
   public ReturnTransformer() {
      super(false, 172, 173, 174, 175, 176, 177);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      StringBuilder source = new StringBuilder();
      if (instruction.getOpcode() == 177) {
         source.append("return;\n");
         return source.toString();
      } else {
         int stackSizeBefore = stack.sizeBefore();
         source.append("return ");
         Type operandType = stack.typeBefore(stackSizeBefore - 1);
         String stackValue = JniTypes.stackVariable("stack", stackSizeBefore - 1, operandType);
         source.append(stackValue);
         source.append(";\n");
         return source.toString();
      }
   }
}
