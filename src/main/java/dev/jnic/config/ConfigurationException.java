package dev.jnic.config;

public class ConfigurationException extends Exception {
   public ConfigurationException(String text) {
      super(text);
   }

   public ConfigurationException(String text, Throwable throwable) {
      super(text, throwable);
   }
}
