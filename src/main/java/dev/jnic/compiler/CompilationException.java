package dev.jnic.compiler;

public class CompilationException extends RuntimeException {
   public CompilationException(String text) {
      super(text);
   }

   public CompilationException(String text, Throwable throwable) {
      super(text, throwable);
   }
}
