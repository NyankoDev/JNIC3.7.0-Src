package dev.jnic.utils;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.IntInsnNode;

public final class JniTypes {
   public static final Type OBJECT_TYPE = Type.getObjectType(new String("java/lang/Object"));

   private JniTypes() {
      throw new AssertionError();
   }

   public static String nativeType(Type type) {
      int typeSort = type.getSort();
      if (typeSort == 10) {
         return "jobject";
      }

      if (typeSort != 9) {
         int typeSort2 = type.getSort();
         if (Integer.compareUnsigned(typeSort2, 8) > 0) {
            throw new IllegalArgumentException("Type sort is invalid");
         }

         switch (typeSort2) {
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
         int typeSort3 = type.getElementType().getSort();
         if (9 != typeSort3) {
            int typeSort4 = type.getElementType().getSort();
            if (10 != typeSort4) {
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
         String typeSuffix = jniTypeSuffix(type);
         return typeSuffix == null ? typeSuffix.toLowerCase() : typeSuffix.toLowerCase();
      } else {
         return type.getInternalName().replaceAll(new String("/"), new String("."));
      }
   }

   static String valueSlot(Type type) {
      if (type.getSort() != 11) {
         int typeSort = type.getSort();
         if (typeSort != 0) {
            int typeSort2 = type.getSort();
            if (Integer.compareUnsigned(typeSort2 - 1, 9) > 0) {
               throw new AssertionError();
            }

            switch (typeSort2 - 1) {
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
         int typeSort = type.getSort();
         if (Integer.compareUnsigned(typeSort, 10) <= 0) {
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
            }
         }

         throw new AssertionError();
      } else {
         throw new IllegalArgumentException("Type sort is invalid");
      }
   }

   public static Type newArrayType(IntInsnNode intInsnNode) {
      int elementTypeCode = intInsnNode.operand;
      if (Integer.compareUnsigned(elementTypeCode + -4, 7) > 0) {
         throw new IllegalArgumentException("Not a suitable argument to NEWARRAY");
      }

      switch (elementTypeCode + -4) {
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
         String slotName = stackSlot(type);
         return slotName == null ? text + index + "." + slotName : text + index + "." + slotName;
      } else {
         throw new AssertionError();
      }
   }

   public static String valueVariable(String text, int index, Type type) {
      if (index >= 0) {
         String slotName = valueSlot(type);
         return text + index + "." + slotName;
      } else {
         throw new AssertionError();
      }
   }
}
