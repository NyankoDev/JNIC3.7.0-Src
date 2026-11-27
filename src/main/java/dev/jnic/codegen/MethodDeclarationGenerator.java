package dev.jnic.codegen;

import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import dev.jnic.utils.SourceText;
import java.util.Arrays;
import java.util.stream.Collectors;
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

   public static String jniFunctionName(String ownerName, String methodName, String descriptor) {
      String name = "Java_" + SourceText.encodeJniName(ownerName) + "_" + SourceText.encodeJniName(methodName);
      Type[] argumentTypes = Type.getArgumentTypes(descriptor);
      if (argumentTypes.length == 0) {
         return name;
      }
      String arguments = Arrays.stream(argumentTypes).map(Type::getDescriptor).collect(Collectors.joining());
      return name + "__" + SourceText.encodeJniName(arguments);
   }

   @Override
   public String generate(CompilationContext context) {
      StringBuilder source = new StringBuilder();
      if (this.functionName == null) {
         source.append("JNIEXPORT ");
      }
      source.append(JniTypes.nativeType(Type.getReturnType(this.method.desc))).append(" JNICALL ")
         .append(this.functionName()).append("(JNIEnv *env, jobject thisObj");
      Type[] argumentTypes = Type.getArgumentTypes(this.method.desc);
      for (int argument = 0; argument < argumentTypes.length; argument++) {
         source.append(", ").append(JniTypes.nativeType(argumentTypes[argument])).append(" arg").append(argument);
      }
      return source.append(')').toString();
   }

   private String functionName() {
      return this.functionName != null ? this.functionName : jniFunctionName(this.owner.name, this.method.name, this.method.desc);
   }
}
