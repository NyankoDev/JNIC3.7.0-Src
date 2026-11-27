package dev.jnic.analysis;

import static org.objectweb.asm.Opcodes.*;

import java.util.List;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

class NullnessInterpreter extends Interpreter<NullnessValue> {
   public static final NullnessInterpreter INSTANCE = new NullnessInterpreter();
   private static final String UNSUPPORTED_MESSAGE = "Unsupported JSR/RET instruction";

   private NullnessInterpreter() {
      super(ASM9);
   }

   @Override
   public NullnessValue newValue(Type type) {
      if (type == Type.VOID_TYPE) {
         return null;
      } else {
         return type != null && type.getSize() == 2 ? NullnessValue.UNKNOWN2 : NullnessValue.UNKNOWN1;
      }
   }

   @Override
   public NullnessValue newParameterValue(boolean enabled, int index, Type type) {
      return enabled && index == 0 ? NullnessValue.NOT_NULL : this.newValue(type);
   }

   @Override
   public NullnessValue newOperation(AbstractInsnNode instruction) throws AnalyzerException {
      switch (instruction.getOpcode()) {
         case ACONST_NULL:
            return NullnessValue.NULL;
         case ICONST_M1:
         case ICONST_0:
         case ICONST_1:
         case ICONST_2:
         case ICONST_3:
         case ICONST_4:
         case ICONST_5:
         case FCONST_0:
         case FCONST_1:
         case FCONST_2:
         case BIPUSH:
         case SIPUSH:
            return NullnessValue.UNKNOWN1;
         case LCONST_0:
         case LCONST_1:
         case DCONST_0:
         case DCONST_1:
            return NullnessValue.UNKNOWN2;
         case LDC:
            Object constant = ((LdcInsnNode)instruction).cst;
            if (constant instanceof String || constant instanceof Type || constant instanceof Handle) {
               return NullnessValue.NOT_NULL;
            }
            return constant instanceof Double || constant instanceof Long ? NullnessValue.UNKNOWN2 : NullnessValue.UNKNOWN1;
         case JSR:
            throw new AnalyzerException(instruction, UNSUPPORTED_MESSAGE);
         case GETSTATIC:
            return NullnessValue.unknownValue(Type.getType(((FieldInsnNode)instruction).desc));
         case NEW:
            return NullnessValue.NOT_NULL;
         default:
            throw new AssertionError();
      }
   }

   @Override
   public NullnessValue copyOperation(AbstractInsnNode instruction, NullnessValue nullnessValue) {
      return nullnessValue;
   }

   @Override
   public NullnessValue unaryOperation(AbstractInsnNode instruction, NullnessValue nullnessValue) {
      switch (instruction.getOpcode()) {
         case INEG:
         case FNEG:
         case IINC:
         case I2F:
         case L2I:
         case L2F:
         case F2I:
         case D2I:
         case D2F:
         case I2B:
         case I2C:
         case I2S:
         case ARRAYLENGTH:
         case INSTANCEOF:
            return NullnessValue.UNKNOWN1;
         case LNEG:
         case DNEG:
         case I2L:
         case I2D:
         case L2D:
         case F2L:
         case F2D:
         case D2L:
            return NullnessValue.UNKNOWN2;
         case IFEQ:
         case IFNE:
         case IFLT:
         case IFGE:
         case IFGT:
         case IFLE:
         case TABLESWITCH:
         case LOOKUPSWITCH:
         case IRETURN:
         case LRETURN:
         case FRETURN:
         case DRETURN:
         case ARETURN:
         case PUTSTATIC:
         case ATHROW:
         case MONITORENTER:
         case MONITOREXIT:
         case IFNULL:
         case IFNONNULL:
            return null;
         case GETFIELD:
            return NullnessValue.unknownValue(Type.getType(((FieldInsnNode)instruction).desc));
         case NEWARRAY:
         case ANEWARRAY:
            return NullnessValue.NOT_NULL;
         case CHECKCAST:
            return nullnessValue;
         default:
            throw new AssertionError();
      }
   }

   @Override
   public NullnessValue binaryOperation(AbstractInsnNode instruction, NullnessValue nullnessValue, NullnessValue nullnessValue2) {
      switch (instruction.getOpcode()) {
         case IALOAD:
         case FALOAD:
         case AALOAD:
         case BALOAD:
         case CALOAD:
         case SALOAD:
         case IADD:
         case FADD:
         case ISUB:
         case FSUB:
         case IMUL:
         case FMUL:
         case IDIV:
         case FDIV:
         case IREM:
         case FREM:
         case ISHL:
         case ISHR:
         case IUSHR:
         case IAND:
         case IOR:
         case IXOR:
         case LCMP:
         case FCMPL:
         case FCMPG:
         case DCMPL:
         case DCMPG:
            return NullnessValue.UNKNOWN1;
         case LALOAD:
         case DALOAD:
         case LADD:
         case DADD:
         case LSUB:
         case DSUB:
         case LMUL:
         case DMUL:
         case LDIV:
         case DDIV:
         case LREM:
         case DREM:
         case LSHL:
         case LSHR:
         case LUSHR:
         case LAND:
         case LOR:
         case LXOR:
            return NullnessValue.UNKNOWN2;
         case IF_ICMPEQ:
         case IF_ICMPNE:
         case IF_ICMPLT:
         case IF_ICMPGE:
         case IF_ICMPGT:
         case IF_ICMPLE:
         case IF_ACMPEQ:
         case IF_ACMPNE:
         case PUTFIELD:
            return null;
         default:
            throw new AssertionError();
      }
   }

   @Override
   public NullnessValue ternaryOperation(AbstractInsnNode instruction, NullnessValue nullnessValue, NullnessValue nullnessValue2, NullnessValue nullnessValue3) {
      return null;
   }

   @Override
   public NullnessValue naryOperation(AbstractInsnNode instruction, List<? extends NullnessValue> list) {
      if (instruction == null) {
         throw new AssertionError();
      } else if (instruction instanceof MethodInsnNode) {
         return NullnessValue.unknownValue(Type.getReturnType(((MethodInsnNode)instruction).desc));
      } else if (instruction instanceof InvokeDynamicInsnNode) {
         return NullnessValue.unknownValue(Type.getReturnType(((InvokeDynamicInsnNode)instruction).desc));
      } else if (instruction instanceof MultiANewArrayInsnNode) {
         return NullnessValue.NOT_NULL;
      } else {
         throw new AssertionError();
      }
   }

   @Override
   public void returnOperation(AbstractInsnNode instruction, NullnessValue nullnessValue, NullnessValue nullnessValue2) {
   }

   @Override
   public NullnessValue merge(NullnessValue nullnessValue, NullnessValue nullnessValue2) {
      return nullnessValue.merge(nullnessValue2);
   }
}
