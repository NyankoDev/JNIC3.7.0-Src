package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

public class ThrowTransformer extends OpcodeTransformer {
   public ThrowTransformer() {
      super(true, 191);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      StringBuilder var7 = new StringBuilder();
      int var8 = stack.sizeBefore();
      int var16 = stack.sizeBefore();
      Type var17 = JniTypes.OBJECT_TYPE;
      String value18 = JniTypes.stackVariable("stack", var16 - 1, var17);
      if (stack.mayBeNull(var8 - 1)) {
         var7.append("if (");
         var7.append(value18);
         var7.append(" == NULL) ");
         String[] var44 = new String[2];
         String value53 = context.getClassCache().classReference("java/lang/NullPointerException");
         var44[0] = value53;
         var44[1] = "\"\"";
         String var61 = "(void)(" + context.jniCall("ThrowNew", var44) + ")";
         var7.append(var61);
         var7.append(";\n    else ");
         String[] var86 = new String[]{value18};
         String var87 = "(void)(" + context.jniCall("Throw", var86) + ")";
         var7.append(var87);
         var7.append(";\n");
         return var7.toString();
      } else {
         String[] var27 = new String[]{value18};
         String var28 = "(void)(" + context.jniCall("Throw", var27) + ")";
         var7.append(var28);
         var7.append(";\n");
         return var7.toString();
      }
   }
}
