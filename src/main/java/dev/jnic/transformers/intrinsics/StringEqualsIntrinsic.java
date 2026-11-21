package dev.jnic.transformers.intrinsics;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import org.objectweb.asm.tree.AbstractInsnNode;

public class StringEqualsIntrinsic extends IntrinsicTransformer {
   public StringEqualsIntrinsic() {
      super(182, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack frame, JumpEmitter labels, String owner) {
      int leftIndex = frame.sizeBefore() - 2;
      int rightIndex = leftIndex + 1;
      String left = "stack" + leftIndex + ".l";
      String right = "stack" + rightIndex + ".l";
      String result = "stack" + leftIndex + ".i";
      StringBuilder code = new StringBuilder();
      if (frame.mayBeNull(leftIndex)) {
         code.append("if (")
            .append(left)
            .append(" == NULL) (void)(")
            .append("(void)(" + context.jniCall("ThrowNew", context.getClassCache().classReference("java/lang/NullPointerException"), "\"\"") + ")")
            .append(");\n    else ");
      }

      if (frame.mayBeNull(rightIndex)) {
         code.append("if (").append(right).append(" == NULL) ").append(result).append(" = 0;\n    else ");
      }

      code.append("if (").append(context.jniCall("IsSameObject", left, right)).append(") ").append(result).append(" = 1;\n    else ");
      if (!"java/lang/String".equals(frame.referenceTypeBefore(rightIndex))) {
         code.append("if (!")
            .append(context.jniCall("IsInstanceOf", right, context.getClassCache().classReference("java/lang/String")))
            .append(") ")
            .append(result)
            .append(" = 0;\n    else ");
      }

      return code.append(stringContents(context, left, right, result)).toString();
   }

   private static String stringContents(CompilationContext context, String left, String right, String result) {
      StringBuilder code = new StringBuilder("{\n        jsize length = ");
      code.append(context.jniCall("GetStringLength", left))
         .append(";\n        if (length != ")
         .append(context.jniCall("GetStringLength", right))
         .append(") ")
         .append(result)
         .append(" = 0;\n        else {\n")
         .append("            const jchar *leftChars = ")
         .append(context.jniCall("GetStringChars", left, "NULL"))
         .append(";\n")
         .append("            const jchar *rightChars = ")
         .append(context.jniCall("GetStringChars", right, "NULL"))
         .append(";\n")
         .append("            jboolean same = leftChars != NULL && rightChars != NULL && memcmp(leftChars, rightChars, (size_t)length * sizeof(jchar)) == 0;\n")
         .append("            if (rightChars != NULL) ")
         .append(context.jniCall("ReleaseStringChars", right, "rightChars"))
         .append(";\n")
         .append("            if (leftChars != NULL) ")
         .append(context.jniCall("ReleaseStringChars", left, "leftChars"))
         .append(";\n")
         .append("            ")
         .append(result)
         .append(" = same;\n")
         .append("        }\n    }\n");
      return code.toString();
   }
}
