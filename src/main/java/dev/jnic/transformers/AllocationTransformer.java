package dev.jnic.transformers;

import dev.jnic.analysis.OperandStack;
import dev.jnic.codegen.JumpEmitter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class AllocationTransformer extends OpcodeTransformer {
   public AllocationTransformer() {
      super(true, 187, 188, 189, 197);
   }

   @Override
   public String transform(CompilationContext context, AbstractInsnNode instruction, OperandStack stack, JumpEmitter jumps, String ownerName) {
      StringBuilder var6 = new StringBuilder();
      int var7 = stack.sizeBefore();
      int var8 = instruction.getOpcode();
      if (var8 <= 188) {
         if (var8 == 187) {
            int var46 = stack.sizeBefore();
            Type var28 = JniTypes.OBJECT_TYPE;
            String value29 = JniTypes.stackVariable("stack", var46, var28);
            var6.append(value29);
            var6.append(" = ");
            String[] var44 = new String[1];
            String value41 = context.getClassCache().classReference(((TypeInsnNode)instruction).desc);
            var44[0] = value41;
            String value42 = context.jniCall("AllocObject", var44);
            var6.append(value42);
            var6.append(";\n");
            return var6.toString();
         }

         if (var8 != 188) {
            throw new AssertionError();
         }

         Type var25 = JniTypes.OBJECT_TYPE;
         String value30 = JniTypes.stackVariable("stack", var7 - 1, var25);
         var6.append(value30);
         var6.append(" = ");
         Type var31 = JniTypes.newArrayType((IntInsnNode)instruction);
         Type var45 = Type.INT_TYPE;
         String value33 = JniTypes.stackVariable("stack", var7 - 1, var45);
         String value43 = this.allocateArray(context, var31, value33);
         var6.append(value43);
         var6.append(";\n");
         return var6.toString();
      } else {
         if (var8 == 189) {
            Type var9 = JniTypes.OBJECT_TYPE;
            String value10 = JniTypes.stackVariable("stack", var7 - 1, var9);
            var6.append(value10);
            var6.append(" = ");
            Type var26 = Type.getObjectType(((TypeInsnNode)instruction).desc);
            Type var11 = Type.INT_TYPE;
            String value15 = JniTypes.stackVariable("stack", var7 - 1, var11);
            String value40 = this.allocateArray(context, var26, value15);
            var6.append(value40);
            var6.append(";\n");
            return var6.toString();
         }

         if (var8 != 197) {
            throw new AssertionError();
         }

         MultiANewArrayInsnNode var24 = (MultiANewArrayInsnNode)instruction;
         Type var16 = Type.getType(var24.desc);
         int var17 = var24.dims;
         var6.append("{\n");
         String value27 = this.indentation(2);
         var6.append(value27);
         var6.append("jsize dim[] = {");
         IntStream var10000 = IntStream.range(var7 - 1 - var17 + 1, var7 - 1 + 1);
         IntFunction<String> function56 = index -> {
            Type var8x = Type.INT_TYPE;
            return JniTypes.stackVariable("stack", index, var8x);
         };
         String var32 = var10000.mapToObj(function56).collect(Collectors.joining(", "));
         var6.append(var32);
         var6.append("};");
         int var18 = 0;
         int var12 = var24.dims;

         while (var18 < var12) {
            label49: {
               String value47 = this.indentation(var18 + 2);
               var6.append(value47);
               Type var48 = JniTypes.OBJECT_TYPE;
               String value49 = JniTypes.stackVariable("stack", var18 + var7 - 1 - var17 + 1, var48);
               var6.append(value49);
               var6.append(" = ");
               Type var50 = this.componentType(var16, var18 + 1);
               String value14 = "]";
               String var13 = "dim[" + var18 + value14;
               String value51 = this.allocateArray(context, var50, var13);
               var6.append(value51);
               var6.append(";\n");
               if (var18 <= 0) {
                  if (var24.dims - 1 <= var18) {
                     break label49;
                  }
               } else {
                  String value53 = this.indentation(var18 + 2);
                  var6.append(value53);
                  String[] var34 = new String[3];
                  Type var35 = JniTypes.OBJECT_TYPE;
                  String value36 = JniTypes.stackVariable("stack", var7 - 1 - var17 + 1 + var18 - 1, var35);
                  var34[0] = value36;
                  String var37 = "d" + (var18 - 1);
                  var34[1] = var37;
                  Type var38 = JniTypes.OBJECT_TYPE;
                  String value39 = JniTypes.stackVariable("stack", var7 - 1 - var17 + 1 + var18, var38);
                  var34[2] = value39;
                  String value54 = context.jniCall("SetObjectArrayElement", var34);
                  var6.append(value54);
                  var6.append(";\n");
                  if (var24.dims - 1 <= var18) {
                     break label49;
                  }
               }

               String value52 = this.indentation(var18 + 2);
               var6.append(value52);
               var6.append("for(jsize d");
               var6.append(var18);
               var6.append(" = 0; d");
               var6.append(var18);
               var6.append(" < dim[");
               var6.append(var18);
               var6.append("]; d");
               var6.append(var18);
               var6.append("++) {\n");
               int var55 = var24.dims;
               int var57 = var18 + 1;
               var12 = var55;
               var18 = var57;
               continue;
            }

            int var23 = var24.dims;
            int var58 = var18 + 1;
            var12 = var23;
            var18 = var58;
         }

         for (int var20 = var24.dims - 2; var20 >= 0; var20--) {
            String value22 = this.indentation(var20 + 2);
            var6.append(value22);
            var6.append("}\n");
         }

         var6.append("    }\n;\n");
         return var6.toString();
      }
   }

   private String allocateArray(CompilationContext context, Type type, String text) {
      int var4 = type.getSort();
      if (var4 == 10) {
         String[] var12 = new String[]{text, null, null};
         String value15 = context.getClassCache().classReference(type.getInternalName());
         var12[1] = value15;
         var12[2] = "NULL";
         return context.jniCall("NewObjectArray", var12);
      } else if (var4 != 9) {
         String value5 = JniTypes.jniTypeSuffix(type);
         String var21 = "New" + value5 + "Array";
         String[] var22 = new String[]{text};
         return context.jniCall(var21, var22);
      } else {
         String[] var23 = new String[]{text, null, null};
         String value24 = context.getClassCache().classReference(type.getInternalName());
         var23[1] = value24;
         var23[2] = "NULL";
         return context.jniCall("NewObjectArray", var23);
      }
   }

   private Type componentType(Type type, int index) {
      return Type.getType(type.getDescriptor().substring(index));
   }

   private String indentation(int index) {
      Supplier<String> function2 = () -> "    ";
      return Stream.generate(function2).limit(index).collect(Collectors.joining());
   }
}
