package dev.jnic.utils;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.IntInsnNode;

public final class JniTypes {
   public static final Type OBJECT_TYPE = Type.getObjectType(new String("java/lang/Object"));

   private JniTypes() {
      throw new AssertionError();
   }

   public static String nativeType(Type type) {
      int var1 = type.getSort();
      if (var1 == 10) {
         return "jobject";
      }

      if (var1 != 9) {
         int var2 = type.getSort();
         if (Integer.compareUnsigned(var2, 8) > 0) {
            throw new IllegalArgumentException("Type sort is invalid");
         }

         switch (var2) {
            case 0:
               return "void";
            case 1:
               return "jboolean";
            case 2:
               return "jchar";
            case 3:
               return "jbyte";
            case 4:
               return "jshort";
            case 5:
               return "jint";
            case 6:
               return "jfloat";
            case 7:
               return "jlong";
            case 8:
               return "jdouble";
            default:
               throw new AssertionError();
         }
      } else {
         int var3 = type.getElementType().getSort();
         if (9 != var3) {
            int var4 = type.getElementType().getSort();
            if (10 != var4) {
               return nativeType(type.getElementType()) + "Array";
            }
         }

         return "jobjectArray";
      }
   }

   public static String stackSlot(Type type) {
      if (type.getSort() != 1) {
         if (type.getSort() != 4) {
            if (type.getSort() != 3) {
               return type.getSort() != 2 ? valueSlot(type) : valueSlot(Type.INT_TYPE);
            } else {
               return valueSlot(Type.INT_TYPE);
            }
         } else {
            return valueSlot(Type.INT_TYPE);
         }
      } else {
         return valueSlot(Type.INT_TYPE);
      }
   }

   public static String javaTypeName(Type type) {
      if (type.getSort() != 10) {
         String value5 = jniTypeSuffix(type);
         return value5 == null ? value5.toLowerCase() : value5.toLowerCase();
      } else {
         return type.getInternalName().replaceAll(new String("/"), new String("."));
      }
   }

   static String valueSlot(Type type) {
      if (type.getSort() != 11) {
         int var1 = type.getSort();
         if (var1 != 0) {
            int var2 = type.getSort();
            if (Integer.compareUnsigned(var2 - 1, 9) > 0) {
               throw new AssertionError();
            }

            switch (var2 - 1) {
               case 0:
                  return new String("z");
               case 1:
                  return new String("c");
               case 2:
                  return new String("b");
               case 3:
                  return new String("s");
               case 4:
                  return new String("i");
               case 5:
                  return new String("f");
               case 6:
                  return new String("j");
               case 7:
                  return new String("d");
               case 8:
               case 9:
                  return new String("l");
               default:
                  throw new AssertionError();
            }
         }
      }

      throw new IllegalArgumentException("Type sort is invalid");
   }

   public static String jniTypeSuffix(Type type) {
      if (type.getSort() != 11) {
         int var1 = type.getSort();
         if (Integer.compareUnsigned(var1, 10) <= 0) {
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
            }
         }

         throw new AssertionError();
      } else {
         throw new IllegalArgumentException("Type sort is invalid");
      }
   }

   public static Type newArrayType(IntInsnNode intInsnNode) {
      int var1 = intInsnNode.operand;
      if (Integer.compareUnsigned(var1 + -4, 7) > 0) {
         throw new IllegalArgumentException("Not a suitable argument to NEWARRAY");
      }

      switch (var1 + -4) {
         case 0:
            return Type.BOOLEAN_TYPE;
         case 1:
            return Type.CHAR_TYPE;
         case 2:
            return Type.FLOAT_TYPE;
         case 3:
            return Type.DOUBLE_TYPE;
         case 4:
            return Type.BYTE_TYPE;
         case 5:
            return Type.SHORT_TYPE;
         case 6:
            return Type.INT_TYPE;
         case 7:
            return Type.LONG_TYPE;
         default:
            throw new AssertionError();
      }
   }

   public static String stackVariable(String text, int index, Type type) {
      if (index >= 0) {
         String value3 = stackSlot(type);
         return value3 == null ? text + index + "." + value3 : text + index + "." + value3;
      } else {
         throw new AssertionError();
      }
   }

   public static String valueVariable(String text, int index, Type type) {
      if (index >= 0) {
         String value3 = valueSlot(type);
         return text + index + "." + value3;
      } else {
         throw new AssertionError();
      }
   }
}
