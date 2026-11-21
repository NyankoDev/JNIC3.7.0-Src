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
      long absoluteSize = value == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(value);
      if (absoluteSize < 1024L) {
         return value + " B";
      }

      long scaledSize = absoluteSize;
      int unitIndex = 0;

      for (byte index = 40; index >= 0 && absoluteSize > 1152865209611504844L >> index; index -= 10) {
         scaledSize >>= 10;
         unitIndex++;
      }

      long signedSize = scaledSize * Long.signum(value);
      return String.format("%.1f %ciB", signedSize / 1024.0, "KMGTPE".charAt(unitIndex));
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

   private static void compileNativeLibrary(Map<TargetPlatform, Path> targetDirectories, Path compiler, List<String> compilerOptions, Path includeDirectory, Path sourceFile, TargetPlatform target) {
      Path libraryFile = ((Path)targetDirectories.get(target)).resolve("jnic.jnilib");

      try {
         ArrayList command = new ArrayList<>(List.of(compiler.toString(), "cc", "-fno-sanitize=all", "-fno-sanitize-trap=all"));
         command.addAll(compilerOptions);
         if (JnicLogger.DEBUG_ENABLED) {
            command.add("-ftime-report");
         }

         command.addAll(
            List.of(
               "-target",
               target.getTargetTriple(),
               "-std=c11",
               "-fPIC",
               "-shared",
               "-s",
               "-fvisibility=hidden",
               "-I" + includeDirectory,
               "-o" + libraryFile,
               sourceFile.toString()
            )
         );
         int exitCode = new ProcessBuilder(command).redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start().waitFor();
         if (exitCode != 0) {
            throw new IOException("Compiler failed with exit code " + exitCode);
         }

         debug(" -> " + target);
      } catch (IOException | InterruptedException | RuntimeException failure) {
         failure("Failed to compile native library", failure);
         System.exit(1);
      }
   }

   private static void logCompilerWarning(String text) {
      JnicLogger.INSTANCE.info(text);
   }

   private static Path findCompiler() {
      try {
         Stream compilerCandidates = Files.walk(Paths.get(Jnic.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent(), 3);
         Predicate<Path> regularFiles = path -> {
            LinkOption[] linkOptions = new LinkOption[0];
            return Files.isRegularFile(path, linkOptions);
         };
         compilerCandidates = compilerCandidates.filter(regularFiles);
         Predicate<Path> executableFiles = Files::isExecutable;
         compilerCandidates = compilerCandidates.filter(executableFiles);
         Predicate<Path> compilerNames = path -> !path.endsWith("zig") ? path.endsWith("zig.exe") : true;
         compilerCandidates = compilerCandidates.filter(compilerNames);
         Predicate<Path> supportedCompilers = path -> {
            try {
               Process compilerProcess = Runtime.getRuntime().exec(new String[]{path.toString(), "version"});

               try {
                  Version compilerVersion = Version.parse(new BufferedReader(new InputStreamReader(compilerProcess.getInputStream())).readLine());
                  if (MINIMUM_ZIG_VERSION.compareTo(compilerVersion) > 0) {
                     throw new RuntimeException(compilerVersion + " older than minimum " + MINIMUM_ZIG_VERSION);
                  } else {
                     return compilerProcess.waitFor() == 0;
                  }
               } catch (RuntimeException versionFailure) {
                  logCompilerWarning("Candidate " + path + ": " + versionFailure.getMessage());
                  return false;
               }
            } catch (IOException | InterruptedException processFailure) {
               return false;
            }
         };
         Optional compiler = compilerCandidates.filter(supportedCompilers).findFirst();
         if (compiler.isPresent()) {
            logCompilerWarning("Found compiler: " + compiler.get());
            return (Path)compiler.get();
         } else {
            logCompilerWarning(new String(""));
            logCompilerWarning("Download the latest release of Zig for your platform");
            logCompilerWarning("    https://ziglang.org/download/");
            logCompilerWarning("Extract the archive into the same folder as JNIC");
            logCompilerWarning(new String(""));
            return null;
         }
      } catch (URISyntaxException | IOException searchFailure) {
         logCompilerSearchFailure("Error searching for compiler", searchFailure);
         return null;
      }
   }

   private static void logCompilerSearchFailure(String text, Throwable throwable) {
      JnicLogger.INSTANCE.error(text, throwable);
   }

   public static void main(String[] arguments) throws Exception {
      initializeTranslators();
      printBanner();
      if (arguments.length > 0 && "activate".equals(arguments[0])) {
         if (arguments.length == 2) {
            License.activate(arguments[1]);
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

            Path path = findCompiler();
            if (path == null) {
               error("No usable compiler found.");
               System.exit(1);
            } else if (arguments.length < 3) {
               printUsage();
            } else {
               if (Arrays.asList(arguments).contains("-debug")) {
                  JnicLogger.INSTANCE.enableDebug();
               }

               InputArchive inputArchive;
               try {
                  info("Reading input jar " + arguments[0]);
                  inputArchive = new InputArchive(arguments[0]);
               } catch (Exception failure) {
                  failure("Failed to read input JAR file", failure);
                  printUsage();
                  System.exit(1);
                  return;
               }

               JnicConfig jnicConfig;
               try {
                  Persister persister = new Persister();
                  info("Reading configuration file " + arguments[2]);
                  jnicConfig = persister.read(JnicConfig.class, new File(arguments[2]));
               } catch (Exception failure2) {
                  failure("Failed to read configuration file", failure2);
                  printUsage();
                  System.exit(1);
                  return;
               }

               if (jnicConfig.getTargets() != null && !jnicConfig.getTargets().isEmpty()) {
                  TargetPlatform random = TargetPlatform.current();
                  if (!jnicConfig.getTargets().contains(random)) {
                     warning("Current target " + random + " not requested.");
                     warning("The output file will not run on this system.");
                  }

                  info("Analysing classes...");
                  CompilationPlan compilationPlan = new CompilationPlan(inputArchive, jnicConfig);
                  Map methods = compilationPlan.getMethods();
                  Stream stream = methods.values().stream();
                  ToIntFunction<List> sizeMapper = List::size;
                  info("Translating " + stream.mapToInt(sizeMapper).sum() + " methods in " + methods.size() + " classes");
                  compileApplication(arguments[1], inputArchive, jnicConfig, compilationPlan, path);
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

   private static void compileApplication(String outputFile, InputArchive inputArchive, JnicConfig config, CompilationPlan plan, Path compiler) throws Exception {
      List<Path> temporaryFiles = new ArrayList();
      EnumMap targetDirectories = new EnumMap<>(TargetPlatform.class);
      Path temporaryDirectory = Files.createTempDirectory("jnic");
      debug("Temporary files will be stored in " + temporaryDirectory);
      copySupportFile(temporaryDirectory, "jni.h", temporaryFiles);
      if (config.getOptions().isFlowObfuscationEnabled() || config.getOptions().isStringObfuscationEnabled()) {
         copySupportFile(temporaryDirectory, "crypto.h", temporaryFiles);
      }

      for (TargetPlatform target : config.getTargets()) {
         targetDirectories.put(target, Files.createDirectory(temporaryDirectory.resolve(target.toString())));
      }

      Map resources = inputArchive.resourceEntries();
      CompilationContext context = new CompilationContext(config.getOptions());
      String generatedSource = new NativeSourceGenerator(plan.getMethods(), resources.size() + inputArchive.getClasses().size() + 14).generate(context);
      info("Saving translated sources");
      Path sourceFile = temporaryDirectory.resolve("jnic.c");
      temporaryFiles.add(sourceFile);

      try (BufferedWriter bufferedWriter = Files.newBufferedWriter(sourceFile)) {
         bufferedWriter.write(generatedSource);
      }

      List compilerOptions = List.of(config.getOptions().isFastCompileEnabled() ? "-Os" : "-O2", "-fno-optimize-sibling-calls");
      info("Compiling sources");
      Stream targets = config.getTargets().parallelStream();
      Consumer<TargetPlatform> compileTarget = targetPlatform -> compileNativeLibrary(targetDirectories, compiler, compilerOptions, temporaryDirectory, sourceFile, targetPlatform);
      targets.forEach(compileTarget);
      if (!JnicLogger.TRACE_ENABLED) {
         Iterator iterator = temporaryFiles.iterator();

         while (iterator.hasNext()) {
            Files.delete((Path)iterator.next());
         }
      }

      info("Finished compilation");
      prepareOutput(outputFile, inputArchive, config, plan, context, targetDirectories);
   }

   private static void prepareOutput(
      String outputFile, InputArchive inputArchive, JnicConfig config, CompilationPlan plan, CompilationContext context, Map<TargetPlatform, Path> targetDirectories
   ) throws Exception {
      LZMA2Options compressionOptions = new LZMA2Options();
      long totalLibrarySize = 0L;

      for (Path libraryDirectory : targetDirectories.values()) {
         totalLibrarySize += Files.size(libraryDirectory.resolve("jnic.jnilib"));
      }

      int compressionPreset = totalLibrarySize > 67108864L ? 9 : (totalLibrarySize > 33554432L ? 8 : (totalLibrarySize > 16777216L ? 7 : 6));
      compressionOptions.setPreset(compressionPreset);
      info("Compressing native libraries (total size: " + formatByteSize(totalLibrarySize) + ") (preset: " + compressionPreset + ")");
      HashMap librarySlices = new HashMap();
      ByteArrayOutputStream compressedLibraries = new ByteArrayOutputStream();
      long libraryOffset = 0L;

      try (FinishableOutputStream outputStream = compressionOptions.getOutputStream(new FinishableWrapperOutputStream(compressedLibraries))) {
         for (Entry entry : targetDirectories.entrySet()) {
            byte[] bytes = Files.readAllBytes(((Path)entry.getValue()).resolve("jnic.jnilib"));
            outputStream.write(bytes);
            long endOffset = libraryOffset + bytes.length;
            TargetPlatform targetPlatform = (TargetPlatform)entry.getKey();
            config.getOptions();
            librarySlices.put(targetPlatform, new NativeLibrarySlice(libraryOffset, endOffset));
            libraryOffset = endOffset;
         }
      }

      String resourceName = "dev/jnic/lib/" + UUID.randomUUID() + ".dat";
      Map resources = inputArchive.resourceEntries();
      resources.put(resourceName, compressedLibraries.toByteArray());
      info("Compressed size: " + formatByteSize(compressedLibraries.size()) + " (ratio: " + compressedLibraries.size() / (float)totalLibrarySize + ")");
      info("Preparing output JAR file");
      String runtimePackage = context.getRuntimePackage();
      String loaderClassName = context.getLoaderClassName();
      int dictSize = compressionOptions.getDictSize();
      byte[] nonce = context.getLiterals().getKeystream().getNonce();
      context.getOptions();
      resources.putAll(RuntimeLoaderGenerator.generateClasses(runtimePackage, loaderClassName, dictSize, librarySlices, resourceName, nonce));
      ArrayList outputClasses = new ArrayList();

      for (ClassReader reader : inputArchive.getClasses()) {
         ClassNode classNode = new ClassNode();
         ClassVisitor visitor = classNode;
         if (plan.getTranslatedMethods().containsClass(reader.getClassName())) {
            visitor = new NativeMethodClassVisitor(new NativeInitializerVisitor(classNode, context.getLoaderClassName()), plan.getTranslatedMethods());
         }

         reader.accept(visitor, 0);
         outputClasses.add(classNode);
      }

      OutputArchive outputArchive = new OutputArchive(outputFile);
      config.stripSelectionAnnotations(outputClasses);
      outputArchive.write(outputClasses, resources);
   }

   private static void copySupportFile(Path directory, String resourceName, List<Path> temporaryFiles) throws IOException {
      Path resolvedPath = directory.resolve(resourceName);

      try (InputStream input = Objects.requireNonNull(Jnic.class.getResourceAsStream("/" + resourceName))) {
         Files.copy(input, resolvedPath);
      }

      temporaryFiles.add(resolvedPath);
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
