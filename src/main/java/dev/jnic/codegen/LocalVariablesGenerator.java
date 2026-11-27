package dev.jnic.codegen;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

import dev.jnic.compiler.CompilationContext;
import dev.jnic.utils.JniTypes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public class LocalVariablesGenerator implements SourceGenerator {
   private final MethodNode method;

   LocalVariablesGenerator(MethodNode method) {
      this.method = method;
   }

   @Override
   public String generate(CompilationContext context) {
      StringBuilder source = new StringBuilder();
      appendVariables(source, "local", this.method.maxLocals);
      source.append('\n');
      appendVariables(source, "stack", this.method.maxStack);
      source.append('\n');
      appendVariables(source, "temp", 4);
      source.append('\n');

      boolean isStatic = (this.method.access & ACC_STATIC) != 0;
      int localOffset = isStatic ? 0 : 1;
      if (!isStatic) {
         source.append("    local0.l = thisObj;\n");
      }
      Type[] argumentTypes = Type.getArgumentTypes(this.method.desc);
      for (int argument = 0; argument < argumentTypes.length; argument++) {
         Type type = argumentTypes[argument];
         source.append("    local").append(localOffset).append('.').append(JniTypes.stackSlot(type))
            .append(" = arg").append(argument).append(";\n");
         localOffset += type.getSize();
      }
      return source.toString();
   }

   private static void appendVariables(StringBuilder source, String prefix, int count) {
      for (int index = 0; index < count; index++) {
         source.append("    jvalue ").append(prefix).append(index).append("; memset(&").append(prefix).append(index)
            .append(", 0, sizeof(jvalue));\n");
      }
   }
}
