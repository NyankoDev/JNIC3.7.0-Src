package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;

public class ConversionTransformer extends OpcodeTransformer {
   public ConversionTransformer() {
      super(false, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147);
   }

   static String castPrefix(Type type) {
      String nativeType = JniTypes.nativeType(type);
      return "(" + nativeType + ") ";
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      int opcode = instruction.getOpcode();
      if (opcode >= 133 && opcode <= 147) {
         int topStackIndex = stack.sizeBefore() - 1;
         char character = "iiijjjfffdddiii".charAt(opcode - 133);
         char character2 = "jfdifdijdijfiii".charAt(opcode - 133);
         String expression = "stack" + topStackIndex + "." + character;
         String expression2 = "stack" + topStackIndex + "." + character2;
         String literalText;
         switch (opcode) {
            case 145:
               literalText = "jbyte";
               break;
            case 146:
               literalText = "jchar";
               break;
            case 147:
               literalText = "jshort";
               break;
            default:
               switch (character2) {
                  case 'd':
                     literalText = "jdouble";
                     break;
                  case 'e':
                  case 'g':
                  case 'h':
                  default:
                     throw new AssertionError();
                  case 'f':
                     literalText = "jfloat";
                     break;
                  case 'i':
                     literalText = "jint";
                     break;
                  case 'j':
                     literalText = "jlong";
               }
         }

         String literalText2 = literalText;
         if ((character == 'f' || character == 'd') && (character2 == 'i' || character2 == 'j')) {
            boolean matches = character2 == 'j';
            String literalText3 = matches ? "0x1p63" : "0x1p31";
            String literalText4 = matches ? "9223372036854775807LL" : "2147483647";
            return expression2
               + " = isnan("
               + expression
               + ") ? 0 : "
               + expression
               + " >= "
               + literalText3
               + " ? "
               + literalText4
               + " : "
               + expression
               + " <= -"
               + literalText3
               + " ? (-"
               + literalText4
               + " - 1) : ("
               + literalText2
               + ") "
               + expression
               + ";\n";
         } else {
            return expression2 + " = (" + literalText2 + ") " + expression + ";\n";
         }
      } else {
         throw new AssertionError();
      }
   }
}
