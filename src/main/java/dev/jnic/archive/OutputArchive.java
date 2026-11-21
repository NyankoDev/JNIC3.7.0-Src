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

   public void write(Collection<?> items1, Map<?, ?> map) throws IOException {
      this.prepareOutputFile();

      try (ZipOutputStream var3 = new ZipOutputStream(new FileOutputStream(this.outputFile))) {
         JnicLogger.INSTANCE.info("Writing to file " + this.outputFile);

         for (Object var5 : items1) {
            if (var5 != null && !(var5 instanceof ClassNode)) {
               throw new ClassCastException("");
            }

            ClassNode var6 = (ClassNode)var5;
            ClassWriter var9 = new ClassWriter(1);
            var6.accept(var9);
            this.writeEntry(var6.name + ".class", var9.toByteArray(), var3);
         }

         for (Object var10 : map.entrySet()) {
            if (var10 != null && !(var10 instanceof Entry)) {
               throw new ClassCastException("");
            }

            Entry var13 = (Entry)var10;
            Object var11 = var13.getKey();
            Object var14 = var13.getValue();
            if (var11 != null && !(var11 instanceof String) || var14 != null && !(var14 instanceof byte[])) {
               throw new ClassCastException("");
            }

            this.writeEntry((String)var11, (byte[])var14, var3);
         }
      }
   }

   private void prepareOutputFile() throws IOException {
      File var1 = new File(this.outputFile);
      if (var1.exists()) {
         JnicLogger.INSTANCE.warn(this.outputFile + " already exists, renaming to " + this.outputFile + ".BACKUP");
         File var2 = new File(this.outputFile + ".BACKUP");
         if (var2.exists()) {
            JnicLogger.INSTANCE.warn(this.outputFile + ".BACKUP already exists, nuking the backup...");
            if (!var2.delete()) {
               throw new IOException("Failed to delete backup file");
            }
         }

         if (!var1.renameTo(var2)) {
            throw new IOException("Failed to rename file");
         }
      }

      if (!var1.createNewFile()) {
         throw new IOException("Failed to create new file");
      }
   }

   public OutputArchive(String outputFile) {
      this.outputFile = outputFile;
   }
}
