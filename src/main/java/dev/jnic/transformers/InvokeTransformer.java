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
      MethodInsnNode invocation = (MethodInsnNode)instruction;
      int opcode = invocation.getOpcode();
      Type[] argumentTypes = Type.getArgumentTypes(invocation.desc);
      Type returnType = Type.getReturnType(invocation.desc);
      String[] callArguments = new String[argumentTypes.length];

      for (int index = 0; index < callArguments.length; index++) {
         callArguments[index] = invocationVariable(stack.sizeBefore() - callArguments.length + index, argumentTypes[index]);
      }

      String receiver = opcode == 184 ? null : invocationVariable(stack.sizeBefore() - callArguments.length - 1, Type.getType(Object.class));
      String emptyText = returnType.getSort() == 0 ? "" : invocationVariable(stack.sizeAfter() - 1, returnType) + " = ";
      String virtualCall;
      switch (opcode) {
         case 182:
         case 185:
            virtualCall = context.invokeVirtual(invocation.owner, invocation.name, invocation.desc, receiver, callArguments);
            break;
         case 183:
            virtualCall = context.invokeNonvirtual(invocation.owner, invocation.name, invocation.desc, receiver, callArguments);
            break;
         case 184:
            virtualCall = context.invokeStatic(invocation.owner, invocation.name, invocation.desc, callArguments);
            break;
         default:
            throw new AssertionError();
      }

      String virtualCall2 = virtualCall;
      return emptyText + virtualCall2 + ";\n";
   }

   private static String invocationVariable(int index, Type type) {
      if (index < 0) {
         throw new AssertionError();
      }

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
            throw new AssertionError();
      }

      char byteValue2 = (char)byteValue;
      return "stack" + index + "." + byteValue2;
   }
}
