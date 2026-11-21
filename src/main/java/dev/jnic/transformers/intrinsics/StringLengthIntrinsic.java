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
      int stackSizeBefore = stack.sizeBefore();
      Type objectType = JniTypes.OBJECT_TYPE;
      String stackValue = JniTypes.stackVariable("stack", stackSizeBefore - 1, objectType);
      Type intType = Type.INT_TYPE;
      String stackValue2 = JniTypes.stackVariable("stack", stackSizeBefore - 1, intType);
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
         builder.append(stackValue2);
         builder.append(" = ");
         String[] callArguments2 = new String[]{stackValue};
         String getStringLengthCall = context.jniCall("GetStringLength", callArguments2);
         builder.append(getStringLengthCall);
         builder.append(";\n");
         return builder.toString();
      } else {
         builder.append(stackValue2);
         builder.append(" = ");
         String[] callArguments3 = new String[]{stackValue};
         String getStringLengthCall2 = context.jniCall("GetStringLength", callArguments3);
         builder.append(getStringLengthCall2);
         builder.append(";\n");
         return builder.toString();
      }
   }
}
