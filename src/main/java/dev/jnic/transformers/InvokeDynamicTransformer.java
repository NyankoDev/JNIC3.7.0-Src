package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.codegen.TypeBoxing;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.logging.JnicLogger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

public class InvokeDynamicTransformer extends OpcodeTransformer {
   public InvokeDynamicTransformer() {
      super(true, 186);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      InvokeDynamicInsnNode dynamicCall = (InvokeDynamicInsnNode)instruction;
      StringBuilder source = new StringBuilder("{\n    static jobject indy;\n    if (indy == NULL) {\n");
      if (JnicLogger.TRACE_ENABLED) {
         source.append("puts(\"[JNIC: indy init]\");\n");
      }

      String[] callArguments = new String[dynamicCall.bsmArgs.length + 3];
      callArguments[0] = context.getClassCache().methodHandleLookup(ownerName);
      callArguments[1] = context.constant(ownerName, dynamicCall.name);
      callArguments[2] = context.constant(ownerName, Type.getType(dynamicCall.desc));
      boolean isDirectBootstrap = isDirectBootstrap(context, dynamicCall);

      for (int index = 0; index < dynamicCall.bsmArgs.length; index++) {
         Object bootstrapArgument = dynamicCall.bsmArgs[index];
         String constantExpression = context.constant(ownerName, bootstrapArgument);
         callArguments[index + 3] = isDirectBootstrap ? constantExpression : TypeBoxing.box(context, constantExpression, TypeBoxing.constantType(bootstrapArgument));
      }

      String staticCall;
      if (isDirectBootstrap) {
         staticCall = context.invokeStatic(dynamicCall.bsm.getOwner(), dynamicCall.bsm.getName(), dynamicCall.bsm.getDesc(), callArguments);
      } else {
         source.append("        jobject args = ")
            .append(context.jniCall("NewObjectArray", Integer.toString(callArguments.length), context.getClassCache().classReference("java/lang/Object"), "NULL"))
            .append(";\n");

         for (int index2 = 0; index2 < callArguments.length; index2++) {
            source.append("        ").append(context.jniCall("SetObjectArrayElement", "args", Integer.toString(index2), callArguments[index2])).append(";\n");
         }

         staticCall = context.invokeVirtual(
            "java/lang/invoke/MethodHandle", "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;", context.constant(ownerName, dynamicCall.bsm), "args"
         );
      }

      source.append("        jobject callSite = ").append(staticCall).append(";\n");
      source.append("        indy = ").append(context.jniCall("NewGlobalRef", "callSite")).append(";\n");
      source.append("        ").append(context.jniCall("DeleteLocalRef", "callSite")).append(";\n    }\n");
      if (JnicLogger.TRACE_ENABLED) {
         source.append("puts(\"[JNIC: indy invoke]\");\n");
      }

      source.append("    temp0.l = ")
         .append(context.invokeVirtual("java/lang/invoke/CallSite", "getTarget", "()Ljava/lang/invoke/MethodHandle;", "indy"))
         .append(";\n");
      Type[] argumentTypes = Type.getArgumentTypes(dynamicCall.desc);
      Type returnType = Type.getReturnType(dynamicCall.desc);
      if (argumentTypes.length == 0) {
         source.append("    temp1.l = NULL;\n");
      } else {
         source.append("    temp1.l = ")
            .append(context.jniCall("NewObjectArray", Integer.toString(argumentTypes.length), context.getClassCache().classReference("java/lang/Object"), "NULL"))
            .append(";\n");
      }

      for (int index3 = 0; index3 < argumentTypes.length; index3++) {
         String argumentExpression = dynamicStack(stack.sizeBefore() - argumentTypes.length + index3, argumentTypes[index3]);
         source.append("    ")
            .append(context.jniCall("SetObjectArrayElement", "temp1.l", Integer.toString(index3), TypeBoxing.box(context, argumentExpression, argumentTypes[index3])))
            .append(";\n");
      }

      String virtualCall = context.invokeVirtual(
         "java/lang/invoke/MethodHandle", "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;", "temp0.l", "temp1.l"
      );
      if (returnType.getSort() == 0) {
         source.append("    ").append(virtualCall).append(";\n");
      } else {
         String expression = "stack" + (stack.sizeAfter() - 1) + ".l";
         source.append("    ").append(expression).append(" = ").append(virtualCall).append(";\n");
         source.append("    ").append(dynamicStack(stack.sizeAfter() - 1, returnType)).append(" = ").append(TypeBoxing.unbox(context, expression, returnType)).append(";\n");
      }

      return source.append("}\n").toString();
   }

   private static boolean isDirectBootstrap(CompilationContext context, InvokeDynamicInsnNode invokeDynamicInsnNode) {
      Type[] argumentTypes = Type.getArgumentTypes(invokeDynamicInsnNode.bsm.getDesc());
      if (argumentTypes.length != invokeDynamicInsnNode.bsmArgs.length + 3) {
         return false;
      }

      for (int index = 0; index < 3; index++) {
         if (argumentTypes[index].getSort() != 10) {
            return false;
         }
      }

      for (int index2 = 0; index2 < invokeDynamicInsnNode.bsmArgs.length; index2++) {
         if (argumentTypes[index2 + 3].getSort() != TypeBoxing.constantType(invokeDynamicInsnNode.bsmArgs[index2]).getSort()) {
            return false;
         }
      }

      return true;
   }

   private static String dynamicStack(int index, Type type) {
      byte byteValue;
      switch (type.getSort()) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
            byteValue = 105;
            break;
         case 6:
            byteValue = 102;
            break;
         case 7:
            byteValue = 106;
            break;
         case 8:
            byteValue = 100;
            break;
         case 9:
         case 10:
            byteValue = 108;
            break;
         default:
            throw new IllegalArgumentException("Invalid stack type: " + type);
      }

      char byteValue2 = (char)byteValue;
      return "stack" + index + "." + byteValue2;
   }
}
