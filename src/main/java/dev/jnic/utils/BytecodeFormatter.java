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
         String nullInstructionMessage = NULL_INSTRUCTION_MESSAGE;
         throw new NullPointerException(nullInstructionMessage);
      } else {
         TraceMethodVisitor visitor = VISITOR;
         instruction.accept(visitor);
         StringWriter stringWriter = new StringWriter();
         PRINTER.print(new PrintWriter(stringWriter));
         PRINTER.getText().clear();
         return stringWriter.toString().trim();
      }
   }

   static {
      Textifier textifier = new Textifier();
      PRINTER = textifier;
      VISITOR = new TraceMethodVisitor(textifier);
   }
}
