package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class TypeCheckTransformer extends OpcodeTransformer {
   public TypeCheckTransformer() {
      super(true, 192, 193);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      StringBuilder source = new StringBuilder();
      TypeInsnNode typeInstruction = (TypeInsnNode)instruction;
      int stackSizeBefore = stack.sizeBefore();
      if (typeInstruction.getOpcode() == 192) {
         source.append("if (!");
         String[] callArguments = new String[2];
         Type objectType = JniTypes.OBJECT_TYPE;
         String stackValue = JniTypes.stackVariable("stack", stackSizeBefore - 1, objectType);
         callArguments[0] = stackValue;
         String classReference = context.getClassCache().classReference(typeInstruction.desc);
         callArguments[1] = classReference;
         String isInstanceOfCall = context.jniCall("IsInstanceOf", callArguments);
         source.append(isInstanceOfCall);
         source.append(") ");
         String[] callArguments2 = new String[2];
         String classReference2 = context.getClassCache().classReference("java/lang/ClassCastException");
         callArguments2[0] = classReference2;
         callArguments2[1] = "\"\"";
         String expression = "(void)(" + context.jniCall("ThrowNew", callArguments2) + ")";
         source.append(expression);
         source.append(";\n");
         return source.toString();
      } else {
         Type intType = Type.INT_TYPE;
         String stackValue2 = JniTypes.stackVariable("stack", stackSizeBefore - 1, intType);
         source.append(stackValue2);
         source.append(" = ");
         Type objectType2 = JniTypes.OBJECT_TYPE;
         String stackValue3 = JniTypes.stackVariable("stack", stackSizeBefore - 1, objectType2);
         source.append(stackValue3);
         source.append(" != NULL && ");
         String[] callArguments3 = new String[2];
         Type objectType3 = JniTypes.OBJECT_TYPE;
         String stackValue4 = JniTypes.stackVariable("stack", stackSizeBefore - 1, objectType3);
         callArguments3[0] = stackValue4;
         String classReference3 = context.getClassCache().classReference(typeInstruction.desc);
         callArguments3[1] = classReference3;
         String isInstanceOfCall2 = context.jniCall("IsInstanceOf", callArguments3);
         source.append(isInstanceOfCall2);
         source.append(";\n");
         return source.toString();
      }
   }
}
