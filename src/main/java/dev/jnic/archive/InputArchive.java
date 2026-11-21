package dev.jnic.archive;

import dev.jnic.logging.JnicLogger;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;

public class InputArchive {
   private final Collection<ClassReader> classes = new ArrayDeque<>();
   private final Map<String, byte[]> resources = new HashMap<>();

   public InputArchive(String text) throws IOException {
      try (JarFile var8 = new JarFile(new File(text))) {
         Enumeration var9 = var8.entries();

         while (var9.hasMoreElements()) {
            JarEntry var10 = (JarEntry)var9.nextElement();
            byte[] var11 = new byte[(int)var10.getSize()];
            new DataInputStream(var8.getInputStream(var10)).readFully(var11);
            this.acceptEntry(var10, var11);
         }
      }
   }

   private void acceptEntry(JarEntry entry, byte[] bytes) {
      String name = entry.getName();
      if (!name.endsWith(".class")) {
         this.resources.put(name, bytes);
      } else {
         short version = classVersion(bytes);
         if (version < 51) {
            JnicLogger.INSTANCE.debug("Skipping " + name + " (version: " + version + ")");
            this.resources.put(name, bytes);
         } else {
            ClassReader reader = new ClassReader(bytes);
            if (!name.equals(reader.getClassName() + ".class")) {
               JnicLogger.INSTANCE.debug("Skipping " + name + " (mismatch: " + reader.getClassName() + ")");
               this.resources.put(name, bytes);
            } else {
               this.classes.add(reader);
            }
         }
      }
   }

   private static short classVersion(byte[] bytes) {
      return (short)((bytes[6] & 255) << 8 | bytes[7] & 0xFF);
   }

   public Collection<ClassReader> getClasses() {
      if (!this.classes.isEmpty()) {
         return this.classes;
      } else {
         throw new IllegalStateException("No classes found");
      }
   }

   public Map<String, byte[]> getResources() {
      return this.resources;
   }

   public Map<String, byte[]> resourceEntries() {
      return this.resources;
   }
}
