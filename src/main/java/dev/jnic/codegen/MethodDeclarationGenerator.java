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
      String encodedOwner = SourceText.encodeJniName(text);
      String encodedMethod = SourceText.encodeJniName(text2);
      String nameSeparator = "_";
      String expression = "Java_" + encodedOwner + nameSeparator + encodedMethod;
      Type[] argumentTypes = Type.getArgumentTypes(text3);
      if (argumentTypes.length > 0) {
         Stream<Type> stream24 = Arrays.stream(argumentTypes);
         Function<Type, String> descriptorMapper = Type::getDescriptor;
         String encodedArguments = SourceText.encodeJniName(stream24.map(descriptorMapper).collect(Collectors.joining(new String(""))));
         return expression + "__" + encodedArguments;
      } else {
         return expression;
      }
   }

   @Override
   public String generate(CompilationContext context) {
      Type returnType = Type.getReturnType(this.method.desc);
      Type[] argumentTypes = Type.getArgumentTypes(this.method.desc);
      StringBuilder source = new StringBuilder();
      String nativeType;
      if (this.functionName == null) {
         source.append("JNIEXPORT ");
         nativeType = JniTypes.nativeType(returnType);
      } else {
         nativeType = JniTypes.nativeType(returnType);
      }

      source.append(nativeType);
      source.append(" JNICALL ");
      String functionName2 = this.functionName();
      source.append(functionName2);
      source.append("(JNIEnv *env, jobject thisObj");
      int argumentTypeCount = argumentTypes.length;
      int index = 0;
      int argumentTypeCount2 = argumentTypeCount;

      while (index < argumentTypeCount2) {
         source.append(", ");
         String nativeType2 = JniTypes.nativeType(argumentTypes[index]);
         source.append(nativeType2);
         source.append(" arg");
         source.append(index);
         int argumentTypeCount3 = argumentTypes.length;
         argumentTypeCount = index + 1;
         argumentTypeCount2 = argumentTypeCount3;
         index = argumentTypeCount;
      }

      String closingParenthesis = ")";
      source.append(closingParenthesis);
      return source.toString();
   }

   private String functionName() {
      return this.functionName != null ? this.functionName : jniFunctionName(this.owner.name, this.method.name, this.method.desc);
   }
}
