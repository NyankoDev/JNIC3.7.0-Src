package dev.jnic.codegen;

import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import dev.jnic.utils.SourceText;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodDeclarationGenerator implements SourceGenerator {
   private final ClassNode owner;
   private final MethodNode method;
   private final String functionName;

   MethodDeclarationGenerator(ClassNode owner, MethodNode method, String functionName) {
      this.owner = owner;
      this.method = method;
      this.functionName = functionName;
   }

   public static String jniFunctionName(String text, String text2, String text3) {
      String value3 = SourceText.encodeJniName(text);
      String value4 = SourceText.encodeJniName(text2);
      String value12 = "_";
      String var14 = "Java_" + value3 + value12 + value4;
      Type[] var15 = Type.getArgumentTypes(text3);
      if (var15.length > 0) {
         Stream<Type> stream24 = Arrays.stream(var15);
         Function<Type, String> function25 = Type::getDescriptor;
         String value23 = SourceText.encodeJniName(stream24.map(function25).collect(Collectors.joining(new String(""))));
         return var14 + "__" + value23;
      } else {
         return var14;
      }
   }

   @Override
   public String generate(CompilationContext context) {
      Type var4 = Type.getReturnType(this.method.desc);
      Type[] var3 = Type.getArgumentTypes(this.method.desc);
      StringBuilder var2 = new StringBuilder();
      String value12;
      if (this.functionName == null) {
         var2.append("JNIEXPORT ");
         value12 = JniTypes.nativeType(var4);
      } else {
         value12 = JniTypes.nativeType(var4);
      }

      var2.append(value12);
      var2.append(" JNICALL ");
      String value13 = this.functionName();
      var2.append(value13);
      var2.append("(JNIEnv *env, jobject thisObj");
      int var10000 = var3.length;
      int var8 = 0;
      int var6 = var10000;

      while (var8 < var6) {
         var2.append(", ");
         String value7 = JniTypes.nativeType(var3[var8]);
         var2.append(value7);
         var2.append(" arg");
         var2.append(var8);
         int var10 = var3.length;
         var10000 = var8 + 1;
         var6 = var10;
         var8 = var10000;
      }

      String value11 = ")";
      var2.append(value11);
      return var2.toString();
   }

   private String functionName() {
      return this.functionName != null ? this.functionName : jniFunctionName(this.owner.name, this.method.name, this.method.desc);
   }
}
