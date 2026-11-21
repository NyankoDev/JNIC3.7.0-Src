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
      StringBuilder var2 = new StringBuilder();
      int var4 = 0;

      while (true) {
         int var5 = this.method.maxLocals;
         if (var4 >= var5) {
            String value12 = "\n";
            var2.append(value12);
            int var13 = this.method.maxStack;
            int var7 = 0;
            int var23 = 0;

            while (var7 < var13) {
               var2.append("    jvalue stack");
               var2.append(var23);
               var2.append("; memset(&stack");
               var2.append(var23);
               var2.append(", 0, sizeof(jvalue));\n");
               var7 = ++var23;
               var13 = this.method.maxStack;
            }

            String value27 = "\n";
            var2.append(value27);
            var2.append("    jvalue temp");
            var2.append(0);
            var2.append("; memset(&temp");
            var2.append(0);
            var2.append(", 0, sizeof(jvalue));\n    jvalue temp");
            var2.append(1);
            var2.append("; memset(&temp");
            var2.append(1);
            var2.append(", 0, sizeof(jvalue));\n    jvalue temp");
            var2.append(2);
            var2.append("; memset(&temp");
            var2.append(2);
            var2.append(", 0, sizeof(jvalue));\n    jvalue temp");
            var2.append(3);
            var2.append("; memset(&temp");
            var2.append(3);
            var2.append(", 0, sizeof(jvalue));\n");
            String value3 = "\n";
            var2.append(value3);
            if ((this.method.access & 8) == 0) {
               var2.append("    local0.l = thisObj;\n");
               Type[] var28 = Type.getArgumentTypes(this.method.desc);
               int var10 = var28.length;
               int var8 = 0;
               int var15 = 0;
               int var6 = 1;

               while (var8 < var10) {
                  var2.append("    local");
                  var2.append(var6);
                  String value16 = ".";
                  var2.append(value16);
                  String value29 = JniTypes.stackSlot(var28[var15]);
                  var2.append(value29);
                  var2.append(" = arg");
                  var2.append(var15);
                  var2.append(";\n");
                  Type var17 = var28[var15];
                  int var25 = new BasicValue(var17).getSize();
                  int var10000 = var28.length;
                  int var10001 = var15 + 1;
                  int var10002 = var15 + 1;
                  var6 += var25;
                  var15 = var10002;
                  var8 = var10001;
                  var10 = var10000;
               }
            } else {
               Type[] var18 = Type.getArgumentTypes(this.method.desc);
               int var9 = var18.length;
               int var11 = 0;
               int var19 = 0;
               int var24 = 0;

               while (var11 < var9) {
                  var2.append("    local");
                  var2.append(var24);
                  String value20 = ".";
                  var2.append(value20);
                  String value21 = JniTypes.stackSlot(var18[var19]);
                  var2.append(value21);
                  var2.append(" = arg");
                  var2.append(var19);
                  var2.append(";\n");
                  Type var22 = var18[var19];
                  int var26 = new BasicValue(var22).getSize();
                  int var30 = var18.length;
                  int var31 = var19 + 1;
                  int var32 = var19 + 1;
                  var24 += var26;
                  var19 = var32;
                  var11 = var31;
                  var9 = var30;
               }
            }

            return var2.toString();
         }

         var2.append("    jvalue local");
         var2.append(var4);
         var2.append("; memset(&local");
         var2.append(var4);
         var2.append(", 0, sizeof(jvalue));\n");
         var4++;
      }
   }
}
