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
      Map functions2 = this.functions;
      String functionName = this.names.nextName();
      functions2.put((MethodReference)methodReference, (String)functionName);
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
      int typeSort = type.getSort();
      if (Integer.compareUnsigned(typeSort, 10) > 0) {
         String expression = "No full name for " + type;
         throw new IllegalArgumentException(expression);
      }

      switch (typeSort) {
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
      String[] callArguments = new String[arguments.length + 2];
      String classReference = this.classCache.classReference(owner);
      callArguments[0] = classReference;
      String methodId = this.classCache.methodId(owner, name, descriptor, true);
      callArguments[1] = methodId;
      int argumentCount = arguments.length;
      System.arraycopy((Object)arguments, 0, (Object)callArguments, 2, argumentCount);
      String typeSuffix = jniTypeSuffix(Type.getReturnType(descriptor));
      String expression = "CallStatic" + typeSuffix + "Method";
      return this.jniCall(expression, callArguments);
   }

   public String invokeNonvirtual(String owner, String name, String descriptor, String receiver, String... arguments) {
      String[] callArguments = new String[arguments.length + 3];
      callArguments[0] = receiver;
      String classReference = this.classCache.classReference(owner);
      callArguments[1] = classReference;
      String methodId = this.classCache.methodId(owner, name, descriptor, false);
      callArguments[2] = methodId;
      int argumentCount = arguments.length;
      System.arraycopy((Object)arguments, 0, (Object)callArguments, 3, argumentCount);
      String typeSuffix = jniTypeSuffix(Type.getReturnType(descriptor));
      if (typeSuffix == null) {
         String expression = "CallNonvirtual" + typeSuffix + "Method";
         return this.jniCall(expression, callArguments);
      } else {
         String expression2 = "CallNonvirtual" + typeSuffix + "Method";
         return this.jniCall(expression2, callArguments);
      }
   }

   public String invokeVirtual(String owner, String name, String descriptor, String receiver, String... arguments) {
      String[] callArguments = new String[arguments.length + 2];
      callArguments[0] = receiver;
      String methodId = this.classCache.methodId(owner, name, descriptor, false);
      callArguments[1] = methodId;
      int argumentCount = arguments.length;
      System.arraycopy((Object)arguments, 0, (Object)callArguments, 2, argumentCount);
      String typeSuffix = jniTypeSuffix(Type.getReturnType(descriptor));
      String expression = "Call" + typeSuffix + "Method";
      return this.jniCall(expression, callArguments);
   }

   public String readField(String owner, String name, String descriptor, String receiver) {
      String typeSuffix = jniTypeSuffix(Type.getType(descriptor));
      String expression = "Get" + typeSuffix + "Field";
      String[] callArguments = new String[]{receiver, null};
      String fieldId = this.classCache.fieldId(owner, name, descriptor, false);
      callArguments[1] = fieldId;
      return this.jniCall(expression, callArguments);
   }

   public String writeField(String owner, String name, String descriptor, String receiver, String value) {
      String typeSuffix = jniTypeSuffix(Type.getType(descriptor));
      String expression = "Set" + typeSuffix + "Field";
      String[] callArguments = new String[]{receiver, null, null};
      String fieldId = this.classCache.fieldId(owner, name, descriptor, false);
      callArguments[1] = fieldId;
      callArguments[2] = value;
      return this.jniCall(expression, callArguments);
   }

   public String readStaticField(String owner, String name, String descriptor) {
      String typeSuffix = jniTypeSuffix(Type.getType(descriptor));
      String expression = "GetStatic" + typeSuffix + "Field";
      String[] callArguments = new String[2];
      String classReference = this.classCache.classReference(owner);
      callArguments[0] = classReference;
      String fieldId = this.classCache.fieldId(owner, name, descriptor, true);
      callArguments[1] = fieldId;
      return this.jniCall(expression, callArguments);
   }

   public String writeStaticField(String owner, String name, String descriptor, String value) {
      String typeSuffix = jniTypeSuffix(Type.getType(descriptor));
      String expression = "SetStatic" + typeSuffix + "Field";
      String[] callArguments = new String[3];
      String classReference = this.classCache.classReference(owner);
      callArguments[0] = classReference;
      String fieldId = this.classCache.fieldId(owner, name, descriptor, true);
      callArguments[1] = fieldId;
      callArguments[2] = value;
      return this.jniCall(expression, callArguments);
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

   private static String rawJniCall(String text, String[] values) {
      StringBuilder builder = new StringBuilder("(*env)->").append(text).append("(env");
      String[] values2 = values;
      int valueCount = values.length;

      for (int index = 0; index < valueCount; index++) {
         String argument = values2[index];
         builder.append(", ").append(argument);
      }

      return builder.append(')').toString();
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
