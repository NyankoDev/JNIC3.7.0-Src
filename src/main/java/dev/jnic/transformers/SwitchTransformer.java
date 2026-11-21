package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

public class SwitchTransformer extends OpcodeTransformer {
   public SwitchTransformer() {
      super(false, 170, 171);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      LinkedHashMap<Integer, LabelNode> items8 = new LinkedHashMap();
      LabelNode var7;
      StringBuilder var15;
      if (instruction.getOpcode() != 170) {
         LookupSwitchInsnNode var13 = (LookupSwitchInsnNode)instruction;
         List var6 = var13.keys;

         for (int var9 = 0; var6.size() > var9; var6 = var13.keys) {
            Integer var16 = var13.keys.get(var9);
            LabelNode var19 = var13.labels.get(var9);
            items8.put(var16, var19);
            var9++;
         }

         var7 = var13.dflt;
         var15 = new StringBuilder();
         var15.append("switch (");
         int var11 = stack.sizeBefore();
         Type var10 = Type.INT_TYPE;
         String value12 = JniTypes.stackVariable("stack", var11 - 1, var10);
         var15.append(value12);
         String value33 = ")";
         var15.append(value33);
         var15.append(" {\n");

         for (Entry<Integer, LabelNode> items34 : items8.entrySet()) {
            var15.append("    case ");
            Object var27 = items34.getKey();
            var15.append(var27);
            var15.append(": ");
            String var28 = new String("1");
            LabelNode var35 = (LabelNode)items34.getValue();
            String value36 = jumps.jumpIf(var28, var35);
            var15.append(value36);
            String value37 = "\n";
            var15.append(value37);
         }
      } else {
         TableSwitchInsnNode var14 = (TableSwitchInsnNode)instruction;
         List var17 = var14.labels;

         for (int var24 = 0; var17.size() > var24; var17 = var14.labels) {
            Integer var18 = var14.min + var24;
            LabelNode var20 = var14.labels.get(var24);
            items8.put((Integer)var18, var20);
            var24++;
         }

         var7 = var14.dflt;
         var15 = new StringBuilder();
         var15.append("switch (");
         int var32 = stack.sizeBefore();
         Type var29 = Type.INT_TYPE;
         String value38 = JniTypes.stackVariable("stack", var32 - 1, var29);
         var15.append(value38);
         String value39 = ")";
         var15.append(value39);
         var15.append(" {\n");

         for (Entry<Integer, LabelNode> items40 : items8.entrySet()) {
            var15.append("    case ");
            Object var30 = items40.getKey();
            var15.append(var30);
            var15.append(": ");
            String var31 = new String("1");
            LabelNode var41 = (LabelNode)items40.getValue();
            String value42 = jumps.jumpIf(var31, var41);
            var15.append(value42);
            String value43 = "\n";
            var15.append(value43);
         }
      }

      var15.append("    default: ");
      String var22 = new String("1");
      String value26 = jumps.jumpIf(var22, var7);
      var15.append(value26);
      var15.append(";\n    }\n");
      return var15.toString();
   }
}
