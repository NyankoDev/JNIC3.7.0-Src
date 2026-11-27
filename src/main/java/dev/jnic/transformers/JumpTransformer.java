package dev.jnic.transformers;

import static org.objectweb.asm.Opcodes.*;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import java.util.concurrent.ThreadLocalRandom;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;

public class JumpTransformer extends OpcodeTransformer {
   public JumpTransformer() {
      super(false, IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE,
         IF_ACMPEQ, IF_ACMPNE, IFNULL, IFNONNULL, GOTO);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      String condition = this.condition(context, instruction.getOpcode(), stack.sizeBefore());
      return jumps.jumpIf(condition, ((JumpInsnNode)instruction).label) + "\n";
   }

   private String condition(CompilationContext context, int opcode, int stackSize) {
      if (opcode >= IFEQ && opcode <= IFLE) {
         return JniTypes.stackVariable("stack", stackSize - 1, Type.INT_TYPE) + comparisonOperator(opcode) + "0";
      }
      if (opcode >= IF_ICMPEQ && opcode <= IF_ICMPLE) {
         return JniTypes.stackVariable("stack", stackSize - 2, Type.INT_TYPE) + comparisonOperator(opcode)
            + JniTypes.stackVariable("stack", stackSize - 1, Type.INT_TYPE);
      }
      switch (opcode) {
         case IF_ACMPEQ:
         case IF_ACMPNE:
            String sameObject = context.jniCall("IsSameObject",
               JniTypes.stackVariable("stack", stackSize - 2, JniTypes.OBJECT_TYPE),
               JniTypes.stackVariable("stack", stackSize - 1, JniTypes.OBJECT_TYPE));
            return opcode == IF_ACMPEQ ? sameObject : "!" + sameObject;
         case IFNULL:
         case IFNONNULL:
            return JniTypes.stackVariable("stack", stackSize - 1, JniTypes.OBJECT_TYPE)
               + (opcode == IFNULL ? " == NULL" : " != NULL");
         case GOTO:
            return context.getOptions().isFlowObfuscationEnabled()
               ? context.getLiterals().encryptedInteger(ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE))
               : "1";
         default:
            throw new AssertionError();
      }
   }

   private static String comparisonOperator(int opcode) {
      switch (opcode) {
         case IFEQ:
         case IF_ICMPEQ:
            return " == ";
         case IFNE:
         case IF_ICMPNE:
            return " != ";
         case IFLT:
         case IF_ICMPLT:
            return " < ";
         case IFGE:
         case IF_ICMPGE:
            return " >= ";
         case IFGT:
         case IF_ICMPGT:
            return " > ";
         case IFLE:
         case IF_ICMPLE:
            return " <= ";
         default:
            throw new AssertionError();
      }
   }
}
