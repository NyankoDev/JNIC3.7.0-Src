package dev.jnic.compiler;

import dev.jnic.bytecode.MethodReference;
import dev.jnic.codegen.NativeLiterals;
import dev.jnic.codegen.TypeBoxing;
import dev.jnic.codegen.jni.JniCache;
import dev.jnic.codegen.jni.JniFunctionTypes;
import dev.jnic.config.CompilerOptions;
import dev.jnic.runtime.RuntimeNameRemapper;
import dev.jnic.utils.SequentialNameGenerator;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;

public class CompilationContext {
   private final String runtimePackage;
   private final String loaderClassName;
   private final CompilerOptions options;
   private final SequentialNameGenerator names = new SequentialNameGenerator();
   private final Map<MethodReference, String> functions = new HashMap<>();
   private final SecureRandom random;
   private final JniCache classCache;
   private final NativeLiterals literals;

   public CompilationContext(CompilerOptions options) {
      this.options = options;
      this.runtimePackage = "dev/jnic/" + RuntimeNameRemapper.randomName(6);
      this.loaderClassName = this.runtimePackage + "/JNICLoader";
      this.random = new SecureRandom();
      this.classCache = new JniCache(this);
      this.literals = new NativeLiterals(this);
   }

   public String getRuntimePackage() {
      return this.runtimePackage;
   }

   public String getLoaderClassName() {
      return this.loaderClassName;
   }

   public CompilerOptions getOptions() {
      return this.options;
   }

   public void registerMethod(MethodReference methodReference) {
      Map var10000 = this.functions;
      String value4 = this.names.nextName();
      var10000.put((MethodReference)methodReference, (String)value4);
   }

   public String nativeFunction(MethodReference methodReference) {
      return this.functions.get(methodReference);
   }

   public SecureRandom getRandom() {
      return this.random;
   }

   public JniCache getClassCache() {
      return this.classCache;
   }

   public NativeLiterals getLiterals() {
      return this.literals;
   }

   public static String jniTypeSuffix(Type type) {
      int var1 = type.getSort();
      if (Integer.compareUnsigned(var1, 10) > 0) {
         String var20 = "No full name for " + type;
         throw new IllegalArgumentException(var20);
      }

      switch (var1) {
         case 0:
            return "Void";
         case 1:
            return "Boolean";
         case 2:
            return "Char";
         case 3:
            return "Byte";
         case 4:
            return "Short";
         case 5:
            return "Int";
         case 6:
            return "Float";
         case 7:
            return "Long";
         case 8:
            return "Double";
         case 9:
            return "Object";
         case 10:
            return "Object";
         default:
            throw new AssertionError();
      }
   }

   public String invokeStatic(String owner, String name, String descriptor, String... arguments) {
      String[] var6 = new String[arguments.length + 2];
      String value8 = this.classCache.classReference(owner);
      var6[0] = value8;
      String value10 = this.classCache.methodId(owner, name, descriptor, true);
      var6[1] = value10;
      int var11 = arguments.length;
      System.arraycopy((Object)arguments, 0, (Object)var6, 2, var11);
      String value13 = jniTypeSuffix(Type.getReturnType(descriptor));
      String var29 = "CallStatic" + value13 + "Method";
      return this.jniCall(var29, var6);
   }

   public String invokeNonvirtual(String owner, String name, String descriptor, String receiver, String... arguments) {
      String[] var8 = new String[arguments.length + 3];
      var8[0] = receiver;
      String value15 = this.classCache.classReference(owner);
      var8[1] = value15;
      String value20 = this.classCache.methodId(owner, name, descriptor, false);
      var8[2] = value20;
      int var23 = arguments.length;
      System.arraycopy((Object)arguments, 0, (Object)var8, 3, var23);
      String value28 = jniTypeSuffix(Type.getReturnType(descriptor));
      if (value28 == null) {
         String var61 = "CallNonvirtual" + value28 + "Method";
         return this.jniCall(var61, var8);
      } else {
         String var62 = "CallNonvirtual" + value28 + "Method";
         return this.jniCall(var62, var8);
      }
   }

   public String invokeVirtual(String owner, String name, String descriptor, String receiver, String... arguments) {
      String[] var7 = new String[arguments.length + 2];
      var7[0] = receiver;
      String value9 = this.classCache.methodId(owner, name, descriptor, false);
      var7[1] = value9;
      int var10 = arguments.length;
      System.arraycopy((Object)arguments, 0, (Object)var7, 2, var10);
      String value12 = jniTypeSuffix(Type.getReturnType(descriptor));
      String var28 = "Call" + value12 + "Method";
      return this.jniCall(var28, var7);
   }

