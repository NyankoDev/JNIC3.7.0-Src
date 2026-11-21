package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.logging.JnicLogger;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

public class FieldTransformer extends OpcodeTransformer {
   public FieldTransformer() {
      super(true, 178, 179, 180, 181);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      if (instruction != null && !(instruction instanceof FieldInsnNode)) {
         throw new ClassCastException("");
      }

      FieldInsnNode var6 = (FieldInsnNode)instruction;
      Type var7 = Type.getType(var6.desc);
      int var8 = stack.sizeBefore();
      int var9 = stack.sizeBefore();
      int var10 = stack.sizeBefore();
      if (var6.owner.startsWith("dev/jnic/")) {
         if (var6.name == null) {
            throw new NullPointerException("");
         }

         if (var6.name.equals("$jnicEnabled") && var6.desc.equals("Z")) {
            JnicLogger.INSTANCE.debug("Replaced $jnicEnabled access in " + ownerName);
            return JniTypes.stackVariable("stack", var10, var7) + " = 1;\n";
         }
      }

      switch (instruction.getOpcode()) {
         case 178:
            return JniTypes.stackVariable("stack", var10, var7) + " = " + context.readStaticField(var6.owner, var6.name, var6.desc) + ";\n";
         case 179:
            return context.writeStaticField(var6.owner, var6.name, var6.desc, JniTypes.stackVariable("stack", var9 - 1, var7)) + ";\n";
         case 180:
            return JniTypes.stackVariable("stack", var9 - 1, var7)
               + " = "
               + context.readField(var6.owner, var6.name, var6.desc, JniTypes.stackVariable("stack", var9 - 1, Type.getObjectType("java/lang/Object")))
               + ";\n";
         case 181:
            return context.writeField(
                  var6.owner,
                  var6.name,
                  var6.desc,
                  JniTypes.stackVariable("stack", var8 - 2, Type.getObjectType("java/lang/Object")),
                  JniTypes.stackVariable("stack", var9 - 1, var7)
               )
               + ";\n";
         default:
            throw new AssertionError();
      }
   }
}
