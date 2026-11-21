package dev.jnic.analysis;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

public class TypedBasicInterpreter extends BasicInterpreter {
   public TypedBasicInterpreter() {
      super(589824);
   }

   @Override
   public BasicValue newOperation(AbstractInsnNode instruction) throws AnalyzerException {
      if (instruction.getOpcode() == 187) {
         if (instruction instanceof TypeInsnNode) {
            return new BasicValue(Type.getObjectType(((TypeInsnNode)instruction).desc));
         } else {
            throw new ClassCastException("");
         }
      } else {
         if (instruction.getOpcode() == 18) {
            if (!(instruction instanceof LdcInsnNode)) {
               throw new ClassCastException("");
            }

            Object constant = ((LdcInsnNode)instruction).cst;
            if (constant instanceof String) {
               return new BasicValue(Type.getObjectType("java/lang/String"));
            }

            if (constant instanceof Type) {
               return new BasicValue(Type.getObjectType(((Type)constant).getSort() == 11 ? "java/lang/invoke/MethodType" : "java/lang/Class"));
            }

            if (constant instanceof Handle) {
               return new BasicValue(Type.getObjectType("java/lang/invoke/MethodHandle"));
            }
         }

         return super.newOperation(instruction);
      }
   }

   @Override
   public BasicValue merge(BasicValue basicValue, BasicValue basicValue2) {
      if (!basicValue.isReference()) {
         return super.merge(basicValue, basicValue2);
      } else if (!basicValue2.isReference()) {
         return super.merge(basicValue, basicValue2);
      } else {
         return !basicValue.equals(basicValue2) ? BasicValue.REFERENCE_VALUE : super.merge(basicValue, basicValue2);
      }
   }
}
