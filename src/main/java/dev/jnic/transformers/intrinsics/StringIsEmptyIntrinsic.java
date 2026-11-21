package dev.jnic.transformers.intrinsics;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

public class StringIsEmptyIntrinsic extends IntrinsicTransformer {
   public StringIsEmptyIntrinsic() {
      super(182, "java/lang/String", "isEmpty", "()Z");
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      int var7 = stack.sizeBefore();
      Type var15 = JniTypes.OBJECT_TYPE;
      String value16 = JniTypes.stackVariable("stack", var7 - 1, var15);
      Type var24 = Type.BOOLEAN_TYPE;
      String value25 = JniTypes.stackVariable("stack", var7 - 1, var24);
      StringBuilder var26 = new StringBuilder();
      if (stack.mayBeNull(var7 - 1)) {
         var26.append("if (");
         var26.append(value16);
         var26.append(" == NULL) ");
         String[] var52 = new String[2];
         String value61 = context.getClassCache().classReference("java/lang/NullPointerException");
         var52[0] = value61;
         var52[1] = "\"\"";
         String var69 = "(void)(" + context.jniCall("ThrowNew", var52) + ")";
         var26.append(var69);
         var26.append(";\n    else ");
         var26.append(value25);
         var26.append(" = ");
         String[] var103 = new String[]{value16};
         String value104 = context.jniCall("GetStringLength", var103);
         var26.append(value104);
         var26.append(" == 0;\n");
         return var26.toString();
      } else {
         var26.append(value25);
         var26.append(" = ");
         String[] var44 = new String[]{value16};
         String value45 = context.jniCall("GetStringLength", var44);
         var26.append(value45);
         var26.append(" == 0;\n");
         return var26.toString();
      }
   }
}
