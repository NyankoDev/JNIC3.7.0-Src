package dev.jnic.codegen;

import dev.jnic.compiler.CompilationContext;
import java.util.Objects;
import org.objectweb.asm.Type;

public class TypeBoxing {
   public static String wrapperClass(Type type) {
      switch (type.getSort()) {
         case 0:
            return "java/lang/Void";
         case 1:
            return "java/lang/Boolean";
         case 2:
            return "java/lang/Character";
         case 3:
            return "java/lang/Byte";
         case 4:
            return "java/lang/Short";
         case 5:
            return "java/lang/Integer";
         case 6:
            return "java/lang/Float";
         case 7:
            return "java/lang/Long";
         case 8:
            return "java/lang/Double";
         case 9:
         default:
            throw new AssertionError();
         case 10:
            throw new IllegalArgumentException("Cannot box objects");
      }
   }

   public static String unbox(CompilationContext context, String value, Type type) {
      return type.getSort() >= 1 && type.getSort() <= 8
         ? context.invokeVirtual(wrapperClass(type), type.getClassName() + "Value", "()" + type.getDescriptor(), value)
         : value;
   }

   public static String box(CompilationContext context, String value, Type type) {
      if (type.getSort() >= 1 && type.getSort() <= 8) {
         String owner = wrapperClass(type);
         String descriptor = "(" + type.getDescriptor() + ")L" + owner + ";";
         return context.invokeStatic(owner, "valueOf", descriptor, value);
      } else {
         return value;
      }
   }

   public static Type constantType(Object value) {
      Objects.requireNonNull(value, "");
      if (value instanceof Boolean) {
         return Type.BOOLEAN_TYPE;
      } else if (value instanceof Character) {
         return Type.CHAR_TYPE;
      } else if (value instanceof Byte) {
         return Type.BYTE_TYPE;
      } else if (value instanceof Short) {
         return Type.SHORT_TYPE;
      } else if (value instanceof Integer) {
         return Type.INT_TYPE;
      } else if (value instanceof Float) {
         return Type.FLOAT_TYPE;
      } else if (value instanceof Long) {
         return Type.LONG_TYPE;
      } else {
         return value instanceof Double ? Type.DOUBLE_TYPE : Type.getType(value.getClass());
      }
   }
}
