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
      int var2 = this.nextMemberId++;
      return "id_" + var2;
   }

   String classReference() {
      return this.functionName + "(env)";
   }

   String fieldReference(JniMember jniMember) {
      if (!this.fields.containsKey(jniMember)) {
         String var2 = "field: " + jniMember;
         throw new IllegalStateException(var2);
      } else {
         return this.functionName + this.fields.get(jniMember) + "(env)";
      }
   }

   String methodReference(JniMember jniMember) {
      if (!this.methods.containsKey(jniMember)) {
         String var25 = "method: " + jniMember;
         throw new IllegalStateException(var25);
      } else {
         return this.functionName + this.methods.get(jniMember) + "(env)";
      }
   }

   String lookupReference() {
      if (!this.needsLookup) {
         throw new IllegalStateException("lookup");
      }

      String var5 = this.functionName;
      return "*" + var5 + "l()";
   }
}
