package dev.jnic.analysis;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

public class NullnessFrame extends AliasFrame<NullnessValue> {
   private AliasSet branchAliases;

   public NullnessFrame(int localCount, int maxStackSize) {
      super(localCount, maxStackSize);
   }

   public NullnessFrame(Frame<? extends NullnessValue> frame) {
      super(frame);
   }

   private void refineAliases(AliasSet aliases, NullnessValue value) {
      for (int slot = aliases.nextSetBit(0); slot >= 0; slot = aliases.nextSetBit(slot + 1)) {
         this.setSlot(slot, value);
      }
   }

   @Override
   public void initJumpTarget(int opcode, LabelNode target) {
      if (opcode == IFNULL || opcode == IFNONNULL) {
         boolean isNull = (opcode == IFNULL) == (target != null);
         this.refineAliases(this.branchAliases, isNull ? NullnessValue.NULL : NullnessValue.NOT_NULL);
      }
   }

   @Override
   public void execute(AbstractInsnNode instruction, Interpreter<NullnessValue> interpreter) throws AnalyzerException {
      int opcode = instruction.getOpcode();
      this.branchAliases = opcode == IFNULL || opcode == IFNONNULL ? this.aliasSet(this.topSlot()) : null;
      AliasSet dereferencedAliases = this.dereferencedAliases(instruction);
      super.execute(instruction, interpreter);
      if (dereferencedAliases != null) {
         this.refineAliases(dereferencedAliases, NullnessValue.NOT_NULL);
      }
   }

   private AliasSet dereferencedAliases(AbstractInsnNode instruction) {
      int top = this.topSlot();
      switch (instruction.getOpcode()) {
         case GETFIELD:
         case ARRAYLENGTH:
         case MONITORENTER:
         case MONITOREXIT:
            return this.aliasSet(top);
         case PUTFIELD:
         case IALOAD:
         case LALOAD:
         case FALOAD:
         case DALOAD:
         case AALOAD:
         case BALOAD:
         case CALOAD:
         case SALOAD:
            return this.aliasSet(top - 1);
         case IASTORE:
         case LASTORE:
         case FASTORE:
         case DASTORE:
         case AASTORE:
         case BASTORE:
         case CASTORE:
         case SASTORE:
            return this.aliasSet(top - 2);
         case INVOKEVIRTUAL:
         case INVOKESPECIAL:
         case INVOKEINTERFACE:
            int argumentCount = Type.getArgumentTypes(((MethodInsnNode)instruction).desc).length;
            return this.aliasSet(top - argumentCount);
         default:
            return null;
      }
   }
}
