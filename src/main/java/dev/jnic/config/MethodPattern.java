package dev.jnic.config;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

@Root(name = "match")
public class MethodPattern {
   @Attribute(name = "className", required = false)
   private String className;
   private Pattern classPattern;
   @Attribute(name = "methodName", required = false)
   private String methodName;
   private Pattern methodPattern;
   @Attribute(name = "methodDesc", required = false)
   private String methodDesc;
   private Pattern descriptorPattern;
   private static final String INVALID_REGEX = "Invalid regex";

   @Commit
   private void compilePatterns() throws ConfigurationException {
      try {
         if (this.className == null) {
            if (this.methodName == null) {
               if (this.methodDesc != null) {
                  Pattern var1 = Pattern.compile(this.methodDesc);
                  this.descriptorPattern = var1;
               }
            } else {
               Pattern var3 = Pattern.compile(this.methodName);
               this.methodPattern = var3;
               if (this.methodDesc != null) {
                  Pattern var4 = Pattern.compile(this.methodDesc);
                  this.descriptorPattern = var4;
               }
            }
         } else {
            Pattern var5 = Pattern.compile(this.className);
            this.classPattern = var5;
            if (this.methodName == null) {
               if (this.methodDesc != null) {
                  Pattern var6 = Pattern.compile(this.methodDesc);
                  this.descriptorPattern = var6;
               }
            } else {
               Pattern var7 = Pattern.compile(this.methodName);
               this.methodPattern = var7;
               if (this.methodDesc != null) {
                  Pattern var8 = Pattern.compile(this.methodDesc);
                  this.descriptorPattern = var8;
               }
            }
         }
      } catch (PatternSyntaxException var10) {
         String var2 = INVALID_REGEX;
         throw new ConfigurationException(var2, var10);
      }
   }

   boolean matches(String className, String methodName, String descriptor) {
      if (this.classPattern == null) {
         if (this.methodPattern == null) {
            if (this.descriptorPattern == null) {
               return true;
            }
         } else {
            if (!this.methodPattern.matcher(methodName).matches()) {
               return false;
            }

            if (this.descriptorPattern == null) {
               return true;
            }
         }
      } else {
         if (!this.classPattern.matcher(className).matches()) {
            return false;
         }

         if (this.methodPattern == null) {
            if (this.descriptorPattern == null) {
               return true;
            }
         } else {
            if (!this.methodPattern.matcher(methodName).matches()) {
               return false;
            }

            if (this.descriptorPattern == null) {
               return true;
            }
         }
      }

      return this.descriptorPattern.matcher(descriptor).matches();
   }
}
