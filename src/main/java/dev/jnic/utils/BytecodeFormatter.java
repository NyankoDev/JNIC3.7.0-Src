package dev.jnic.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class BytecodeFormatter {
   private static final Printer PRINTER;
   private static final TraceMethodVisitor VISITOR;
   private static final String NULL_INSTRUCTION_MESSAGE = "Instruction argument cannot be null";

   public static String format(AbstractInsnNode instruction) {
      if (instruction == null) {
         String var3 = NULL_INSTRUCTION_MESSAGE;
         throw new NullPointerException(var3);
      } else {
         TraceMethodVisitor var7 = VISITOR;
         instruction.accept(var7);
         StringWriter var6 = new StringWriter();
         PRINTER.print(new PrintWriter(var6));
         PRINTER.getText().clear();
         return var6.toString().trim();
      }
   }

   static {
      Textifier var1 = new Textifier();
      PRINTER = var1;
      VISITOR = new TraceMethodVisitor(var1);
   }
}
