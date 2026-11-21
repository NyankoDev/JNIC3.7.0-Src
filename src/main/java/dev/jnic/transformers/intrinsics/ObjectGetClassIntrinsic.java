package dev.jnic.transformers.intrinsics;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

public class ObjectGetClassIntrinsic extends IntrinsicTransformer {
   public ObjectGetClassIntrinsic() {
      super(182, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      int var6 = stack.sizeBefore();
      Type var14 = JniTypes.OBJECT_TYPE;
      String value15 = JniTypes.stackVariable("stack", var6 - 1, var14);
      StringBuilder var17 = new StringBuilder();
      if (stack.mayBeNull(var6 - 1)) {
         var17.append("if (");
         var17.append(value15);
         var17.append(" == NULL) ");
         String[] var43 = new String[2];
         String value52 = context.getClassCache().classReference("java/lang/NullPointerException");
         var43[0] = value52;
         var43[1] = "\"\"";
         String var60 = "(void)(" + context.jniCall("ThrowNew", var43) + ")";
         var17.append(var60);
         var17.append(";\n    else ");
         var17.append(value15);
         var17.append(" = ");
         String[] var94 = new String[]{value15};
         String value95 = context.jniCall("GetObjectClass", var94);
         var17.append(value95);
         var17.append(";\n");
         return var17.toString();
      } else {
         var17.append(value15);
         var17.append(" = ");
         String[] var35 = new String[]{value15};
         String value36 = context.jniCall("GetObjectClass", var35);
         var17.append(value36);
         var17.append(";\n");
         return var17.toString();
      }
   }
}
