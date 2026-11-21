package dev.jnic.compiler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum TargetPlatform {
   WINDOWS_X86_64("win", "x86_64-windows", "amd64", "x86_64"),
   WINDOWS_AARCH64("win", "aarch64-windows", "aarch64"),
   MACOS_X86_64("mac", "x86_64-macos", "amd64", "x86_64"),
   MACOS_AARCH64("mac", "aarch64-macos", "aarch64"),
   LINUX_X86_64("lin", "x86_64-linux-gnu", "amd64", "x86_64"),
   LINUX_AARCH64("lin", "aarch64-linux-gnu", "aarch64");

   private final String osName;
   private final String targetTriple;
   private final Set<String> architectures;

   TargetPlatform(String osName, String targetTriple, String... values) {
      this.osName = osName;
      this.targetTriple = targetTriple;
      this.architectures = new HashSet<>(Arrays.asList(values));
   }

   public String getOsName() {
      return this.osName;
   }

   public static TargetPlatform current() {
      String value0 = System.getProperty("os.name").toLowerCase();
      String value1 = System.getProperty("os.arch").toLowerCase();
      TargetPlatform[] var2 = values();

      for (int var3 = 0; var3 < var2.length; var3++) {
         TargetPlatform var4 = var2[var3];
         if (value0.contains(var4.getOsName()) && var4.getArchitectures().contains(value1)) {
            return var4;
         }
      }

      return null;
   }

   public Set<String> getArchitectures() {
      return this.architectures;
   }

   public String getTargetTriple() {
      return this.targetTriple;
   }

   static {
      TargetPlatform[] var36 = new TargetPlatform[]{WINDOWS_X86_64, WINDOWS_AARCH64, MACOS_X86_64, MACOS_AARCH64, LINUX_X86_64, LINUX_AARCH64};
   }
}