   public String readField(String owner, String name, String descriptor, String receiver) {
      String value6 = jniTypeSuffix(Type.getType(descriptor));
      String var22 = "Get" + value6 + "Field";
      String[] var23 = new String[]{receiver, null};
      String value25 = this.classCache.fieldId(owner, name, descriptor, false);
      var23[1] = value25;
      return this.jniCall(var22, var23);
   }

   public String writeField(String owner, String name, String descriptor, String receiver, String value) {
      String value7 = jniTypeSuffix(Type.getType(descriptor));
      String var23 = "Set" + value7 + "Field";
      String[] var24 = new String[]{receiver, null, null};
      String value26 = this.classCache.fieldId(owner, name, descriptor, false);
      var24[1] = value26;
      var24[2] = value;
      return this.jniCall(var23, var24);
   }

   public String readStaticField(String owner, String name, String descriptor) {
      String value5 = jniTypeSuffix(Type.getType(descriptor));
      String var21 = "GetStatic" + value5 + "Field";
      String[] var22 = new String[2];
      String value24 = this.classCache.classReference(owner);
      var22[0] = value24;
      String value26 = this.classCache.fieldId(owner, name, descriptor, true);
      var22[1] = value26;
      return this.jniCall(var21, var22);
   }

   public String writeStaticField(String owner, String name, String descriptor, String value) {
      String value6 = jniTypeSuffix(Type.getType(descriptor));
      String var22 = "SetStatic" + value6 + "Field";
      String[] var23 = new String[3];
      String value25 = this.classCache.classReference(owner);
      var23[0] = value25;
      String value27 = this.classCache.fieldId(owner, name, descriptor, true);
      var23[1] = value27;
      var23[2] = value;
      return this.jniCall(var22, var23);
   }

   public String constant(String owner, Object value) {
      if (value instanceof Integer) {
         return value.toString();
      }

      if (value instanceof String) {
         String var3 = (String)value;
         return this.jniCall("NewString", var3.isEmpty() ? "NULL" : this.literals.utf16String(var3), Integer.toString(var3.length()));
      }

      if (value instanceof Long) {
         Long var6 = (Long)value;
         return var6 + (var6 == Long.MIN_VALUE ? "ULL" : "LL");
      }

      if (value instanceof Float || value instanceof Double) {
         return floatingLiteral((Number)value);
      }

      if (value instanceof Type) {
         Type var7 = (Type)value;
         if (var7.getSort() <= 8) {
            return this.readStaticField(TypeBoxing.wrapperClass(var7), "TYPE", "Ljava/lang/Class;");
         } else if (var7.getSort() == 9 || var7.getSort() == 10) {
            return this.classCache.classReference(var7.getInternalName());
         } else if (var7.getSort() == 11) {
            String value4 = this.constant(owner, var7.getDescriptor());
            String value5 = this.invokeVirtual("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", this.classCache.classReference(owner));
            return this.invokeStatic(
               "java/lang/invoke/MethodType",
               "fromMethodDescriptorString",
               "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;",
               value4,
               value5
            );
         } else {
            throw new AssertionError("LDC of type " + var7);
         }
      } else if (value instanceof Handle) {
         Handle var8 = (Handle)value;
         return this.handleLiteral(owner, var8);
      } else {
         throw new IllegalArgumentException("LDC of unknown " + value);
      }
   }

