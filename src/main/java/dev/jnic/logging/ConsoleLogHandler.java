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

   void log(int level, String tag, String message, Throwable cause) {
      StringBuilder output = new StringBuilder(256);
      output.append(this.timestampFormat.format(new Date())).append(levelPrefix(level));
      if (tag != null) {
         output.append('[').append(tag).append("] ");
      }
      output.append(message);
      if (cause != null) {
         StringWriter trace = new StringWriter(256);
         cause.printStackTrace(new PrintWriter(trace));
         output.append('\n').append(trace.toString().trim());
      }
      this.printLine(output.toString());
   }

   private static String levelPrefix(int level) {
      switch (level) {
         case JnicLogger.LEVEL_TRACE:
            return " TRACE: ";
         case JnicLogger.LEVEL_DEBUG:
            return " DEBUG: ";
         case JnicLogger.LEVEL_INFO:
            return "  INFO: ";
         case JnicLogger.LEVEL_WARN:
            return "  WARN: ";
         case JnicLogger.LEVEL_ERROR:
            return " ERROR: ";
         default:
            return "";
      }
   }

   void printLine(String text) {
      System.out.println(text);
   }
}
