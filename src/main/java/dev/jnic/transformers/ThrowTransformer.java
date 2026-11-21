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
      StringBuilder source = new StringBuilder();
      int stackSizeBefore = stack.sizeBefore();
      int stackSizeBefore2 = stack.sizeBefore();
      Type objectType = JniTypes.OBJECT_TYPE;
      String stackValue = JniTypes.stackVariable("stack", stackSizeBefore2 - 1, objectType);
      if (stack.mayBeNull(stackSizeBefore - 1)) {
         source.append("if (");
         source.append(stackValue);
         source.append(" == NULL) ");
         String[] callArguments = new String[2];
         String classReference = context.getClassCache().classReference("java/lang/NullPointerException");
         callArguments[0] = classReference;
         callArguments[1] = "\"\"";
         String expression = "(void)(" + context.jniCall("ThrowNew", callArguments) + ")";
         source.append(expression);
         source.append(";\n    else ");
         String[] callArguments2 = new String[]{stackValue};
         String expression2 = "(void)(" + context.jniCall("Throw", callArguments2) + ")";
         source.append(expression2);
         source.append(";\n");
         return source.toString();
      } else {
         String[] callArguments3 = new String[]{stackValue};
         String expression3 = "(void)(" + context.jniCall("Throw", callArguments3) + ")";
         source.append(expression3);
         source.append(";\n");
         return source.toString();
      }
   }
}
