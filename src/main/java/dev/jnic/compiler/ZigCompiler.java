package dev.jnic.compiler;

import dev.jnic.logging.JnicLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.module.ModuleDescriptor.Version;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ZigCompiler {
   private static final Version MINIMUM_VERSION = Version.parse("0.10.1");
   private static final String EXECUTABLE_NAME = File.separatorChar == '\\' ? "zig.exe" : "zig";
   private final Path executable;
   private final Version version;

   private ZigCompiler(Path executable, Version version) {
      this.executable = executable;
      this.version = version;
   }

   public Path getExecutable() {
      return this.executable;
   }

   public Version getVersion() {
      return this.version;
   }

   public static ZigCompiler find(Path directory) throws IOException, InterruptedException {
      String configuredCompiler = System.getenv("JNIC_ZIG");
      if (configuredCompiler != null && !configuredCompiler.isBlank()) {
         Path configuredPath = Paths.get(configuredCompiler);
         if (Files.isDirectory(configuredPath)) {
            configuredPath = configuredPath.resolve(EXECUTABLE_NAME);
         }
         return inspect(configuredPath);
      }

      Set<Path> candidates = new LinkedHashSet<>();
      if (Files.isDirectory(directory)) {
         try (Stream<Path> files = Files.find(directory, 3,
            (path, attributes) -> attributes.isRegularFile() && path.getFileName().toString().equals(EXECUTABLE_NAME))) {
            files.map(path -> path.toAbsolutePath().normalize()).forEach(candidates::add);
         }
      }

      String searchPath = System.getenv("PATH");
      if (searchPath != null) {
         for (String entry : searchPath.split(Pattern.quote(File.pathSeparator))) {
            if (!entry.isBlank()) {
               try {
                  Path candidate = Paths.get(entry).resolve(EXECUTABLE_NAME).toAbsolutePath().normalize();
                  if (Files.isRegularFile(candidate)) {
                     candidates.add(candidate);
                  }
               } catch (InvalidPathException failure) {
                  JnicLogger.INSTANCE.warn("Ignoring invalid PATH entry: " + failure.getReason());
               }
            }
         }
      }

      ZigCompiler newest = null;
      Set<Path> inspected = new LinkedHashSet<>();
      for (Path candidate : candidates) {
         try {
            Path executable = candidate.toRealPath();
            if (!inspected.add(executable)) {
               continue;
            }
            ZigCompiler compiler = inspect(executable);
            if (newest == null || compiler.version.compareTo(newest.version) > 0) {
               newest = compiler;
            }
         } catch (IOException | IllegalArgumentException failure) {
            JnicLogger.INSTANCE.warn("Candidate " + candidate + ": " + failure.getMessage());
         }
      }
      return newest;
   }

   private static ZigCompiler inspect(Path executable) throws IOException, InterruptedException {
      executable = executable.toAbsolutePath().normalize();
      if (!Files.isRegularFile(executable) || !Files.isExecutable(executable)) {
         throw new IOException("Zig executable not found: " + executable);
      }
      Process process = new ProcessBuilder(executable.toString(), "version").redirectErrorStream(true).start();
      try {
         if (!process.waitFor(10, TimeUnit.SECONDS)) {
            throw new IOException("Timed out reading Zig version: " + executable);
         }
         String output;
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            output = reader.readLine();
         }
         if (process.exitValue() != 0 || output == null || output.isBlank()) {
            throw new IOException("Failed to read Zig version: " + executable);
         }
         Version version = Version.parse(output.trim());
         if (version.compareTo(MINIMUM_VERSION) < 0) {
            throw new IOException("Zig " + version + " is older than minimum " + MINIMUM_VERSION);
         }
         return new ZigCompiler(executable, version);
      } finally {
         if (process.isAlive()) {
            process.destroyForcibly();
         }
      }
   }
}
