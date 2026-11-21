package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class TypeCheckTransformer extends OpcodeTransformer {
   public TypeCheckTransformer() {
      super(true, 192, 193);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      StringBuilder var6 = new StringBuilder();
      TypeInsnNode var11 = (TypeInsnNode)instruction;
      int var7 = stack.sizeBefore();
      if (var11.getOpcode() == 192) {
         var6.append("if (!");
         String[] var8 = new String[2];
         Type var9 = JniTypes.OBJECT_TYPE;
         String value10 = JniTypes.stackVariable("stack", var7 - 1, var9);
         var8[0] = value10;
         String value19 = context.getClassCache().classReference(var11.desc);
         var8[1] = value19;
         String value20 = context.jniCall("IsInstanceOf", var8);
         var6.append(value20);
         var6.append(") ");
         String[] var21 = new String[2];
         String value16 = context.getClassCache().classReference("java/lang/ClassCastException");
         var21[0] = value16;
         var21[1] = "\"\"";
         String var23 = "(void)(" + context.jniCall("ThrowNew", var21) + ")";
         var6.append(var23);
         var6.append(";\n");
         return var6.toString();
      } else {
         Type var12 = Type.INT_TYPE;
         String value13 = JniTypes.stackVariable("stack", var7 - 1, var12);
         var6.append(value13);
         var6.append(" = ");
         Type var17 = JniTypes.OBJECT_TYPE;
         String value14 = JniTypes.stackVariable("stack", var7 - 1, var17);
         var6.append(value14);
         var6.append(" != NULL && ");
         String[] var15 = new String[2];
         Type var18 = JniTypes.OBJECT_TYPE;
         String value24 = JniTypes.stackVariable("stack", var7 - 1, var18);
         var15[0] = value24;
         String value25 = context.getClassCache().classReference(var11.desc);
         var15[1] = value25;
         String value26 = context.jniCall("IsInstanceOf", var15);
         var6.append(value26);
         var6.append(";\n");
         return var6.toString();
      }
   }
}
