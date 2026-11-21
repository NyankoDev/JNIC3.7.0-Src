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
      StringBuilder builder;
      label43: {
         JnicLogger.getState();
         builder = new StringBuilder(256);
         String timestamp = this.timestampFormat.format(new Date());
         builder.append(timestamp);
         if (Integer.compareUnsigned(index - 1, 4) > 0) {
            if (text == null) {
               break label43;
            }
         } else {
            switch (index - 1) {
               case 0:
                  builder.append(" TRACE: ");
                  if (text == null) {
                     break label43;
                  }
                  break;
               case 1:
                  builder.append(" DEBUG: ");
                  if (text == null) {
                     break label43;
                  }
                  break;
               case 2:
                  builder.append("  INFO: ");
                  if (text == null) {
                     break label43;
                  }
                  break;
               case 3:
                  builder.append("  WARN: ");
                  if (text == null) {
                     break label43;
                  }
                  break;
               case 4:
                  builder.append(" ERROR: ");
                  if (text == null) {
                     break label43;
                  }
                  break;
               default:
                  throw new AssertionError();
            }
         }

         builder.append('[');
         builder.append(text);
         builder.append("] ");
         builder.append(text2);
         if (throwable == null) {
            String generatedSource = builder.toString();
            this.printLine(generatedSource);
            return;
         }

         StringWriter stringWriter = new StringWriter(256);
         PrintWriter printWriter = new PrintWriter(stringWriter);
         throwable.printStackTrace(printWriter);
         builder.append('\n');
         String stackTraceText = stringWriter.toString().trim();
         builder.append(stackTraceText);
         String generatedSource2 = builder.toString();
         this.printLine(generatedSource2);
         return;
      }

      builder.append(text2);
      if (throwable == null) {
         String generatedSource3 = builder.toString();
         this.printLine(generatedSource3);
      } else {
         StringWriter stringWriter2 = new StringWriter(256);
         PrintWriter printWriter2 = new PrintWriter(stringWriter2);
         throwable.printStackTrace(printWriter2);
         builder.append('\n');
         String stackTraceText2 = stringWriter2.toString().trim();
         builder.append(stackTraceText2);
         String generatedSource4 = builder.toString();
         this.printLine(generatedSource4);
      }
   }

   void printLine(String text) {
      System.out.println(text);
   }
}
