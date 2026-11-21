package dev.jnic.transformers.intrinsics;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

public class StringLengthIntrinsic extends IntrinsicTransformer {
   public StringLengthIntrinsic() {
      super(182, "java/lang/String", "length", "()I");
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      int var6 = stack.sizeBefore();
      Type var14 = JniTypes.OBJECT_TYPE;
      String value15 = JniTypes.stackVariable("stack", var6 - 1, var14);
      Type var23 = Type.INT_TYPE;
      String value24 = JniTypes.stackVariable("stack", var6 - 1, var23);
      StringBuilder var26 = new StringBuilder();
      if (stack.mayBeNull(var6 - 1)) {
         var26.append("if (");
         var26.append(value15);
         var26.append(" == NULL) ");
         String[] var52 = new String[2];
         String value61 = context.getClassCache().classReference("java/lang/NullPointerException");
         var52[0] = value61;
         var52[1] = "\"\"";
         String var69 = "(void)(" + context.jniCall("ThrowNew", var52) + ")";
         var26.append(var69);
         var26.append(";\n    else ");
         var26.append(value24);
         var26.append(" = ");
         String[] var103 = new String[]{value15};
         String value104 = context.jniCall("GetStringLength", var103);
         var26.append(value104);
         var26.append(";\n");
         return var26.toString();
      } else {
         var26.append(value24);
         var26.append(" = ");
         String[] var44 = new String[]{value15};
         String value45 = context.jniCall("GetStringLength", var44);
         var26.append(value45);
         var26.append(";\n");
         return var26.toString();
      }
   }
}
