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
      StringBuilder var7 = new StringBuilder();
      if (instruction.getOpcode() == 194) {
         String[] var16 = new String[1];
         int var24 = stack.sizeBefore();
         Type var25 = JniTypes.OBJECT_TYPE;
         String value26 = JniTypes.stackVariable("stack", var24 - 1, var25);
         var16[0] = value26;
         String value27 = context.jniCall("MonitorEnter", var16);
         var7.append(value27);
         var7.append(";\n");
         return var7.toString();
      } else {
         String[] var28 = new String[1];
         int var29 = stack.sizeBefore();
         Type var30 = JniTypes.OBJECT_TYPE;
         String value31 = JniTypes.stackVariable("stack", var29 - 1, var30);
         var28[0] = value31;
         String value32 = context.jniCall("MonitorExit", var28);
         var7.append(value32);
         var7.append(";\n");
         return var7.toString();
      }
   }
}
