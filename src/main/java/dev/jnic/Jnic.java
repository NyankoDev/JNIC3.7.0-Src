package dev.jnic;

import dev.jnic.archive.InputArchive;
import dev.jnic.archive.NativeLibrarySlice;
import dev.jnic.archive.OutputArchive;
import dev.jnic.bytecode.NativeInitializerVisitor;
import dev.jnic.bytecode.NativeMethodClassVisitor;
import dev.jnic.codegen.InstructionGenerator;
import dev.jnic.codegen.NativeSourceGenerator;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.compiler.CompilationPlan;
import dev.jnic.compiler.TargetPlatform;
import dev.jnic.config.JnicConfig;
import dev.jnic.license.License;
import dev.jnic.logging.JnicLogger;
import dev.jnic.runtime.RuntimeLoaderGenerator;
import dev.jnic.transformers.AllocationTransformer;
import dev.jnic.transformers.ArithmeticTransformer;
import dev.jnic.transformers.ArrayTransformer;
import dev.jnic.transformers.ConstantTransformer;
import dev.jnic.transformers.ConversionTransformer;
import dev.jnic.transformers.FieldTransformer;
import dev.jnic.transformers.InvokeDynamicTransformer;
import dev.jnic.transformers.InvokeTransformer;
import dev.jnic.transformers.JumpTransformer;
import dev.jnic.transformers.LdcTransformer;
import dev.jnic.transformers.LocalVariableTransformer;
import dev.jnic.transformers.MonitorTransformer;
import dev.jnic.transformers.NopTransformer;
import dev.jnic.transformers.ReturnTransformer;
import dev.jnic.transformers.StackTransformer;
import dev.jnic.transformers.SwitchTransformer;
import dev.jnic.transformers.ThrowTransformer;
import dev.jnic.transformers.TypeCheckTransformer;
import dev.jnic.transformers.UnsupportedSubroutineTransformer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.lang.module.ModuleDescriptor.Version;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.simpleframework.xml.core.Persister;
import org.tukaani.xz.FinishableOutputStream;
import org.tukaani.xz.FinishableWrapperOutputStream;
import org.tukaani.xz.LZMA2Options;

public final class Jnic {
   private static final Version MINIMUM_ZIG_VERSION = Version.parse("0.10.1");
   public static final String CHACHA_MAGIC = new String(" jnic.dev v3.7.0");
   private static License license;

   private static String formatByteSize(long value) {
      long var2 = value == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(value);
      if (var2 < 1024L) {
         return value + " B";
      }

      long var4 = var2;
      int var6 = 0;

      for (byte var7 = 40; var7 >= 0 && var2 > 1152865209611504844L >> var7; var7 -= 10) {
         var4 >>= 10;
         var6++;
      }

      long var8 = var4 * Long.signum(value);
      return String.format("%.1f %ciB", var8 / 1024.0, "KMGTPE".charAt(var6));
   }

   private static void printUsage() {
      System.out.println("java -jar jnic.jar <inputJAR> <outputJAR> <configFile> [-debug]");
   }

   public static License getLicense() {
      return license;
   }

   private static void printBanner() {
      System.out.println("\nJNIC Java Obfuscator " + Jnic.class.getPackage().getImplementationVersion());
      System.out.println(" ~ (c) +Vincent Tang 2020-2023\n\n");
   }

   private static void compileNativeLibrary(Map<TargetPlatform, Path> map, Path path, List<String> list, Path path2, Path path3, TargetPlatform targetPlatform) {
      Path var6 = ((Path)map.get(targetPlatform)).resolve("jnic.jnilib");

      try {
         ArrayList var7 = new ArrayList<>(List.of(path.toString(), "cc", "-fno-sanitize=all", "-fno-sanitize-trap=all"));
         var7.addAll(list);
         if (JnicLogger.DEBUG_ENABLED) {
            var7.add("-ftime-report");
         }

         var7.addAll(
            List.of(
               "-target",
               targetPlatform.getTargetTriple(),
               "-std=c11",
               "-fPIC",
               "-shared",
               "-s",
               "-fvisibility=hidden",
               "-I" + path2,
               "-o" + var6,
               path3.toString()
            )
         );
         int var8 = new ProcessBuilder(var7).redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start().waitFor();
         if (var8 != 0) {
            throw new IOException("Compiler failed with exit code " + var8);
         }

         debug(" -> " + targetPlatform);
      } catch (IOException | InterruptedException | RuntimeException var10) {
         failure("Failed to compile native library", var10);
         System.exit(1);
      }
   }

