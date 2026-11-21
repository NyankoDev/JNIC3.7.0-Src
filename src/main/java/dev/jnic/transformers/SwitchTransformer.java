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
      LinkedHashMap<Integer, LabelNode> entries = new LinkedHashMap();
      LabelNode defaultTarget;
      StringBuilder source;
      if (instruction.getOpcode() != 170) {
         LookupSwitchInsnNode lookupSwitch = (LookupSwitchInsnNode)instruction;
         List caseKeys = lookupSwitch.keys;

         for (int index = 0; caseKeys.size() > index; caseKeys = lookupSwitch.keys) {
            Integer integer = lookupSwitch.keys.get(index);
            LabelNode label = lookupSwitch.labels.get(index);
            entries.put(integer, label);
            index++;
         }

         defaultTarget = lookupSwitch.dflt;
         source = new StringBuilder();
         source.append("switch (");
         int stackSizeBefore = stack.sizeBefore();
         Type intType = Type.INT_TYPE;
         String stackValue = JniTypes.stackVariable("stack", stackSizeBefore - 1, intType);
         source.append(stackValue);
         String closingParenthesis = ")";
         source.append(closingParenthesis);
         source.append(" {\n");

         for (Entry<Integer, LabelNode> entry : entries.entrySet()) {
            source.append("    case ");
            Object key = entry.getKey();
            source.append(key);
            source.append(": ");
            String oneLiteral = new String("1");
            LabelNode label2 = (LabelNode)entry.getValue();
            String branchSource = jumps.jumpIf(oneLiteral, label2);
            source.append(branchSource);
            String newline = "\n";
            source.append(newline);
         }
      } else {
         TableSwitchInsnNode tableSwitch = (TableSwitchInsnNode)instruction;
         List caseLabels = tableSwitch.labels;

         for (int index2 = 0; caseLabels.size() > index2; caseLabels = tableSwitch.labels) {
            Integer caseValue = tableSwitch.min + index2;
            LabelNode label3 = tableSwitch.labels.get(index2);
            entries.put((Integer)caseValue, label3);
            index2++;
         }

         defaultTarget = tableSwitch.dflt;
         source = new StringBuilder();
         source.append("switch (");
         int stackSizeBefore2 = stack.sizeBefore();
         Type intType2 = Type.INT_TYPE;
         String stackValue2 = JniTypes.stackVariable("stack", stackSizeBefore2 - 1, intType2);
         source.append(stackValue2);
         String closingParenthesis2 = ")";
         source.append(closingParenthesis2);
         source.append(" {\n");

         for (Entry<Integer, LabelNode> entry2 : entries.entrySet()) {
            source.append("    case ");
            Object key2 = entry2.getKey();
            source.append(key2);
            source.append(": ");
            String oneLiteral2 = new String("1");
            LabelNode label4 = (LabelNode)entry2.getValue();
            String tableBranchSource = jumps.jumpIf(oneLiteral2, label4);
            source.append(tableBranchSource);
            String newline2 = "\n";
            source.append(newline2);
         }
      }

      source.append("    default: ");
      String oneLiteral3 = new String("1");
      String defaultBranchSource = jumps.jumpIf(oneLiteral3, defaultTarget);
      source.append(defaultBranchSource);
      source.append(";\n    }\n");
      return source.toString();
   }
}
