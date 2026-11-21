package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

public class InvokeTransformer extends OpcodeTransformer {
   public InvokeTransformer() {
      super(true, 182, 183, 184, 185);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      MethodInsnNode var6 = (MethodInsnNode)instruction;
      int var7 = var6.getOpcode();
      Type[] var10 = Type.getArgumentTypes(var6.desc);
      Type var11 = Type.getReturnType(var6.desc);
      String[] var8 = new String[var10.length];

      for (int var9 = 0; var9 < var8.length; var9++) {
         var8[var9] = invocationVariable(stack.sizeBefore() - var8.length + var9, var10[var9]);
      }

      String value12 = var7 == 184 ? null : invocationVariable(stack.sizeBefore() - var8.length - 1, Type.getType(Object.class));
      String var14 = var11.getSort() == 0 ? "" : invocationVariable(stack.sizeAfter() - 1, var11) + " = ";
      String var10000;
      switch (var7) {
         case 182:
         case 185:
            var10000 = context.invokeVirtual(var6.owner, var6.name, var6.desc, value12, var8);
            break;
         case 183:
            var10000 = context.invokeNonvirtual(var6.owner, var6.name, var6.desc, value12, var8);
            break;
         case 184:
            var10000 = context.invokeStatic(var6.owner, var6.name, var6.desc, var8);
            break;
         default:
            throw new AssertionError();
      }

      String value13 = var10000;
      return var14 + value13 + ";\n";
   }

   private static String invocationVariable(int index, Type type) {
      if (index < 0) {
         throw new AssertionError();
      }

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
            throw new AssertionError();
      }

      char var2 = (char)var10000;
      return "stack" + index + "." + var2;
   }
}
