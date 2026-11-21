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
                  Pattern pattern = Pattern.compile(this.methodDesc);
                  this.descriptorPattern = pattern;
               }
            } else {
               Pattern pattern2 = Pattern.compile(this.methodName);
               this.methodPattern = pattern2;
               if (this.methodDesc != null) {
                  Pattern pattern3 = Pattern.compile(this.methodDesc);
                  this.descriptorPattern = pattern3;
               }
            }
         } else {
            Pattern pattern4 = Pattern.compile(this.className);
            this.classPattern = pattern4;
            if (this.methodName == null) {
               if (this.methodDesc != null) {
                  Pattern pattern5 = Pattern.compile(this.methodDesc);
                  this.descriptorPattern = pattern5;
               }
            } else {
               Pattern pattern6 = Pattern.compile(this.methodName);
               this.methodPattern = pattern6;
               if (this.methodDesc != null) {
                  Pattern pattern7 = Pattern.compile(this.methodDesc);
                  this.descriptorPattern = pattern7;
               }
            }
         }
      } catch (PatternSyntaxException patternSyntaxFailure) {
         String invalidRegex = INVALID_REGEX;
         throw new ConfigurationException(invalidRegex, patternSyntaxFailure);
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
