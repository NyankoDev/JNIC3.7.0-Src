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
      int stackSizeBefore = stack.sizeBefore();
      Type objectType = JniTypes.OBJECT_TYPE;
      String stackValue = JniTypes.stackVariable("stack", stackSizeBefore - 1, objectType);
      StringBuilder builder = new StringBuilder();
      if (stack.mayBeNull(stackSizeBefore - 1)) {
         builder.append("if (");
         builder.append(stackValue);
         builder.append(" == NULL) ");
         String[] callArguments = new String[2];
         String classReference = context.getClassCache().classReference("java/lang/NullPointerException");
         callArguments[0] = classReference;
         callArguments[1] = "\"\"";
         String expression = "(void)(" + context.jniCall("ThrowNew", callArguments) + ")";
         builder.append(expression);
         builder.append(";\n    else ");
         builder.append(stackValue);
         builder.append(" = ");
         String[] callArguments2 = new String[]{stackValue};
         String getObjectClassCall = context.jniCall("GetObjectClass", callArguments2);
         builder.append(getObjectClassCall);
         builder.append(";\n");
         return builder.toString();
      } else {
         builder.append(stackValue);
         builder.append(" = ");
         String[] callArguments3 = new String[]{stackValue};
         String getObjectClassCall2 = context.jniCall("GetObjectClass", callArguments3);
         builder.append(getObjectClassCall2);
         builder.append(";\n");
         return builder.toString();
      }
   }
}
