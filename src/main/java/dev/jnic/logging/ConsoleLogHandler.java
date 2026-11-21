package dev.jnic.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class ConsoleLogHandler {
   private final DateFormat timestampFormat;
   final JnicLogger logger;

   ConsoleLogHandler(JnicLogger logger) {
      this.logger = logger;
      this.timestampFormat = new SimpleDateFormat("HH-mm-ss.SSS");
   }

   void log(int index, String text, String text2, Throwable throwable) {
      StringBuilder var5;
      label43: {
         JnicLogger.getState();
         var5 = new StringBuilder(256);
         String value6 = this.timestampFormat.format(new Date());
         var5.append(value6);
         if (Integer.compareUnsigned(index - 1, 4) > 0) {
            if (text == null) {
               break label43;
            }
         } else {
            switch (index - 1) {
               case 0:
                  var5.append(" TRACE: ");
                  if (text == null) {
                     break label43;
                  }
                  break;
               case 1:
                  var5.append(" DEBUG: ");
                  if (text == null) {
                     break label43;
                  }
                  break;
               case 2:
                  var5.append("  INFO: ");
                  if (text == null) {
                     break label43;
                  }
                  break;
               case 3:
                  var5.append("  WARN: ");
                  if (text == null) {
                     break label43;
                  }
                  break;
               case 4:
                  var5.append(" ERROR: ");
                  if (text == null) {
                     break label43;
                  }
                  break;
               default:
                  throw new AssertionError();
            }
         }

         var5.append('[');
         var5.append(text);
         var5.append("] ");
         var5.append(text2);
         if (throwable == null) {
            String value14 = var5.toString();
            this.printLine(value14);
            return;
         }

         StringWriter var15 = new StringWriter(256);
         PrintWriter var11 = new PrintWriter(var15);
         throwable.printStackTrace(var11);
         var5.append('\n');
         String value16 = var15.toString().trim();
         var5.append(value16);
         String value8 = var5.toString();
         this.printLine(value8);
         return;
      }

      var5.append(text2);
      if (throwable == null) {
         String value9 = var5.toString();
         this.printLine(value9);
      } else {
         StringWriter var10 = new StringWriter(256);
         PrintWriter var7 = new PrintWriter(var10);
         throwable.printStackTrace(var7);
         var5.append('\n');
         String value12 = var10.toString().trim();
         var5.append(value12);
         String value13 = var5.toString();
         this.printLine(value13);
      }
   }

   void printLine(String text) {
      System.out.println(text);
   }
}
