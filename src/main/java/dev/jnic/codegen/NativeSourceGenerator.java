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
      StringBuilder source = new StringBuilder();
      String openingParenthesis = "(";
      source.append(openingParenthesis);
      source.append(text2);
      source.append("[]) {");
      if (text == null) {
         throw new NullPointerException("");
      }

      int textLength = text.length();
      int characterIndex = 0;
      int textLength2 = textLength;

      while (characterIndex < textLength2) {
         char character = text.charAt(characterIndex);
         source.append((int)character);
         source.append(", ");
         if (text == null) {
            throw new NullPointerException("");
         }

         int textLength3 = text.length();
         textLength = characterIndex + 1;
         textLength2 = textLength3;
         characterIndex = textLength;
      }

      source.append("0}");
      return source.toString();
   }

   private static String randomizeKeyBytes(int index) {
      StringBuilder source = new StringBuilder();
      int randomValue = ThreadLocalRandom.current().nextInt(32);
      int randomValue2 = ThreadLocalRandom.current().nextInt(256);
      int assignmentCount = 0;
      int randomValue3 = randomValue2;
      int randomValue4 = randomValue;

      while (true) {
         int indentationLevel = Math.max(0, index);
         String indentation = "    ".repeat(indentationLevel);
         source.append(indentation);
         source.append("key[");
         source.append(randomValue4);
         source.append("] = ");
         source.append(randomValue3);
         source.append(";\n");
         if (assignmentCount + 1 >= 3) {
            return source.toString();
         }

         int randomValue5 = ThreadLocalRandom.current().nextInt(32);
         int randomValue6 = ThreadLocalRandom.current().nextInt(256);
         randomValue = assignmentCount + 1;
         randomValue4 = randomValue5;
         randomValue3 = randomValue6;
         assignmentCount = randomValue;
      }
   }

   @Override
   public String generate(CompilationContext context) {
      String generatedSource = new NativeRegistrationGenerator(this.methods, this.flags).generate(context);
      if (Jnic.getLicense() == null) {
         throw new RuntimeException("missing licence");
      }

      StringBuilder source = new StringBuilder(1024);
      source.append(SOURCE_HEADER);
      context.getOptions();
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

      context.getOptions();
      source.append(context.getClassCache().generate()).append(generatedSource);
      context.getOptions();
      if (context.getOptions().isStringObfuscationEnabled() || context.getOptions().isFlowObfuscationEnabled()) {
         this.appendNativeInitializer(source, context);
      }

      context.getOptions();
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
      context.getOptions();
      stringBuilder.append("    uint8_t key[32] = {");

      for (byte byteValue : context.getLiterals().getKeystream().getKey()) {
         stringBuilder.append(byteValue).append(", ");
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
