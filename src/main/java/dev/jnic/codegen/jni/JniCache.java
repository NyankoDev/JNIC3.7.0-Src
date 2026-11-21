package dev.jnic.codegen.jni;

import dev.jnic.compiler.CompilationContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;

public class JniCache {
   private final CompilationContext context;
   private final Map<String, JniClassCache> classes = new HashMap<>();
   int nextClassId = 0;

   public JniCache(CompilationContext context) {
      this.context = context;
   }

   public String classReference(String text) {
      Map var2 = this.classes;
      Function<String, JniClassCache> function4 = textx -> new JniClassCache(this, textx);
      return ((JniClassCache)var2.computeIfAbsent((String)text, function4)).classReference();
   }

   public String methodId(String owner, String name, String descriptor, boolean isStatic) {
      Map var5 = this.classes;
      Function<String, JniClassCache> function11 = text -> new JniClassCache(this, text);
      JniClassCache var9 = (JniClassCache)var5.computeIfAbsent((String)owner, function11);
      JniMember var10 = new JniMember(name, descriptor, isStatic & true);
      Map var10000 = var9.methods;
      Objects.requireNonNull(var9);
      JniClassCache capturedValue12 = var9;
      Function<JniMember, String> function13 = capturedValue12::nextMemberName;
      var10000.computeIfAbsent((JniMember)var10, function13);
      return var9.methodReference(var10);
   }

   public String fieldId(String owner, String name, String descriptor, boolean isStatic) {
      Map var5 = this.classes;
      Function<String, JniClassCache> function11 = text -> new JniClassCache(this, text);
      JniClassCache var9 = (JniClassCache)var5.computeIfAbsent((String)owner, function11);
      JniMember var10 = new JniMember(name, descriptor, isStatic & true);
      Map var10000 = var9.fields;
      Objects.requireNonNull(var9);
      JniClassCache capturedValue12 = var9;
      Function<JniMember, String> function13 = capturedValue12::nextMemberName;
      var10000.computeIfAbsent((JniMember)var10, function13);
      return var9.fieldReference(var10);
   }

   public String methodHandleLookup(String text) {
      Map var2 = this.classes;
      Function<String, JniClassCache> function5 = textx -> new JniClassCache(this, textx);
      JniClassCache var4 = (JniClassCache)var2.computeIfAbsent((String)text, function5);
      var4.needsLookup = true;
      return var4.lookupReference();
   }

   public String generate() {
      StringBuilder var1 = new StringBuilder();

      for (Entry var3 : this.classes.entrySet()) {
         JniClassCache var4 = (JniClassCache)var3.getValue();
         var1.append("static jclass ")
            .append(var4.functionName)
            .append("(JNIEnv *env) {\n")
            .append("    static _Atomic jclass clazz;\n    jclass cached = atomic_load_explicit(&clazz, memory_order_relaxed);\n")
            .append("    if (__builtin_expect(cached != 0, 1)) return cached;\n    if ((*env)->ExceptionCheck(env)) return NULL;\n")
            .append("    jclass local = (*env)->FindClass(env, ")
            .append(this.context.getLiterals().asciiString((String)var3.getKey()))
            .append(");\n")
            .append("    if (local == NULL) return NULL;\n    jclass loaded = (*env)->NewGlobalRef(env, local);\n")
            .append("    (*env)->DeleteLocalRef(env, local);\n    if (loaded == NULL) return NULL;\n")
            .append("    if (atomic_compare_exchange_strong_explicit(&clazz, &cached, loaded, memory_order_relaxed, memory_order_relaxed))\n")
            .append("        return loaded;\n    (*env)->DeleteGlobalRef(env, loaded);\n    return cached;\n}\n");
         if (var4.needsLookup) {
            var1.append("static jobject *").append(var4.functionName).append("l() {\n    static jobject lookup; return &lookup;\n}\n");
         }

         this.memberAccessors(var1, var4.functionName, var4.fields, "field");
         this.memberAccessors(var1, var4.functionName, var4.methods, "method");
      }

      return var1.append('\n').toString();
   }

   private void memberAccessors(StringBuilder stringBuilder, String text, Map<JniMember, String> map, String text2) {
      String var5 = "j" + text2 + "ID";
      String value6 = text2.equals("field") ? "FieldID" : "MethodID";

      for (Entry var8 : map.entrySet()) {
         JniMember var9 = (JniMember)var8.getKey();
         stringBuilder.append("static ")
            .append(var5)
            .append(' ')
            .append(text)
            .append((String)var8.getValue())
            .append("(JNIEnv *env) {\n")
            .append("    static _Atomic ")
            .append(var5)
            .append(" id;\n    ")
            .append(var5)
            .append(" cached = atomic_load_explicit(&id, memory_order_relaxed);\n")
            .append("    if (__builtin_expect(cached != 0, 1)) return cached;\n    if ((*env)->ExceptionCheck(env)) return NULL;\n")
            .append("    jclass clazz = ")
            .append(text)
            .append("(env);\n    if (clazz == NULL) return NULL;\n")
            .append("    cached = (*env)->Get")
            .append(var9.isStatic() ? "Static" : "")
            .append(value6)
            .append("(env, clazz, ")
            .append(this.context.getLiterals().asciiString(var9.getName()))
            .append(", ")
            .append(this.context.getLiterals().asciiString(var9.getDescriptor()))
            .append(");\n")
            .append("    if (cached == NULL) return NULL;\n    atomic_store_explicit(&id, cached, memory_order_relaxed);\n    return cached;\n}\n");
      }
   }
}
