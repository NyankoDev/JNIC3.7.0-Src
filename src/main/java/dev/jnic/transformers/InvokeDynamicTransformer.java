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
      InvokeDynamicInsnNode var9 = (InvokeDynamicInsnNode)instruction;
      StringBuilder var6 = new StringBuilder("{\n    static jobject indy;\n    if (indy == NULL) {\n");
      if (JnicLogger.TRACE_ENABLED) {
         var6.append("puts(\"[JNIC: indy init]\");\n");
      }

      String[] var10 = new String[var9.bsmArgs.length + 3];
      var10[0] = context.getClassCache().methodHandleLookup(ownerName);
      var10[1] = context.constant(ownerName, var9.name);
      var10[2] = context.constant(ownerName, Type.getType(var9.desc));
      boolean var11 = isDirectBootstrap(context, var9);

      for (int var12 = 0; var12 < var9.bsmArgs.length; var12++) {
         Object var7 = var9.bsmArgs[var12];
         String value8 = context.constant(ownerName, var7);
         var10[var12 + 3] = var11 ? value8 : TypeBoxing.box(context, value8, TypeBoxing.constantType(var7));
      }

      String value13;
      if (var11) {
         value13 = context.invokeStatic(var9.bsm.getOwner(), var9.bsm.getName(), var9.bsm.getDesc(), var10);
      } else {
         var6.append("        jobject args = ")
            .append(context.jniCall("NewObjectArray", Integer.toString(var10.length), context.getClassCache().classReference("java/lang/Object"), "NULL"))
            .append(";\n");

         for (int var14 = 0; var14 < var10.length; var14++) {
            var6.append("        ").append(context.jniCall("SetObjectArrayElement", "args", Integer.toString(var14), var10[var14])).append(";\n");
         }

         value13 = context.invokeVirtual(
            "java/lang/invoke/MethodHandle", "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;", context.constant(ownerName, var9.bsm), "args"
         );
      }

      var6.append("        jobject callSite = ").append(value13).append(";\n");
      var6.append("        indy = ").append(context.jniCall("NewGlobalRef", "callSite")).append(";\n");
      var6.append("        ").append(context.jniCall("DeleteLocalRef", "callSite")).append(";\n    }\n");
      if (JnicLogger.TRACE_ENABLED) {
         var6.append("puts(\"[JNIC: indy invoke]\");\n");
      }

      var6.append("    temp0.l = ")
         .append(context.invokeVirtual("java/lang/invoke/CallSite", "getTarget", "()Ljava/lang/invoke/MethodHandle;", "indy"))
         .append(";\n");
      Type[] var16 = Type.getArgumentTypes(var9.desc);
      Type var17 = Type.getReturnType(var9.desc);
      if (var16.length == 0) {
         var6.append("    temp1.l = NULL;\n");
      } else {
         var6.append("    temp1.l = ")
            .append(context.jniCall("NewObjectArray", Integer.toString(var16.length), context.getClassCache().classReference("java/lang/Object"), "NULL"))
            .append(";\n");
      }

      for (int var15 = 0; var15 < var16.length; var15++) {
         String value19 = dynamicStack(stack.sizeBefore() - var16.length + var15, var16[var15]);
         var6.append("    ")
            .append(context.jniCall("SetObjectArrayElement", "temp1.l", Integer.toString(var15), TypeBoxing.box(context, value19, var16[var15])))
            .append(";\n");
      }

      String value18 = context.invokeVirtual(
         "java/lang/invoke/MethodHandle", "invokeWithArguments", "([Ljava/lang/Object;)Ljava/lang/Object;", "temp0.l", "temp1.l"
      );
      if (var17.getSort() == 0) {
         var6.append("    ").append(value18).append(";\n");
      } else {
         String var20 = "stack" + (stack.sizeAfter() - 1) + ".l";
         var6.append("    ").append(var20).append(" = ").append(value18).append(";\n");
         var6.append("    ").append(dynamicStack(stack.sizeAfter() - 1, var17)).append(" = ").append(TypeBoxing.unbox(context, var20, var17)).append(";\n");
      }

      return var6.append("}\n").toString();
   }

   private static boolean isDirectBootstrap(CompilationContext context, InvokeDynamicInsnNode invokeDynamicInsnNode) {
      Type[] var2 = Type.getArgumentTypes(invokeDynamicInsnNode.bsm.getDesc());
      if (var2.length != invokeDynamicInsnNode.bsmArgs.length + 3) {
         return false;
      }

      for (int var3 = 0; var3 < 3; var3++) {
         if (var2[var3].getSort() != 10) {
            return false;
         }
      }

      for (int var4 = 0; var4 < invokeDynamicInsnNode.bsmArgs.length; var4++) {
         if (var2[var4 + 3].getSort() != TypeBoxing.constantType(invokeDynamicInsnNode.bsmArgs[var4]).getSort()) {
            return false;
         }
      }

      return true;
   }

   private static String dynamicStack(int index, Type type) {
      byte var10000;
      switch (type.getSort()) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
            var10000 = 105;
            break;
         case 6:
            var10000 = 102;
            break;
         case 7:
            var10000 = 106;
            break;
         case 8:
            var10000 = 100;
            break;
         case 9:
         case 10:
            var10000 = 108;
            break;
         default:
            throw new IllegalArgumentException("Invalid stack type: " + type);
      }

      char var2 = (char)var10000;
      return "stack" + index + "." + var2;
   }
}