   public String jniCall(String name, String... arguments) {
      if (!allowsPending(name) && !name.equals("GetPrimitiveArrayCritical") && !name.equals("GetStringCritical")) {
         String var3 = JniFunctionTypes.FUNCTION_TYPES.get(name);
         if (var3 == null) {
            throw new IllegalArgumentException("Unknown JNI function: " + name);
         }

         String value4 = var3.substring(0, var3.indexOf(" (JNICALL"));
         boolean var5 = !value4.equals("void");
         StringBuilder var6 = new StringBuilder("({ ");
         if (var5) {
            var6.append(value4).append(" jnic_result = 0; ");
         }

         var6.append("if (!(*env)->ExceptionCheck(env)) { ");
         String[] var7 = new String[arguments.length];

         for (int var8 = 0; var8 < arguments.length; var8++) {
            var7[var8] = "jnic_argument" + var8;
            var6.append("__auto_type ").append(var7[var8]).append(" = (").append(arguments[var8]).append("); ");
         }

         if (requiresReceiver(name)) {
            var6.append("if (!(*env)->ExceptionCheck(env) && jnic_argument0 == NULL) { ")
               .append("jclass jnic_exception = (*env)->FindClass(env, \"java/lang/NullPointerException\"); ")
               .append("if (jnic_exception != NULL) { (*env)->ThrowNew(env, jnic_exception, \"\"); (*env)->DeleteLocalRef(env, jnic_exception); } } ");
         }

         var6.append("if (!(*env)->ExceptionCheck(env)) { ");
         if (var5) {
            var6.append("jnic_result = ");
         }

         var6.append(rawJniCall(name, var7)).append("; (*env)->ExceptionCheck(env); } } ");
         return var6.append(var5 ? "jnic_result; })" : "(void)0; })").toString();
      } else {
         return rawJniCall(name, arguments);
      }
   }

   private static String floatingLiteral(Number number) {
      double var1 = number.doubleValue();
      if (Double.isNaN(var1)) {
         return "NAN";
      } else if (var1 == Double.POSITIVE_INFINITY) {
         return "INFINITY";
      } else {
         return var1 == Double.NEGATIVE_INFINITY ? "-INFINITY" : number.toString() + (number instanceof Float ? "f" : "");
      }
   }

   private String handleLiteral(String text, Handle handle) {
      int var3 = handle.getTag();
      if (var3 >= 1 && var3 <= 9) {
         Type var4 = Type.getObjectType(handle.getOwner());
         String value5 = this.classCache.methodHandleLookup(text);
         String value6 = this.constant(text, var4);
         if (var3 == 8) {
            return this.invokeVirtual(
               "java/lang/invoke/MethodHandles$Lookup",
               "findConstructor",
               "(Ljava/lang/Class;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
               value5,
               value6,
               this.constant(text, Type.getType(handle.getDesc()))
            );
         } else {
            String value7 = this.constant(text, handle.getName());
            String value8 = this.constant(text, Type.getType(handle.getDesc()));
            if (var3 <= 4) {
               String var9 = "find" + (var3 != 2 && var3 != 4 ? "" : "Static") + (var3 != 3 && var3 != 4 ? "Getter" : "Setter");
               return this.invokeVirtual(
                  "java/lang/invoke/MethodHandles$Lookup",
                  var9,
                  "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;",
                  value5,
                  value6,
                  value7,
                  value8
               );
            } else {
               return var3 == 7
                  ? this.invokeVirtual(
                     "java/lang/invoke/MethodHandles$Lookup",
                     "findSpecial",
                     "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;",
                     value5,
                     value6,
                     value7,
                     value8,
                     this.constant(text, var4)
                  )
                  : this.invokeVirtual(
                     "java/lang/invoke/MethodHandles$Lookup",
                     var3 == 6 ? "findStatic" : "findVirtual",
                     "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
                     value5,
                     value6,
                     value7,
                     value8
                  );
            }
         }
      } else {
         throw new AssertionError("LDC of handle " + handle);
      }
   }

   private static String rawJniCall(String text, String[] values) {
      StringBuilder var2 = new StringBuilder("(*env)->").append(text).append("(env");
      String[] var3 = values;
      int var4 = values.length;

      for (int var5 = 0; var5 < var4; var5++) {
         String var6 = var3[var5];
         var2.append(", ").append(var6);
      }

      return var2.append(')').toString();
   }

   private static boolean allowsPending(String text) {
      return text.startsWith("Exception")
         || text.startsWith("Release")
         || text.startsWith("Delete")
         || text.equals("MonitorExit")
         || text.equals("PushLocalFrame")
         || text.equals("PopLocalFrame");
   }

   private static boolean requiresReceiver(String text) {
      return text.equals("GetArrayLength")
         || text.equals("GetObjectClass")
         || text.equals("MonitorEnter")
         || text.startsWith("GetString")
         || (text.startsWith("Get") || text.startsWith("Set")) && (text.contains("ArrayRegion") || text.contains("ArrayElement"))
         || (text.startsWith("Get") || text.startsWith("Set")) && text.endsWith("Field") && !text.contains("Static")
         || text.startsWith("Call") && text.contains("Method") && !text.contains("Static");
   }
}
