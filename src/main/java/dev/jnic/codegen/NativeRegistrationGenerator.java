package dev.jnic.codegen;

import dev.jnic.bytecode.MethodReference;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.logging.JnicLogger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class NativeRegistrationGenerator implements SourceGenerator {
   private final Map<ClassNode, List<MethodNode>> methods;
   private final int flags;

   public NativeRegistrationGenerator(Map<ClassNode, List<MethodNode>> methods, int flags) {
      this.methods = methods;
      this.flags = flags;
   }

   @Override
   public String generate(CompilationContext context) {
      for (Entry<ClassNode, List<MethodNode>> entry : this.methods.entrySet()) {
         for (MethodNode method : entry.getValue()) {
            context.registerMethod(MethodReference.of(entry.getKey(), method));
         }
      }

      Objects.requireNonNull(context);
      StringBuilder output = new StringBuilder(1024);

      for (Entry<ClassNode, List<MethodNode>> entry : this.methods.entrySet()) {
         ClassNode owner = entry.getKey();
         JnicLogger.INSTANCE.debug("Processing class <" + owner.name + ">");

         for (MethodNode method : entry.getValue()) {
            String function = context.nativeFunction(MethodReference.of(owner, method));
            JnicLogger.INSTANCE.debug(" -> " + method.name + method.desc);
            output.append(new NativeMethodGenerator(owner, method, function).generate(context)).append("\n\n");
         }

         output.append("JNIEXPORT void JNICALL ")
            .append(MethodDeclarationGenerator.jniFunctionName(owner.name, "$jnicLoader", "()V"))
            .append("(JNIEnv *env, jclass clazz) {\n    ")
            .append(context.getClassCache().methodHandleLookup(owner.name))
            .append(" = ")
            .append(
               context.jniCall("NewGlobalRef", context.invokeStatic("java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;"))
            )
            .append(";\n    JNINativeMethod table[] = {\n");

         for (MethodNode method : entry.getValue()) {
            String function = context.nativeFunction(MethodReference.of(owner, method));
            if (function != null) {
               output.append("            {")
                  .append(context.getLiterals().asciiString(this.registrationName(method.name)))
                  .append(", ")
                  .append(context.getLiterals().asciiString(method.desc))
                  .append(", (void *) &")
                  .append(function)
                  .append("},\n");
            }
         }

         output.append("    };\n    (*env)->RegisterNatives(env, clazz, table, ").append(entry.getValue().size()).append(");\n}\n\n");
      }

      return output.toString();
   }

   private String registrationName(String text) {
      return "<clinit>".equals(text) ? "$jnicClinit" : text;
   }
}
