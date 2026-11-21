package dev.jnic.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "options")
public class CompilerOptions {
   public static final CompilerOptions DEFAULT = new CompilerOptions();
   @Element(name = "stringObf", required = false)
   private boolean stringObfuscation;
   @Element(name = "flowObf", required = false)
   private boolean flowObfuscation;
   @Element(name = "useIntrinsics", required = false)
   private boolean useIntrinsics;
   @Element(name = "fastCompile", required = false)
   private boolean fastCompile;

   public boolean isStringObfuscationEnabled() {
      return this.stringObfuscation;
   }

   public boolean isFlowObfuscationEnabled() {
      return this.flowObfuscation;
   }

   public boolean isIntrinsicsEnabled() {
      return this.useIntrinsics;
   }

   public boolean isFastCompileEnabled() {
      return this.fastCompile;
   }
}
