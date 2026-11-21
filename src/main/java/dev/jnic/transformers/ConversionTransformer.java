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
      String value1 = JniTypes.nativeType(type);
      return "(" + value1 + ") ";
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      int var6 = instruction.getOpcode();
      if (var6 >= 133 && var6 <= 147) {
         int var7 = stack.sizeBefore() - 1;
         char var8 = "iiijjjfffdddiii".charAt(var6 - 133);
         char var9 = "jfdifdijdijfiii".charAt(var6 - 133);
         String var10 = "stack" + var7 + "." + var8;
         String var11 = "stack" + var7 + "." + var9;
         String var10000;
         switch (var6) {
            case 145:
               var10000 = "jbyte";
               break;
            case 146:
               var10000 = "jchar";
               break;
            case 147:
               var10000 = "jshort";
               break;
            default:
               switch (var9) {
                  case 'd':
                     var10000 = "jdouble";
                     break;
                  case 'e':
                  case 'g':
                  case 'h':
                  default:
                     throw new AssertionError();
                  case 'f':
                     var10000 = "jfloat";
                     break;
                  case 'i':
                     var10000 = "jint";
                     break;
                  case 'j':
                     var10000 = "jlong";
               }
         }

         String value12 = var10000;
         if ((var8 == 'f' || var8 == 'd') && (var9 == 'i' || var9 == 'j')) {
            boolean var13 = var9 == 'j';
            String value14 = var13 ? "0x1p63" : "0x1p31";
            String value15 = var13 ? "9223372036854775807LL" : "2147483647";
            return var11
               + " = isnan("
               + var10
               + ") ? 0 : "
               + var10
               + " >= "
               + value14
               + " ? "
               + value15
               + " : "
               + var10
               + " <= -"
               + value14
               + " ? (-"
               + value15
               + " - 1) : ("
               + value12
               + ") "
               + var10
               + ";\n";
         } else {
            return var11 + " = (" + value12 + ") " + var10 + ";\n";
         }
      } else {
         throw new AssertionError();
      }
   }
}
