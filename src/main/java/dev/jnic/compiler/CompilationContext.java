package dev.jnic.compiler;

import dev.jnic.bytecode.MethodReference;
import dev.jnic.codegen.NativeLiterals;
import dev.jnic.codegen.TypeBoxing;
import dev.jnic.codegen.jni.JniCache;
import dev.jnic.codegen.jni.JniFunctionTypes;
import dev.jnic.config.CompilerOptions;
import dev.jnic.runtime.RuntimeNameRemapper;
import dev.jnic.utils.JniTypes;
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

   public void registerMethod(MethodReference method) {
      this.functions.put(method, this.names.nextName());
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
      if (type.getSort() < Type.VOID || type.getSort() > Type.OBJECT) {
         throw new IllegalArgumentException("No full name for " + type);
      }
      return JniTypes.jniTypeSuffix(type);
   }

   public String invokeStatic(String owner, String name, String descriptor, String... arguments) {
      String[] callArguments = new String[arguments.length + 2];
      callArguments[0] = this.classCache.classReference(owner);
      callArguments[1] = this.classCache.methodId(owner, name, descriptor, true);
      System.arraycopy(arguments, 0, callArguments, 2, arguments.length);
      return this.jniCall("CallStatic" + jniTypeSuffix(Type.getReturnType(descriptor)) + "Method", callArguments);
   }

   public String invokeNonvirtual(String owner, String name, String descriptor, String receiver, String... arguments) {
      String[] callArguments = new String[arguments.length + 3];
      callArguments[0] = receiver;
      callArguments[1] = this.classCache.classReference(owner);
      callArguments[2] = this.classCache.methodId(owner, name, descriptor, false);
      System.arraycopy(arguments, 0, callArguments, 3, arguments.length);
      return this.jniCall("CallNonvirtual" + jniTypeSuffix(Type.getReturnType(descriptor)) + "Method", callArguments);
   }

   public String invokeVirtual(String owner, String name, String descriptor, String receiver, String... arguments) {
      String[] callArguments = new String[arguments.length + 2];
      callArguments[0] = receiver;
      callArguments[1] = this.classCache.methodId(owner, name, descriptor, false);
      System.arraycopy(arguments, 0, callArguments, 2, arguments.length);
      return this.jniCall("Call" + jniTypeSuffix(Type.getReturnType(descriptor)) + "Method", callArguments);
   }

   public String readField(String owner, String name, String descriptor, String receiver) {
      return this.jniCall("Get" + jniTypeSuffix(Type.getType(descriptor)) + "Field",
         receiver, this.classCache.fieldId(owner, name, descriptor, false));
   }

   public String writeField(String owner, String name, String descriptor, String receiver, String value) {
      return this.jniCall("Set" + jniTypeSuffix(Type.getType(descriptor)) + "Field",
         receiver, this.classCache.fieldId(owner, name, descriptor, false), value);
   }

   public String readStaticField(String owner, String name, String descriptor) {
      String classReference = this.classCache.classReference(owner);
      String fieldId = this.classCache.fieldId(owner, name, descriptor, true);
      return this.jniCall("GetStatic" + jniTypeSuffix(Type.getType(descriptor)) + "Field", classReference, fieldId);
   }

   public String writeStaticField(String owner, String name, String descriptor, String value) {
      String classReference = this.classCache.classReference(owner);
      String fieldId = this.classCache.fieldId(owner, name, descriptor, true);
      return this.jniCall("SetStatic" + jniTypeSuffix(Type.getType(descriptor)) + "Field", classReference, fieldId, value);
   }

   public String constant(String owner, Object value) {
      if (value instanceof Integer) {
         return value.toString();
      }

      if (value instanceof String) {
         String stringValue = (String)value;
         return this.jniCall("NewString", stringValue.isEmpty() ? "NULL" : this.literals.utf16String(stringValue), Integer.toString(stringValue.length()));
      }

      if (value instanceof Long) {
         Long longValue = (Long)value;
         return longValue + (longValue == Long.MIN_VALUE ? "ULL" : "LL");
      }

      if (value instanceof Float || value instanceof Double) {
         return floatingLiteral((Number)value);
      }

      if (value instanceof Type) {
         Type constantType = (Type)value;
         if (constantType.getSort() <= 8) {
            return this.readStaticField(TypeBoxing.wrapperClass(constantType), "TYPE", "Ljava/lang/Class;");
         } else if (constantType.getSort() == 9 || constantType.getSort() == 10) {
            return this.classCache.classReference(constantType.getInternalName());
         } else if (constantType.getSort() == 11) {
            String constantExpression = this.constant(owner, constantType.getDescriptor());
            String virtualCall = this.invokeVirtual("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", this.classCache.classReference(owner));
            return this.invokeStatic(
               "java/lang/invoke/MethodType",
               "fromMethodDescriptorString",
               "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;",
               constantExpression,
               virtualCall
            );
         } else {
            throw new AssertionError("LDC of type " + constantType);
         }
      } else if (value instanceof Handle) {
         Handle methodHandle = (Handle)value;
         return this.handleLiteral(owner, methodHandle);
      } else {
         throw new IllegalArgumentException("LDC of unknown " + value);
      }
   }

   public String jniCall(String name, String... arguments) {
      if (!allowsPending(name) && !name.equals("GetPrimitiveArrayCritical") && !name.equals("GetStringCritical")) {
         String functionSignature = JniFunctionTypes.FUNCTION_TYPES.get(name);
         if (functionSignature == null) {
            throw new IllegalArgumentException("Unknown JNI function: " + name);
         }

         String returnType = functionSignature.substring(0, functionSignature.indexOf(" (JNICALL"));
         boolean hasReturnValue = !returnType.equals("void");
         StringBuilder builder = new StringBuilder("({ ");
         if (hasReturnValue) {
            builder.append(returnType).append(" jnic_result = 0; ");
         }

         builder.append("if (!(*env)->ExceptionCheck(env)) { ");
         String[] callArguments = new String[arguments.length];

         for (int index = 0; index < arguments.length; index++) {
            callArguments[index] = "jnic_argument" + index;
            builder.append("__auto_type ").append(callArguments[index]).append(" = (").append(arguments[index]).append("); ");
         }

         if (requiresReceiver(name)) {
            builder.append("if (!(*env)->ExceptionCheck(env) && jnic_argument0 == NULL) { ")
               .append("jclass jnic_exception = (*env)->FindClass(env, \"java/lang/NullPointerException\"); ")
               .append("if (jnic_exception != NULL) { (*env)->ThrowNew(env, jnic_exception, \"\"); (*env)->DeleteLocalRef(env, jnic_exception); } } ");
         }

         builder.append("if (!(*env)->ExceptionCheck(env)) { ");
         if (hasReturnValue) {
            builder.append("jnic_result = ");
         }

         builder.append(rawJniCall(name, callArguments)).append("; (*env)->ExceptionCheck(env); } } ");
         return builder.append(hasReturnValue ? "jnic_result; })" : "(void)0; })").toString();
      } else {
         return rawJniCall(name, arguments);
      }
   }

   private static String floatingLiteral(Number number) {
      double numericValue = number.doubleValue();
      if (Double.isNaN(numericValue)) {
         return "NAN";
      } else if (numericValue == Double.POSITIVE_INFINITY) {
         return "INFINITY";
      } else {
         return numericValue == Double.NEGATIVE_INFINITY ? "-INFINITY" : number.toString() + (number instanceof Float ? "f" : "");
      }
   }

   private String handleLiteral(String text, Handle handle) {
      int tag = handle.getTag();
      if (tag >= 1 && tag <= 9) {
         Type objectType = Type.getObjectType(handle.getOwner());
         String lookup = this.classCache.methodHandleLookup(text);
         String constantExpression = this.constant(text, objectType);
         if (tag == 8) {
            return this.invokeVirtual(
               "java/lang/invoke/MethodHandles$Lookup",
               "findConstructor",
               "(Ljava/lang/Class;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
               lookup,
               constantExpression,
               this.constant(text, Type.getType(handle.getDesc()))
            );
         } else {
            String constantExpression2 = this.constant(text, handle.getName());
            String constantExpression3 = this.constant(text, Type.getType(handle.getDesc()));
            if (tag <= 4) {
               String expression = "find" + (tag != 2 && tag != 4 ? "" : "Static") + (tag != 3 && tag != 4 ? "Getter" : "Setter");
               return this.invokeVirtual(
                  "java/lang/invoke/MethodHandles$Lookup",
                  expression,
                  "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;",
                  lookup,
                  constantExpression,
                  constantExpression2,
                  constantExpression3
               );
            } else {
               return tag == 7
                  ? this.invokeVirtual(
                     "java/lang/invoke/MethodHandles$Lookup",
                     "findSpecial",
                     "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;",
                     lookup,
                     constantExpression,
                     constantExpression2,
                     constantExpression3,
                     this.constant(text, objectType)
                  )
                  : this.invokeVirtual(
                     "java/lang/invoke/MethodHandles$Lookup",
                     tag == 6 ? "findStatic" : "findVirtual",
                     "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
                     lookup,
                     constantExpression,
                     constantExpression2,
                     constantExpression3
                  );
            }
         }
      } else {
         throw new AssertionError("LDC of handle " + handle);
      }
   }

   private static String rawJniCall(String name, String[] arguments) {
      StringBuilder call = new StringBuilder("(*env)->").append(name).append("(env");
      for (String argument : arguments) {
         call.append(", ").append(argument);
      }
      return call.append(')').toString();
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
