package dev.jnic.codegen;

import dev.jnic.Jnic;
import dev.jnic.compiler.CompilationContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
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

   private static String characterArray(String text, String text2) {
      StringBuilder var4 = new StringBuilder();
      String value6 = "(";
      var4.append(value6);
      var4.append(text2);
      var4.append("[]) {");
      if (text == null) {
         throw new NullPointerException("");
      }

      int var10000 = text.length();
      int var30 = 0;
      int var32 = var10000;

      while (var30 < var32) {
         char var36 = text.charAt(var30);
         var4.append((int)var36);
         var4.append(", ");
         if (text == null) {
            throw new NullPointerException("");
         }

         int var55 = text.length();
         var10000 = var30 + 1;
         var32 = var55;
         var30 = var10000;
      }

      var4.append("0}");
      return var4.toString();
   }

   private static String randomizeKeyBytes(int index) {
      StringBuilder var3 = new StringBuilder();
      int var10000 = ThreadLocalRandom.current().nextInt(32);
      int var10001 = ThreadLocalRandom.current().nextInt(256);
      int var13 = 0;
      int var15 = var10001;
      int var16 = var10000;

      while (true) {
         int var30 = Math.max(0, index);
         String value32 = "    ".repeat(var30);
         var3.append(value32);
         var3.append("key[");
         var3.append(var16);
         var3.append("] = ");
         var3.append(var15);
         var3.append(";\n");
         if (var13 + 1 >= 3) {
            return var3.toString();
         }

         int var87 = ThreadLocalRandom.current().nextInt(32);
         int var91 = ThreadLocalRandom.current().nextInt(256);
         var10000 = var13 + 1;
         var16 = var87;
         var15 = var91;
         var13 = var10000;
      }
   }

   @Override
   public String generate(CompilationContext context) {
      String value2 = new NativeRegistrationGenerator(this.methods, this.flags).generate(context);
      if (Jnic.getLicense() == null) {
         throw new RuntimeException("missing licence");
      }

      StringBuilder var3 = new StringBuilder(1024);
      var3.append(SOURCE_HEADER);
      context.getOptions();
      if (context.getOptions().isStringObfuscationEnabled() || context.getOptions().isFlowObfuscationEnabled()) {
         var3.append("const uint8_t *magic_constant = (uint8_t*) \"")
            .append(Jnic.CHACHA_MAGIC)
            .append("\";\n")
            .append("#include <crypto.h>\nstatic uint8_t *jnic_buf;\n\n");
      }

      if (context.getOptions().isStringObfuscationEnabled()) {
         var3.append("static inline void* jnic_decrypt(char *a, size_t offset, const size_t len) {\n")
            .append("    for(size_t i = 0; i < len; i++) a[i] ^= jnic_buf[offset + i]; return a;\n}\n");
      }

      context.getOptions();
      var3.append(context.getClassCache().generate()).append(value2);
      context.getOptions();
      if (context.getOptions().isStringObfuscationEnabled() || context.getOptions().isFlowObfuscationEnabled()) {
         this.appendNativeInitializer(var3, context);
      }

      context.getOptions();
      return var3.append(SOURCE_FOOTER).toString();
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
      context.getOptions();
      stringBuilder.append("    uint8_t key[32] = {");

      for (byte var6 : context.getLiterals().getKeystream().getKey()) {
         stringBuilder.append(var6).append(", ");
      }

      stringBuilder.append("};\n");
      context.getOptions();
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
