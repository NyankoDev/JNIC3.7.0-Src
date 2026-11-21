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
      StringBuilder var7 = new StringBuilder();
      if (instruction.getOpcode() == 177) {
         var7.append("return;\n");
         return var7.toString();
      } else {
         int var9 = stack.sizeBefore();
         var7.append("return ");
         Type var25 = stack.typeBefore(var9 - 1);
         String value26 = JniTypes.stackVariable("stack", var9 - 1, var25);
         var7.append(value26);
         var7.append(";\n");
         return var7.toString();
      }
   }
}
