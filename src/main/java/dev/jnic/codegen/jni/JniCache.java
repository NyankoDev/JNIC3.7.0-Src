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
      Map classes2 = this.classes;
      Function<String, JniClassCache> cachedClassFactory = textx -> new JniClassCache(this, textx);
      return ((JniClassCache)classes2.computeIfAbsent((String)text, cachedClassFactory)).classReference();
   }

   public String methodId(String owner, String name, String descriptor, boolean isStatic) {
      Map classes2 = this.classes;
      Function<String, JniClassCache> cachedClassFactory = text -> new JniClassCache(this, text);
      JniClassCache cachedClass = (JniClassCache)classes2.computeIfAbsent((String)owner, cachedClassFactory);
      JniMember member = new JniMember(name, descriptor, isStatic & true);
      Map methods = cachedClass.methods;
      Objects.requireNonNull(cachedClass);
      JniClassCache cachedClass2 = cachedClass;
      Function<JniMember, String> nextMemberNameMapper = cachedClass2::nextMemberName;
      methods.computeIfAbsent((JniMember)member, nextMemberNameMapper);
      return cachedClass.methodReference(member);
   }

   public String fieldId(String owner, String name, String descriptor, boolean isStatic) {
      Map classes2 = this.classes;
      Function<String, JniClassCache> cachedClassFactory = text -> new JniClassCache(this, text);
      JniClassCache cachedClass = (JniClassCache)classes2.computeIfAbsent((String)owner, cachedClassFactory);
      JniMember member = new JniMember(name, descriptor, isStatic & true);
      Map fields = cachedClass.fields;
      Objects.requireNonNull(cachedClass);
      JniClassCache cachedClass2 = cachedClass;
      Function<JniMember, String> nextMemberNameMapper = cachedClass2::nextMemberName;
      fields.computeIfAbsent((JniMember)member, nextMemberNameMapper);
      return cachedClass.fieldReference(member);
   }

   public String methodHandleLookup(String text) {
      Map classes2 = this.classes;
      Function<String, JniClassCache> cachedClassFactory = textx -> new JniClassCache(this, textx);
      JniClassCache cachedClass = (JniClassCache)classes2.computeIfAbsent((String)text, cachedClassFactory);
      cachedClass.needsLookup = true;
      return cachedClass.lookupReference();
   }

   public String generate() {
      StringBuilder builder = new StringBuilder();

      for (Entry entry : this.classes.entrySet()) {
         JniClassCache cachedClass = (JniClassCache)entry.getValue();
         builder.append("static jclass ")
            .append(cachedClass.functionName)
            .append("(JNIEnv *env) {\n")
            .append("    static _Atomic jclass clazz;\n    jclass cached = atomic_load_explicit(&clazz, memory_order_relaxed);\n")
            .append("    if (__builtin_expect(cached != 0, 1)) return cached;\n    if ((*env)->ExceptionCheck(env)) return NULL;\n")
            .append("    jclass local = (*env)->FindClass(env, ")
            .append(this.context.getLiterals().asciiString((String)entry.getKey()))
            .append(");\n")
            .append("    if (local == NULL) return NULL;\n    jclass loaded = (*env)->NewGlobalRef(env, local);\n")
            .append("    (*env)->DeleteLocalRef(env, local);\n    if (loaded == NULL) return NULL;\n")
            .append("    if (atomic_compare_exchange_strong_explicit(&clazz, &cached, loaded, memory_order_relaxed, memory_order_relaxed))\n")
            .append("        return loaded;\n    (*env)->DeleteGlobalRef(env, loaded);\n    return cached;\n}\n");
         if (cachedClass.needsLookup) {
            builder.append("static jobject *").append(cachedClass.functionName).append("l() {\n    static jobject lookup; return &lookup;\n}\n");
         }

         this.memberAccessors(builder, cachedClass.functionName, cachedClass.fields, "field");
         this.memberAccessors(builder, cachedClass.functionName, cachedClass.methods, "method");
      }

      return builder.append('\n').toString();
   }

   private void memberAccessors(StringBuilder stringBuilder, String text, Map<JniMember, String> map, String text2) {
      String expression = "j" + text2 + "ID";
      String literalText = text2.equals("field") ? "FieldID" : "MethodID";

      for (Entry entry : map.entrySet()) {
         JniMember member = (JniMember)entry.getKey();
         stringBuilder.append("static ")
            .append(expression)
            .append(' ')
            .append(text)
            .append((String)entry.getValue())
            .append("(JNIEnv *env) {\n")
            .append("    static _Atomic ")
            .append(expression)
            .append(" id;\n    ")
            .append(expression)
            .append(" cached = atomic_load_explicit(&id, memory_order_relaxed);\n")
            .append("    if (__builtin_expect(cached != 0, 1)) return cached;\n    if ((*env)->ExceptionCheck(env)) return NULL;\n")
            .append("    jclass clazz = ")
            .append(text)
            .append("(env);\n    if (clazz == NULL) return NULL;\n")
            .append("    cached = (*env)->Get")
            .append(member.isStatic() ? "Static" : "")
            .append(literalText)
            .append("(env, clazz, ")
            .append(this.context.getLiterals().asciiString(member.getName()))
            .append(", ")
            .append(this.context.getLiterals().asciiString(member.getDescriptor()))
            .append(");\n")
            .append("    if (cached == NULL) return NULL;\n    atomic_store_explicit(&id, cached, memory_order_relaxed);\n    return cached;\n}\n");
      }
   }
}
