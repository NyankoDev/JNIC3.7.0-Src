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
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
      ) {
         dataOutputStream.writeUTF(text);
         StringBuilder builder = new StringBuilder();

         for (byte byteValue : byteArrayOutputStream.toByteArray()) {
            if (byteValue > 31 && byteValue <= 126) {
               builder.append((char)byteValue);
            } else {
               builder.append(String.format("\\%03o", byteValue));
            }
         }

         return builder.toString();
      } catch (IOException ioFailure) {
         throw new AssertionError();
      }
   }

   public static String methodDeclaration(int access, String methodName, String descriptor, String signature, List<String> exceptions) {
      StringBuilder builder = new StringBuilder();
      if ((access & 1) != 0) {
         builder.append("public ");
      }

      if ((access & 2) != 0) {
         builder.append("private ");
      }

      if ((access & 4) != 0) {
         builder.append("protected ");
      }

      if ((access & 8) != 0) {
         builder.append("static ");
      }

      if ((access & 16) != 0) {
         builder.append("final ");
      }

      if ((access & 32) != 0) {
         builder.append("synchronized ");
      }

      if ((access & 256) != 0) {
         builder.append("native ");
      }

      if ((access & 1024) != 0) {
         builder.append("abstract ");
      }

      if ((access & 2048) != 0) {
         builder.append("strictfp ");
      }

      TraceSignatureVisitor traceSignatureVisitor = new TraceSignatureVisitor(0);
      new SignatureReader(signature == null ? descriptor : signature).accept(traceSignatureVisitor);
      String declaration = traceSignatureVisitor.getDeclaration();
      String returnType = traceSignatureVisitor.getReturnType();
      if (declaration.charAt(0) == '<') {
         builder.append(declaration, 0, declaration.indexOf("(")).append(' ');
      }

      if (returnType.isEmpty() || returnType.charAt(0) == '[') {
         builder.append("java.lang.Object");
      }

      builder.append(returnType).append(' ').append(methodName);
      builder.append(declaration, declaration.indexOf(40), declaration.length());
      if ((access & 128) != 0 && declaration.endsWith("[])")) {
         builder.replace(builder.length() - 3, builder.length(), "...)");
      }

      String exceptions2 = traceSignatureVisitor.getExceptions();
      if (exceptions2 != null && !exceptions2.isEmpty()) {
         builder.append(" throws ").append(exceptions2);
      } else if (exceptions != null && exceptions.size() > 0) {
         builder.append(" throws ");
         int exceptionsOffset = builder.length();

         for (String exceptionType : exceptions) {
            builder.append(exceptionType).append(", ");
         }

         builder.setLength(builder.length() - 2);

         for (int exceptionsOffset2 = exceptionsOffset; exceptionsOffset2 < builder.length(); exceptionsOffset2++) {
            if (builder.charAt(exceptionsOffset2) == '/') {
               builder.setCharAt(exceptionsOffset2, '.');
            }
         }
      }

      return builder.toString();
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
         int index2 = index;

         while (true) {
            label55: {
               if ((0 - index2 & index2) > 15) {
                  if ((0 - index2 & index2) == 16) {
                     stringBuilder.append("final ");
                     if (index2 - (0 - index2 & index2) == 0) {
                        break;
                     }
                     break label55;
                  }

                  if ((0 - index2 & index2) == 512) {
                     stringBuilder.append("native ");
                     if (index2 - (0 - index2 & index2) == 0) {
                        break;
                     }
                     break label55;
                  }

                  if ((0 - index2 & index2) == 1024) {
                     stringBuilder.append("abstract ");
                     if (index2 - (0 - index2 & index2) == 0) {
                        break;
                     }
                     break label55;
                  }
               } else if (Integer.compareUnsigned((0 - index2 & index2) - 1, 7) <= 0) {
                  switch ((0 - index2 & index2) - 1) {
                     case 0:
                        stringBuilder.append("public ");
                        if (index2 - (0 - index2 & index2) == 0) {
                           return;
                        }
                        break label55;
                     case 1:
                        stringBuilder.append("private ");
                        if (index2 - (0 - index2 & index2) == 0) {
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
                        if (index2 - (0 - index2 & index2) == 0) {
                           return;
                        }
                        break label55;
                     case 7:
                        stringBuilder.append("static ");
                        if (index2 - (0 - index2 & index2) == 0) {
                           return;
                        }
                        break label55;
                     default:
                        throw new AssertionError();
                  }
               }

               if (index2 - (0 - index2 & index2) == 0) {
                  break;
               }
            }

            index2 -= 0 - index2 & index2;
         }
      }
   }
}
