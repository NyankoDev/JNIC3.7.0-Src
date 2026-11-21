package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

public class MonitorTransformer extends OpcodeTransformer {
   public MonitorTransformer() {
      super(true, 194, 195);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      StringBuilder source = new StringBuilder();
      if (instruction.getOpcode() == 194) {
         String[] callArguments = new String[1];
         int stackSizeBefore = stack.sizeBefore();
         Type objectType = JniTypes.OBJECT_TYPE;
         String stackValue = JniTypes.stackVariable("stack", stackSizeBefore - 1, objectType);
         callArguments[0] = stackValue;
         String monitorEnterCall = context.jniCall("MonitorEnter", callArguments);
         source.append(monitorEnterCall);
         source.append(";\n");
         return source.toString();
      } else {
         String[] callArguments2 = new String[1];
         int stackSizeBefore2 = stack.sizeBefore();
         Type objectType2 = JniTypes.OBJECT_TYPE;
         String stackValue2 = JniTypes.stackVariable("stack", stackSizeBefore2 - 1, objectType2);
         callArguments2[0] = stackValue2;
         String monitorExitCall = context.jniCall("MonitorExit", callArguments2);
         source.append(monitorExitCall);
         source.append(";\n");
         return source.toString();
      }
   }
}
