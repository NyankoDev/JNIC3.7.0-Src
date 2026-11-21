package dev.jnic.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.util.TraceSignatureVisitor;

public final class SourceText {
   private SourceText() {
      throw new AssertionError();
   }

   public static String encodeJniName(String name) {
      StringBuilder result = new StringBuilder();

      for (int index = 0; index < name.length(); index++) {
         char character = name.charAt(index);
         switch (character) {
            case '/':
               result.append('_');
               break;
            case ';':
               result.append("_2");
               break;
            case '[':
               result.append("_3");
               break;
            case '_':
               result.append("_1");
               break;
            default:
               if ((character < 'a' || character > 'z') && (character < 'A' || character > 'Z') && (character < '0' || character > '9')) {
                  result.append("_0").append(String.format("%04x", Integer.valueOf(character)));
               } else {
                  result.append(character);
               }
         }
      }

      return result.toString();
   }

   private static boolean isJniNameCharacter(char value) {
      return value == '/' || value == '_' || 'a' <= value && value <= 'z' || 'A' <= value && value <= 'Z' || '0' <= value && value <= '9';
   }

   public static String diagnosticText(String text) {
      StringBuilder result = new StringBuilder();

      for (int index = 0; index < text.length(); index++) {
         char character = text.charAt(index);
         switch (character) {
            case '\b':
            case '\t':
            case '\n':
            case '\f':
            case '\r':
            case '"':
            case '\'':
            case '\\':
               result.append(' ');
               break;
            default:
               if (character >= ' ' && character <= '~') {
                  result.append(character);
               } else {
                  result.append('u').append(String.format("%04x", Integer.valueOf(character)));
               }
         }
      }

      return result.toString();
   }

   public static String modifiedUtf8Literal(String text) {
      try (
         ByteArrayOutputStream var1 = new ByteArrayOutputStream();
         DataOutputStream var2 = new DataOutputStream(var1);
      ) {
         var2.writeUTF(text);
         StringBuilder var3 = new StringBuilder();

         for (byte var7 : var1.toByteArray()) {
            if (var7 > 31 && var7 <= 126) {
               var3.append((char)var7);
            } else {
               var3.append(String.format("\\%03o", var7));
            }
         }

         return var3.toString();
      } catch (IOException var17) {
         throw new AssertionError();
      }
   }

   public static String methodDeclaration(int index, String text, String text2, String text3, List<String> items4) {
      StringBuilder var5 = new StringBuilder();
      if ((index & 1) != 0) {
         var5.append("public ");
      }

      if ((index & 2) != 0) {
         var5.append("private ");
      }

      if ((index & 4) != 0) {
         var5.append("protected ");
      }

      if ((index & 8) != 0) {
         var5.append("static ");
      }

      if ((index & 16) != 0) {
         var5.append("final ");
      }

      if ((index & 32) != 0) {
         var5.append("synchronized ");
      }

      if ((index & 256) != 0) {
         var5.append("native ");
      }

      if ((index & 1024) != 0) {
         var5.append("abstract ");
      }

      if ((index & 2048) != 0) {
         var5.append("strictfp ");
      }

      TraceSignatureVisitor var6 = new TraceSignatureVisitor(0);
      new SignatureReader(text3 == null ? text2 : text3).accept(var6);
      String value7 = var6.getDeclaration();
      String value8 = var6.getReturnType();
      if (value7.charAt(0) == '<') {
         var5.append(value7, 0, value7.indexOf("(")).append(' ');
      }

      if (value8.isEmpty() || value8.charAt(0) == '[') {
         var5.append("java.lang.Object");
      }

      var5.append(value8).append(' ').append(text);
      var5.append(value7, value7.indexOf(40), value7.length());
      if ((index & 128) != 0 && value7.endsWith("[])")) {
         var5.replace(var5.length() - 3, var5.length(), "...)");
      }

      String value11 = var6.getExceptions();
      if (value11 != null && !value11.isEmpty()) {
         var5.append(" throws ").append(value11);
      } else if (items4 != null && items4.size() > 0) {
         var5.append(" throws ");
         int var9 = var5.length();

         for (String var13 : items4) {
            var5.append(var13).append(", ");
         }

         var5.setLength(var5.length() - 2);

         for (int var10 = var9; var10 < var5.length(); var10++) {
            if (var5.charAt(var10) == '/') {
               var5.setCharAt(var10, '.');
            }
         }
      }

      return var5.toString();
   }

   public static String classDeclaration(int access, String name, String parent, List<String> interfaces) {
      boolean annotation = (access & 8192) != 0;
      boolean enumeration = (access & 16384) != 0;
      boolean contract = (access & 512) != 0;
      int modifiers = access & 3103;
      if (contract || enumeration) {
         modifiers &= -1041;
      }

      String prefix = Modifier.toString(modifiers);
      StringBuilder declaration = new StringBuilder(prefix);
      if (!prefix.isEmpty()) {
         declaration.append(' ');
      }

      declaration.append(annotation ? "@interface " : (enumeration ? "enum " : (contract ? "interface " : "class "))).append(name.replace('/', '.'));
      if (!contract && !enumeration && parent != null && !parent.equals("java/lang/Object")) {
         declaration.append(" extends ").append(parent.replace('/', '.'));
      }

      if (!annotation && !interfaces.isEmpty()) {
         declaration.append(contract ? " extends " : " implements ");

         for (int index = 0; index < interfaces.size(); index++) {
            if (index != 0) {
               declaration.append(", ");
            }

            declaration.append(interfaces.get(index).replace('/', '.'));
         }
      }

      return declaration.toString();
   }

   private static void appendModifiers(StringBuilder stringBuilder, int index) {
      if (index != 0) {
         int var2 = index;

         while (true) {
            label55: {
               if ((0 - var2 & var2) > 15) {
                  if ((0 - var2 & var2) == 16) {
                     stringBuilder.append("final ");
                     if (var2 - (0 - var2 & var2) == 0) {
                        break;
                     }
                     break label55;
                  }

                  if ((0 - var2 & var2) == 512) {
                     stringBuilder.append("native ");
                     if (var2 - (0 - var2 & var2) == 0) {
                        break;
                     }
                     break label55;
                  }

                  if ((0 - var2 & var2) == 1024) {
                     stringBuilder.append("abstract ");
                     if (var2 - (0 - var2 & var2) == 0) {
                        break;
                     }
                     break label55;
                  }
               } else if (Integer.compareUnsigned((0 - var2 & var2) - 1, 7) <= 0) {
                  switch ((0 - var2 & var2) - 1) {
                     case 0:
                        stringBuilder.append("public ");
                        if (var2 - (0 - var2 & var2) == 0) {
                           return;
                        }
                        break label55;
                     case 1:
                        stringBuilder.append("private ");
                        if (var2 - (0 - var2 & var2) == 0) {
                           return;
                        }
                        break label55;
                     case 2:
                     case 4:
                     case 5:
                     case 6:
                        break;
                     case 3:
                        stringBuilder.append("protected ");
                        if (var2 - (0 - var2 & var2) == 0) {
                           return;
                        }
                        break label55;
                     case 7:
                        stringBuilder.append("static ");
                        if (var2 - (0 - var2 & var2) == 0) {
                           return;
                        }
                        break label55;
                     default:
                        throw new AssertionError();
                  }
               }

               if (var2 - (0 - var2 & var2) == 0) {
                  break;
               }
            }

            var2 -= 0 - var2 & var2;
         }
      }
   }
}
