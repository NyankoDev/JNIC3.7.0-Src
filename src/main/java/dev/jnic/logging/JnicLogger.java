package dev.jnic.logging;

public enum JnicLogger {
   INSTANCE;

   public static final int LEVEL_NONE = 6;
   public static final int LEVEL_ERROR = 5;
   public static final int LEVEL_WARN = 4;
   public static final int LEVEL_INFO = 3;
   public static final int LEVEL_DEBUG = 2;
   public static final int LEVEL_TRACE = 1;
   private static int level = 3;
   public static boolean ERROR_ENABLED = true;
   public static boolean WARN_ENABLED = true;
   public static boolean INFO_ENABLED = true;
   public static boolean DEBUG_ENABLED = false;
   public static boolean TRACE_ENABLED = false;
   private ConsoleLogHandler handler = new ConsoleLogHandler(this);
   private static int[] state;

   public void setLevel(int level) {
      JnicLogger.level = level;
      ERROR_ENABLED = level < 6;
      WARN_ENABLED = level < 5;
      INFO_ENABLED = level < 4;
      DEBUG_ENABLED = level < 3;
      TRACE_ENABLED = level < 2;
   }

   public void disableLogging() {
      this.setLevel(6);
   }

   public void enableErrors() {
      this.setLevel(5);
   }

   public void enableWarnings() {
      this.setLevel(4);
   }

   public void enableInfo() {
      this.setLevel(3);
   }

   public void enableDebug() {
      this.setLevel(2);
   }

   public void enableTrace() {
      this.setLevel(1);
   }

   public void setHandler(ConsoleLogHandler handler) {
      this.handler = handler;
   }

   public void error(String message, Throwable cause) {
      if (ERROR_ENABLED) {
         this.handler.log(5, null, message, cause);
      }
   }

   public void error(String tag, String message, Throwable cause) {
      if (ERROR_ENABLED) {
         this.handler.log(5, tag, message, cause);
      }
   }

   public void error(String message) {
      if (ERROR_ENABLED) {
         this.handler.log(5, null, message, null);
      }
   }

   public void error(String tag, String message) {
      if (ERROR_ENABLED) {
         this.handler.log(5, tag, message, null);
      }
   }

   public void warn(String message, Throwable cause) {
      if (WARN_ENABLED) {
         this.handler.log(4, null, message, cause);
      }
   }

   public void warn(String tag, String message, Throwable cause) {
      if (WARN_ENABLED) {
         this.handler.log(4, tag, message, cause);
      }
   }

   public void warn(String message) {
      if (WARN_ENABLED) {
         this.handler.log(4, null, message, null);
      }
   }

   public void warn(String tag, String message) {
      if (WARN_ENABLED) {
         this.handler.log(4, tag, message, null);
      }
   }

   public void info(String message, Throwable cause) {
      if (INFO_ENABLED) {
         this.handler.log(3, null, message, cause);
      }
   }

   public void info(String tag, String message, Throwable cause) {
      if (INFO_ENABLED) {
         this.handler.log(3, tag, message, cause);
      }
   }

   public void info(String message) {
      if (INFO_ENABLED) {
         this.handler.log(3, null, message, null);
      }
   }

   public void info(String tag, String message) {
      if (INFO_ENABLED) {
         this.handler.log(3, tag, message, null);
      }
   }

   public void debug(String message, Throwable cause) {
      if (DEBUG_ENABLED) {
         this.handler.log(2, null, message, cause);
      }
   }

   public void debug(String tag, String message, Throwable cause) {
      if (DEBUG_ENABLED) {
         this.handler.log(2, tag, message, cause);
      }
   }

   public void debug(String message) {
      if (DEBUG_ENABLED) {
         this.handler.log(2, null, message, null);
      }
   }

   public void debug(String tag, String message) {
      if (DEBUG_ENABLED) {
         this.handler.log(2, tag, message, null);
      }
   }

   public void trace(String message, Throwable cause) {
      if (TRACE_ENABLED) {
         this.handler.log(1, null, message, cause);
      }
   }

   public void trace(String tag, String message, Throwable cause) {
      if (TRACE_ENABLED) {
         this.handler.log(1, tag, message, cause);
      }
   }

   public void trace(String message) {
      if (TRACE_ENABLED) {
         this.handler.log(1, null, message, null);
      }
   }

   public void trace(String tag, String message) {
      if (TRACE_ENABLED) {
         this.handler.log(1, tag, message, null);
      }
   }

   public static int[] getState() {
      return state;
   }

   static {
      JnicLogger[] var2 = new JnicLogger[]{INSTANCE};
   }
}
