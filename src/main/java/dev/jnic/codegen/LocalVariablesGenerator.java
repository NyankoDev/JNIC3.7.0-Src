package dev.jnic.codegen;

import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicValue;

public class LocalVariablesGenerator implements SourceGenerator {
   private final String ownerName;
   private final MethodNode method;

   LocalVariablesGenerator(String ownerName, MethodNode method) {
      this.ownerName = ownerName;
      this.method = method;
   }

   @Override
   public String generate(CompilationContext context) {
      StringBuilder source = new StringBuilder();
      int localIndex = 0;

      while (true) {
         int maxLocals = this.method.maxLocals;
         if (localIndex >= maxLocals) {
            String newline = "\n";
            source.append(newline);
            int maxStack = this.method.maxStack;
            int stackPosition = 0;
            int stackIndex = 0;

            while (stackPosition < maxStack) {
               source.append("    jvalue stack");
               source.append(stackIndex);
               source.append("; memset(&stack");
               source.append(stackIndex);
               source.append(", 0, sizeof(jvalue));\n");
               stackPosition = ++stackIndex;
               maxStack = this.method.maxStack;
            }

            String newline2 = "\n";
            source.append(newline2);
            source.append("    jvalue temp");
            source.append(0);
            source.append("; memset(&temp");
            source.append(0);
            source.append(", 0, sizeof(jvalue));\n    jvalue temp");
            source.append(1);
            source.append("; memset(&temp");
            source.append(1);
            source.append(", 0, sizeof(jvalue));\n    jvalue temp");
            source.append(2);
            source.append("; memset(&temp");
            source.append(2);
            source.append(", 0, sizeof(jvalue));\n    jvalue temp");
            source.append(3);
            source.append("; memset(&temp");
            source.append(3);
            source.append(", 0, sizeof(jvalue));\n");
            String newline3 = "\n";
            source.append(newline3);
            if ((this.method.access & 8) == 0) {
               source.append("    local0.l = thisObj;\n");
               Type[] argumentTypes = Type.getArgumentTypes(this.method.desc);
               int argumentTypeCount = argumentTypes.length;
               int argumentPosition = 0;
               int index = 0;
               int localOffset = 1;

               while (argumentPosition < argumentTypeCount) {
                  source.append("    local");
                  source.append(localOffset);
                  String memberSeparator = ".";
                  source.append(memberSeparator);
                  String slotName = JniTypes.stackSlot(argumentTypes[index]);
                  source.append(slotName);
                  source.append(" = arg");
                  source.append(index);
                  source.append(";\n");
                  Type type = argumentTypes[index];
                  int slotWidth = new BasicValue(type).getSize();
                  int argumentTypeCount2 = argumentTypes.length;
                  int nextIndex = index + 1;
                  int nextIndex2 = index + 1;
                  localOffset += slotWidth;
                  index = nextIndex2;
                  argumentPosition = nextIndex;
                  argumentTypeCount = argumentTypeCount2;
               }
            } else {
               Type[] argumentTypes2 = Type.getArgumentTypes(this.method.desc);
               int argumentTypes2Count = argumentTypes2.length;
               int argumentPosition2 = 0;
               int index2 = 0;
               int localOffset2 = 0;

               while (argumentPosition2 < argumentTypes2Count) {
                  source.append("    local");
                  source.append(localOffset2);
                  String memberSeparator2 = ".";
                  source.append(memberSeparator2);
                  String slotName2 = JniTypes.stackSlot(argumentTypes2[index2]);
                  source.append(slotName2);
                  source.append(" = arg");
                  source.append(index2);
                  source.append(";\n");
                  Type type2 = argumentTypes2[index2];
                  int slotWidth2 = new BasicValue(type2).getSize();
                  int argumentTypes2Count2 = argumentTypes2.length;
                  int nextIndex3 = index2 + 1;
                  int nextIndex4 = index2 + 1;
                  localOffset2 += slotWidth2;
                  index2 = nextIndex4;
                  argumentPosition2 = nextIndex3;
                  argumentTypes2Count = argumentTypes2Count2;
               }
            }

            return source.toString();
         }

         source.append("    jvalue local");
         source.append(localIndex);
         source.append("; memset(&local");
         source.append(localIndex);
         source.append(", 0, sizeof(jvalue));\n");
         localIndex++;
      }
   }
}
