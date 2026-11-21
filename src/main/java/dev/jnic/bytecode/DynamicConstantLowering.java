package dev.jnic.bytecode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class DynamicConstantLowering {
   private final ClassNode owner;
   private final Set<String> methodNames = new HashSet<>();
   private final Map<ConstantDynamic, MethodNode> constantHelpers = new IdentityHashMap<>();
   private final List<MethodNode> helpers = new ArrayList<>();
   private int nextHelperId;

   private DynamicConstantLowering(ClassNode owner) {
      this.owner = owner;
      for (MethodNode method : owner.methods) {
         this.methodNames.add(method.name);
      }
   }

   public static List<MethodNode> lower(ClassNode owner, List<MethodNode> selectedMethods) {
      DynamicConstantLowering lowering = new DynamicConstantLowering(owner);
      for (MethodNode method : selectedMethods) {
         lowering.lower(method);
      }
      return lowering.helpers;
   }

   private void lower(MethodNode method) {
      for (AbstractInsnNode instruction : method.instructions.toArray()) {
         MethodNode helper = null;
         if (instruction instanceof LdcInsnNode && ((LdcInsnNode)instruction).cst instanceof ConstantDynamic) {
            ConstantDynamic constant = (ConstantDynamic)((LdcInsnNode)instruction).cst;
            helper = this.constantHelpers.computeIfAbsent(constant, this::constantHelper);
         } else if (instruction instanceof InvokeDynamicInsnNode) {
            InvokeDynamicInsnNode call = (InvokeDynamicInsnNode)instruction;
            for (Object argument : call.bsmArgs) {
               if (argument instanceof ConstantDynamic) {
                  helper = this.invocationHelper(call);
                  break;
               }
            }
         }
         if (helper != null) {
            method.instructions.set(instruction, new MethodInsnNode(
               Opcodes.INVOKESTATIC, this.owner.name, helper.name, helper.desc, (this.owner.access & Opcodes.ACC_INTERFACE) != 0
            ));
         }
      }
   }

   private MethodNode constantHelper(ConstantDynamic constant) {
      MethodNode helper = this.newHelper("()" + constant.getDescriptor());
      helper.visitLdcInsn(constant);
      finish(helper, 0);
      return helper;
   }

   private MethodNode invocationHelper(InvokeDynamicInsnNode call) {
      MethodNode helper = this.newHelper(call.desc);
      int local = 0;
      for (Type argument : Type.getArgumentTypes(call.desc)) {
         helper.visitVarInsn(argument.getOpcode(Opcodes.ILOAD), local);
         local += argument.getSize();
      }
      helper.visitInvokeDynamicInsn(call.name, call.desc, call.bsm, call.bsmArgs);
      finish(helper, local);
      return helper;
   }

   private MethodNode newHelper(String descriptor) {
      String name;
      do {
         name = "$jnic$dynamic$" + this.nextHelperId++;
      } while (!this.methodNames.add(name));
      // Keep resolution in the original class so the JVM owns lookup, caching and bootstrap errors.
      MethodNode helper = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC, name, descriptor, null, null);
      helper.visitCode();
      this.helpers.add(helper);
      return helper;
   }

   private static void finish(MethodNode helper, int argumentSlots) {
      Type returnType = Type.getReturnType(helper.desc);
      helper.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
      helper.visitMaxs(Math.max(argumentSlots, returnType.getSize()), argumentSlots);
      helper.visitEnd();
   }
}
