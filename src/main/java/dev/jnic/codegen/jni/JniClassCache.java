package dev.jnic.codegen.jni;

import java.util.HashMap;
import java.util.Map;

class JniClassCache {
   final String functionName;
   final Map<JniMember, String> fields;
   final Map<JniMember, String> methods;
   boolean needsLookup;
   int nextMemberId;
   final JniCache cache;

   JniClassCache(JniCache cache, String text) {
      this.cache = cache;
      this.fields = new HashMap<>();
      this.methods = new HashMap<>();
      this.needsLookup = false;
      this.nextMemberId = 0;
      this.functionName = "c_" + cache.nextClassId++;
   }

   String nextMemberName(JniMember jniMember) {
      int previousNextMemberId = this.nextMemberId++;
      return "id_" + previousNextMemberId;
   }

   String classReference() {
      return this.functionName + "(env)";
   }

   String fieldReference(JniMember jniMember) {
      if (!this.fields.containsKey(jniMember)) {
         String expression = "field: " + jniMember;
         throw new IllegalStateException(expression);
      } else {
         return this.functionName + this.fields.get(jniMember) + "(env)";
      }
   }

   String methodReference(JniMember jniMember) {
      if (!this.methods.containsKey(jniMember)) {
         String expression = "method: " + jniMember;
         throw new IllegalStateException(expression);
      } else {
         return this.functionName + this.methods.get(jniMember) + "(env)";
      }
   }

   String lookupReference() {
      if (!this.needsLookup) {
         throw new IllegalStateException("lookup");
      }

      String functionName2 = this.functionName;
      return "*" + functionName2 + "l()";
   }
}
