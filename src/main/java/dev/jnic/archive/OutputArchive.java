package dev.jnic.archive;

import dev.jnic.logging.JnicLogger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class OutputArchive {
   private final String outputFile;

   private void writeEntry(String text, byte[] bytes, ZipOutputStream zipOutputStream) throws IOException {
      zipOutputStream.putNextEntry(new ZipEntry(text));
      zipOutputStream.write(bytes);
      zipOutputStream.closeEntry();
   }

   public void write(Collection<?> classes, Map<?, ?> resources) throws IOException {
      this.prepareOutputFile();

      try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(this.outputFile))) {
         JnicLogger.INSTANCE.info("Writing to file " + this.outputFile);

         for (Object classValue : classes) {
            if (classValue != null && !(classValue instanceof ClassNode)) {
               throw new ClassCastException("");
            }

            ClassNode classNode = (ClassNode)classValue;
            ClassWriter writer = new ClassWriter(1);
            classNode.accept(writer);
            this.writeEntry(classNode.name + ".class", writer.toByteArray(), zipOutputStream);
         }

         for (Object resourceEntryValue : resources.entrySet()) {
            if (resourceEntryValue != null && !(resourceEntryValue instanceof Entry)) {
               throw new ClassCastException("");
            }

            Entry resourceEntry = (Entry)resourceEntryValue;
            Object key = resourceEntry.getKey();
            Object entryValue = resourceEntry.getValue();
            if (key != null && !(key instanceof String) || entryValue != null && !(entryValue instanceof byte[])) {
               throw new ClassCastException("");
            }

            this.writeEntry((String)key, (byte[])entryValue, zipOutputStream);
         }
      }
   }

   private void prepareOutputFile() throws IOException {
      File file = new File(this.outputFile);
      if (file.exists()) {
         JnicLogger.INSTANCE.warn(this.outputFile + " already exists, renaming to " + this.outputFile + ".BACKUP");
         File file2 = new File(this.outputFile + ".BACKUP");
         if (file2.exists()) {
            JnicLogger.INSTANCE.warn(this.outputFile + ".BACKUP already exists, nuking the backup...");
            if (!file2.delete()) {
               throw new IOException("Failed to delete backup file");
            }
         }

         if (!file.renameTo(file2)) {
            throw new IOException("Failed to rename file");
         }
      }

      if (!file.createNewFile()) {
         throw new IOException("Failed to create new file");
      }
   }

   public OutputArchive(String outputFile) {
      this.outputFile = outputFile;
   }
}
