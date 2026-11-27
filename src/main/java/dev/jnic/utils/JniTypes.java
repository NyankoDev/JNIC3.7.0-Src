package dev.jnic.utils;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.IntInsnNode;

public final class JniTypes {
   public static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");

   private JniTypes() {
      throw new AssertionError();
   }

   public static String nativeType(Type type) {
      switch (type.getSort()) {
         case Type.VOID:
            return "void";
         case Type.BOOLEAN:
            return "jboolean";
         case Type.CHAR:
            return "jchar";
         case Type.BYTE:
            return "jbyte";
         case Type.SHORT:
            return "jshort";
         case Type.INT:
            return "jint";
         case Type.FLOAT:
            return "jfloat";
         case Type.LONG:
            return "jlong";
         case Type.DOUBLE:
            return "jdouble";
         case Type.OBJECT:
            return "jobject";
         case Type.ARRAY:
            Type elementType = type.getElementType();
            return elementType.getSort() == Type.OBJECT || elementType.getSort() == Type.ARRAY
               ? "jobjectArray" : nativeType(elementType) + "Array";
         default:
            throw new IllegalArgumentException("Type sort is invalid");
      }
   }

   public static String stackSlot(Type type) {
      switch (type.getSort()) {
         case Type.BOOLEAN:
         case Type.CHAR:
         case Type.BYTE:
         case Type.SHORT:
            return valueSlot(Type.INT_TYPE);
         default:
            return valueSlot(type);
      }
   }

   public static String javaTypeName(Type type) {
      return type.getSort() == Type.OBJECT
         ? type.getInternalName().replace('/', '.') : jniTypeSuffix(type).toLowerCase();
   }

   static String valueSlot(Type type) {
      switch (type.getSort()) {
         case Type.BOOLEAN:
            return "z";
         case Type.CHAR:
            return "c";
         case Type.BYTE:
            return "b";
         case Type.SHORT:
            return "s";
         case Type.INT:
            return "i";
         case Type.FLOAT:
            return "f";
         case Type.LONG:
            return "j";
         case Type.DOUBLE:
            return "d";
         case Type.ARRAY:
         case Type.OBJECT:
            return "l";
         case Type.VOID:
         case Type.METHOD:
            throw new IllegalArgumentException("Type sort is invalid");
         default:
            throw new AssertionError();
      }
   }

   public static String jniTypeSuffix(Type type) {
      switch (type.getSort()) {
         case Type.VOID:
            return "Void";
         case Type.BOOLEAN:
            return "Boolean";
         case Type.CHAR:
            return "Char";
         case Type.BYTE:
            return "Byte";
         case Type.SHORT:
            return "Short";
         case Type.INT:
            return "Int";
         case Type.FLOAT:
            return "Float";
         case Type.LONG:
            return "Long";
         case Type.DOUBLE:
            return "Double";
         case Type.ARRAY:
         case Type.OBJECT:
            return "Object";
         case Type.METHOD:
            throw new IllegalArgumentException("Type sort is invalid");
         default:
            throw new AssertionError();
      }
   }

   public static Type newArrayType(IntInsnNode instruction) {
      switch (instruction.operand) {
         case T_BOOLEAN:
            return Type.BOOLEAN_TYPE;
         case T_CHAR:
            return Type.CHAR_TYPE;
         case T_FLOAT:
            return Type.FLOAT_TYPE;
         case T_DOUBLE:
            return Type.DOUBLE_TYPE;
         case T_BYTE:
            return Type.BYTE_TYPE;
         case T_SHORT:
            return Type.SHORT_TYPE;
         case T_INT:
            return Type.INT_TYPE;
         case T_LONG:
            return Type.LONG_TYPE;
         default:
            throw new IllegalArgumentException("Not a suitable argument to NEWARRAY");
      }
   }

   public static String stackVariable(String prefix, int index, Type type) {
      if (index < 0) {
         throw new AssertionError();
      }
      return prefix + index + "." + stackSlot(type);
   }

   public static String valueVariable(String prefix, int index, Type type) {
      if (index < 0) {
         throw new AssertionError();
      }
      return prefix + index + "." + valueSlot(type);
   }
}