   private static void logCompilerWarning(String text) {
      JnicLogger.INSTANCE.info(text);
   }

   private static Path findCompiler() {
      try {
         Stream var10000 = Files.walk(Paths.get(Jnic.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent(), 3);
         Predicate<Path> function2 = path -> {
            LinkOption[] var1x = new LinkOption[0];
            return Files.isRegularFile(path, var1x);
         };
         var10000 = var10000.filter(function2);
         Predicate<Path> function3 = Files::isExecutable;
         var10000 = var10000.filter(function3);
         Predicate<Path> function4 = path -> !path.endsWith("zig") ? path.endsWith("zig.exe") : true;
         var10000 = var10000.filter(function4);
         Predicate<Path> function5 = path -> {
            try {
               Process var1x = Runtime.getRuntime().exec(new String[]{path.toString(), "version"});

               try {
                  Version var3x = Version.parse(new BufferedReader(new InputStreamReader(var1x.getInputStream())).readLine());
                  if (MINIMUM_ZIG_VERSION.compareTo(var3x) > 0) {
                     throw new RuntimeException(var3x + " older than minimum " + MINIMUM_ZIG_VERSION);
                  } else {
                     return var1x.waitFor() == 0;
                  }
               } catch (RuntimeException var4x) {
                  logCompilerWarning("Candidate " + path + ": " + var4x.getMessage());
                  return false;
               }
            } catch (IOException | InterruptedException var5x) {
               return false;
            }
         };
         Optional var1 = var10000.filter(function5).findFirst();
         if (var1.isPresent()) {
            logCompilerWarning("Found compiler: " + var1.get());
            return (Path)var1.get();
         } else {
            logCompilerWarning(new String(""));
            logCompilerWarning("Download the latest release of Zig for your platform");
            logCompilerWarning("    https://ziglang.org/download/");
            logCompilerWarning("Extract the archive into the same folder as JNIC");
            logCompilerWarning(new String(""));
            return null;
         }
      } catch (URISyntaxException | IOException var6) {
         logCompilerSearchFailure("Error searching for compiler", var6);
         return null;
      }
   }

   private static void logCompilerSearchFailure(String text, Throwable throwable) {
      JnicLogger.INSTANCE.error(text, throwable);
   }

   public static void main(String[] values) throws Exception {
      initializeTranslators();
      printBanner();
      if (values.length > 0 && "activate".equals(values[0])) {
         if (values.length == 2) {
            License.activate(values[1]);
         } else {
            System.out.println("java -jar jnic.jar activate <licenceKey>");
         }
      } else {
         license = License.defaultLicense();
         if (license == null) {
            error("No valid licence found.");
            System.out.println("Code: " + License.hardwareId());
         } else {
            info("Licence: " + license.getHolder() + " (" + license.licenseType() + ")");
            if (!license.getExpiration().equals("0000-00-00")) {
               info("Expires: " + license.getExpiration());
            }

            Path var1 = findCompiler();
            if (var1 == null) {
               error("No usable compiler found.");
               System.exit(1);
            } else if (values.length < 3) {
               printUsage();
            } else {
               if (Arrays.asList(values).contains("-debug")) {
                  JnicLogger.INSTANCE.enableDebug();
               }

               InputArchive var2;
               try {
                  info("Reading input jar " + values[0]);
                  var2 = new InputArchive(values[0]);
               } catch (Exception var11) {
                  failure("Failed to read input JAR file", var11);
                  printUsage();
                  System.exit(1);
                  return;
               }

               JnicConfig var6;
               try {
                  Persister var10000 = new Persister();
                  info("Reading configuration file " + values[2]);
                  var6 = var10000.read(JnicConfig.class, new File(values[2]));
               } catch (Exception var10) {
                  failure("Failed to read configuration file", var10);
                  printUsage();
                  System.exit(1);
                  return;
               }

               if (var6.getTargets() != null && !var6.getTargets().isEmpty()) {
                  TargetPlatform var7 = TargetPlatform.current();
                  if (!var6.getTargets().contains(var7)) {
                     warning("Current target " + var7 + " not requested.");
                     warning("The output file will not run on this system.");
                  }

                  info("Analysing classes...");
                  CompilationPlan var8 = new CompilationPlan(var2, var6);
                  Map var5 = var8.getMethods();
                  Stream var12 = var5.values().stream();
                  ToIntFunction<List> function9 = List::size;
                  info("Translating " + var12.mapToInt(function9).sum() + " methods in " + var5.size() + " classes");
                  compileApplication(values[1], var2, var6, var8, var1);
                  info("Terminating normally.");
               } else {
                  error("No targets specified. At least one target is required.");
                  info("Available targets: " + Arrays.toString(TargetPlatform.values()));
                  System.exit(1);
               }
            }
         }
      }
   }

   private static void compileApplication(String text, InputArchive inputArchive, JnicConfig jnicConfig, CompilationPlan compilationPlan, Path path) throws Exception {
      List<Path> items7 = new ArrayList();
      EnumMap var5 = new EnumMap<>(TargetPlatform.class);
      Path var8 = Files.createTempDirectory("jnic");
      debug("Temporary files will be stored in " + var8);
      copySupportFile(var8, "jni.h", items7);
      if (jnicConfig.getOptions().isFlowObfuscationEnabled() || jnicConfig.getOptions().isStringObfuscationEnabled()) {
         copySupportFile(var8, "crypto.h", items7);
      }

      for (TargetPlatform var6 : jnicConfig.getTargets()) {
         var5.put(var6, Files.createDirectory(var8.resolve(var6.toString())));
      }

      Map var16 = inputArchive.resourceEntries();
      CompilationContext var13 = new CompilationContext(jnicConfig.getOptions());
      String value10 = new NativeSourceGenerator(compilationPlan.getMethods(), var16.size() + inputArchive.getClasses().size() + 14).generate(var13);
      info("Saving translated sources");
      Path var11 = var8.resolve("jnic.c");
      items7.add(var11);

      try (BufferedWriter var17 = Files.newBufferedWriter(var11)) {
         var17.write(value10);
      }

      List var18 = List.of(jnicConfig.getOptions().isFastCompileEnabled() ? "-Os" : "-O2", "-fno-optimize-sibling-calls");
      info("Compiling sources");
      Stream var10000 = jnicConfig.getTargets().parallelStream();
      Consumer<TargetPlatform> function19 = targetPlatform -> compileNativeLibrary(var5, path, var18, var8, var11, targetPlatform);
      var10000.forEach(function19);
      if (!JnicLogger.TRACE_ENABLED) {
         Iterator var15 = items7.iterator();

         while (var15.hasNext()) {
            Files.delete((Path)var15.next());
         }
      }

      info("Finished compilation");
      prepareOutput(text, inputArchive, jnicConfig, compilationPlan, var13, var5);
   }

   private static void prepareOutput(
      String text, InputArchive inputArchive, JnicConfig jnicConfig, CompilationPlan compilationPlan, CompilationContext context, Map<TargetPlatform, Path> map
   ) throws Exception {
      LZMA2Options var6 = new LZMA2Options();
      long var7 = 0L;

      for (Path var9 : map.values()) {
         var7 += Files.size(var9.resolve("jnic.jnilib"));
      }

      int var19 = var7 > 67108864L ? 9 : (var7 > 33554432L ? 8 : (var7 > 16777216L ? 7 : 6));
      var6.setPreset(var19);
      info("Compressing native libraries (total size: " + formatByteSize(var7) + ") (preset: " + var19 + ")");
      HashMap var22 = new HashMap();
      ByteArrayOutputStream var23 = new ByteArrayOutputStream();
      long var11 = 0L;

      try (FinishableOutputStream var13 = var6.getOutputStream(new FinishableWrapperOutputStream(var23))) {
         for (Entry var15 : map.entrySet()) {
            byte[] var16 = Files.readAllBytes(((Path)var15.getValue()).resolve("jnic.jnilib"));
            var13.write(var16);
            long var17 = var11 + var16.length;
            TargetPlatform var10001 = (TargetPlatform)var15.getKey();
            jnicConfig.getOptions();
            var22.put(var10001, new NativeLibrarySlice(var11, var17));
            var11 = var17;
         }
      }

      String var24 = "dev/jnic/lib/" + UUID.randomUUID() + ".dat";
      Map var26 = inputArchive.resourceEntries();
      var26.put(var24, var23.toByteArray());
      info("Compressed size: " + formatByteSize(var23.size()) + " (ratio: " + var23.size() / (float)var7 + ")");
      info("Preparing output JAR file");
      String var32 = context.getRuntimePackage();
      String var10002 = context.getLoaderClassName();
      int var10003 = var6.getDictSize();
      byte[] var10006 = context.getLiterals().getKeystream().getNonce();
      context.getOptions();
      var26.putAll(RuntimeLoaderGenerator.generateClasses(var32, var10002, var10003, var22, var24, var10006));
      ArrayList var28 = new ArrayList();

      for (ClassReader var20 : inputArchive.getClasses()) {
         ClassNode var18 = new ClassNode();
         ClassVisitor var21 = var18;
         if (compilationPlan.getTranslatedMethods().containsClass(var20.getClassName())) {
            var21 = new NativeMethodClassVisitor(new NativeInitializerVisitor(var18, context.getLoaderClassName()), compilationPlan.getTranslatedMethods());
         }

         var20.accept(var21, 0);
         var28.add(var18);
      }

      OutputArchive var10000 = new OutputArchive(text);
      jnicConfig.stripSelectionAnnotations(var28);
      var10000.write(var28, var26);
   }

   private static void copySupportFile(Path path, String text, List<Path> list) throws IOException {
      Path var3 = path.resolve(text);

      try (InputStream var4 = Objects.requireNonNull(Jnic.class.getResourceAsStream("/" + text))) {
         Files.copy(var4, var3);
      }

      list.add(var3);
   }

   private static void initializeTranslators() {
      InstructionGenerator.TRANSFORMERS = new LinkedHashSet<>(
         Arrays.asList(
            new InvokeDynamicTransformer(),
            new FieldTransformer(),
            new JumpTransformer(),
            new ThrowTransformer(),
            new TypeCheckTransformer(),
            new MonitorTransformer(),
            new InvokeTransformer(),
            new StackTransformer(),
            new ConstantTransformer(),
            new LdcTransformer(),
            new UnsupportedSubroutineTransformer(),
            new ReturnTransformer(),
            new SwitchTransformer(),
            new LocalVariableTransformer(),
            new ConversionTransformer(),
            new NopTransformer(),
            new AllocationTransformer(),
            new ArrayTransformer(),
            new ArithmeticTransformer()
         )
      );
   }

   private static void info(String text) {
      JnicLogger.INSTANCE.info(text);
   }

   private static void debug(String text) {
      JnicLogger.INSTANCE.debug(text);
   }

   private static void error(String text) {
      JnicLogger.INSTANCE.error(text);
   }

   private static void warning(String text) {
      JnicLogger.INSTANCE.warn(text);
   }

   private static void failure(String text, Throwable throwable) {
      JnicLogger.INSTANCE.error(text, throwable);
   }
}
