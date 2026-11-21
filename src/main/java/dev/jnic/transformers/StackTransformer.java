package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.compiler.CompilationException;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

public class StackTransformer extends OpcodeTransformer {
   public StackTransformer() {
      super(false, 87, 88, 89, 90, 91, 92, 93, 94, 95);
   }

   private int saveStackValue(StringBuilder source, int stackIndex, int temporaryIndex, Type type) {
      String temporaryValue = JniTypes.stackVariable("temp", temporaryIndex, type);
      source.append(temporaryValue);
      source.append(" = ");
      String stackValue = JniTypes.stackVariable("stack", stackIndex, type);
      source.append(stackValue);
      source.append(";\n    ");
      return type.getSize();
   }

   private void restoreStackValue(StringBuilder source, int stackIndex, int temporaryIndex, Type type) {
      String stackValue = JniTypes.stackVariable("stack", stackIndex, type);
      source.append(stackValue);
      source.append(" = ");
      String temporaryValue = JniTypes.stackVariable("temp", temporaryIndex, type);
      source.append(temporaryValue);
      source.append(";\n    ");
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      int opcode = instruction.getOpcode();
      if (opcode == 87 || opcode == 88) {
         return "\n";
      }

      if (opcode >= 89 && opcode <= 95) {
         int stackSizeBefore = stack.sizeBefore();
         StringBuilder source = new StringBuilder();
         if (opcode == 95) {
            if (this.saveStackValue(source, stackSizeBefore - 1, 0, stack.typeBefore(stackSizeBefore - 1)) == 1
               && this.saveStackValue(source, stackSizeBefore - 2, 1, stack.typeBefore(stackSizeBefore - 2)) == 1) {
               this.restoreStackValue(source, stackSizeBefore - 2, 0, stack.typeAfter(stackSizeBefore - 2));
               this.restoreStackValue(source, stackSizeBefore - 1, 1, stack.typeAfter(stackSizeBefore - 1));
               return source.toString();
            } else {
               throw new CompilationException("Illegal use of SWAP");
            }
         } else {
            int duplicatedWordCount = opcode < 92 ? 1 : 2;
            int insertedWordCount = (opcode - 89) % 3;
            int duplicatedValueCount = this.saveWords(source, stack, stackSizeBefore, 0, duplicatedWordCount, opcode);
            int movedValueCount = this.saveWords(source, stack, stackSizeBefore, duplicatedValueCount, insertedWordCount, opcode);
            int destinationIndex = stackSizeBefore - duplicatedValueCount - movedValueCount;

            for (int duplicateIndex = duplicatedValueCount - 1; duplicateIndex >= 0; duplicateIndex--) {
               this.restoreStackValue(source, destinationIndex, duplicateIndex, stack.typeAfter(destinationIndex));
               destinationIndex++;
            }

            for (int movedIndex = duplicatedValueCount + movedValueCount - 1; movedIndex >= duplicatedValueCount; movedIndex--) {
               this.restoreStackValue(source, destinationIndex, movedIndex, stack.typeAfter(destinationIndex));
               destinationIndex++;
            }

            for (int repeatIndex = duplicatedValueCount - 1; repeatIndex >= 0; repeatIndex--) {
               this.restoreStackValue(source, destinationIndex, repeatIndex, stack.typeAfter(destinationIndex));
               destinationIndex++;
            }

            return source.toString();
         }
      } else {
         throw new AssertionError();
      }
   }

   private int saveWords(StringBuilder source, OperandStack stack, int stackSize, int skipCount, int wordCount, int opcode) {
      int savedValues = 0;

      int savedWords;
      for (savedWords = 0; savedWords < wordCount; savedValues++) {
         int temporaryIndex = skipCount + savedValues;
         int stackIndex = stackSize - 1 - temporaryIndex;
         savedWords += this.saveStackValue(source, stackIndex, temporaryIndex, stack.typeBefore(stackIndex));
      }

      if (savedWords != wordCount) {
         throw new CompilationException("Illegal use of " + new String[]{"DUP", "DUP_X1", "DUP_X2", "DUP2", "DUP2_X1", "DUP2_X2"}[opcode - 89]);
      } else {
         return savedValues;
      }
   }
}
