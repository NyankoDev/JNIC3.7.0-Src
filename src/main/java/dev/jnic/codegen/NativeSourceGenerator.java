package dev.jnic.codegen;

import dev.jnic.Jnic;
import dev.jnic.compiler.CompilationContext;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class NativeSourceGenerator implements SourceGenerator {
   private static final String SOURCE_HEADER = "/* \n * Auto-generated JNIC implementation file\n * \n * \n * \n * \n * \n * \n */\n\n#include <jni.h>\n#include <stdatomic.h>\n#include <stdlib.h>\n#include <string.h>\n#include <stdarg.h>\n#include <math.h>\n\n";
   private static final String SOURCE_FOOTER = "/* End JNIC implementation file */\n";
   private final Map<ClassNode, List<MethodNode>> methods;
   private final int flags;

   public NativeSourceGenerator(Map<ClassNode, List<MethodNode>> methods, int flags) {
      this.methods = methods;
      this.flags = flags;
   }

   @Override
   public String generate(CompilationContext context) {
      String generatedSource = new NativeRegistrationGenerator(this.methods, this.flags).generate(context);
      if (Jnic.getLicense() == null) {
         throw new RuntimeException("missing licence");
      }

      StringBuilder source = new StringBuilder(1024);
      source.append(SOURCE_HEADER);
      if (context.getOptions().isStringObfuscationEnabled() || context.getOptions().isFlowObfuscationEnabled()) {
         source.append("const uint8_t *magic_constant = (uint8_t*) \"")
            .append(Jnic.CHACHA_MAGIC)
            .append("\";\n")
            .append("#include <crypto.h>\nstatic uint8_t *jnic_buf;\n\n");
      }

      if (context.getOptions().isStringObfuscationEnabled()) {
         source.append("static inline void* jnic_decrypt(char *a, size_t offset, const size_t len) {\n")
            .append("    for(size_t i = 0; i < len; i++) a[i] ^= jnic_buf[offset + i]; return a;\n}\n");
      }

      source.append(context.getClassCache().generate()).append(generatedSource);
      if (context.getOptions().isStringObfuscationEnabled() || context.getOptions().isFlowObfuscationEnabled()) {
         this.appendNativeInitializer(source, context);
      }

      return source.append(SOURCE_FOOTER).toString();
   }

   private void appendNativeInitializer(StringBuilder stringBuilder, CompilationContext context) {
      stringBuilder.append("\nJNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {\n")
         .append("    JNIEnv *env; (*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_6);\n")
         .append("    jclass loader = (*env)->FindClass(env, \"")
         .append(context.getLoaderClassName())
         .append("\");\n")
         .append("    jfieldID buf_field_id = (*env)->GetStaticFieldID(env, loader, \"z\", \"Ljava/nio/ByteBuffer;\");\n")
         .append("    jobject buf = (*env)->GetStaticObjectField(env, loader, buf_field_id);\n")
         .append("    uint8_t *java_buf = (*env)->GetDirectBufferAddress(env, buf);\n");
      stringBuilder.append("    uint8_t key[32] = {");

      for (byte byteValue : context.getLiterals().getKeystream().getKey()) {
         stringBuilder.append(byteValue).append(", ");
      }

      stringBuilder.append("};\n");
      stringBuilder.append("    struct chacha20_context c;\n    chacha20_init_context(&c, key, java_buf + 32, 0);\n")
         .append("    jnic_buf = malloc(")
         .append(context.getLiterals().getKeystream().getPosition())
         .append(");\n")
         .append("    chacha20_write_stream(&c, jnic_buf, ")
         .append(context.getLiterals().getKeystream().getPosition())
         .append(");\n")
         .append("    return JNI_VERSION_1_6;\n}\n");
   }
}
