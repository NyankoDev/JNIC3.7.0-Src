package dev.jnic.archive;

import java.util.concurrent.ThreadLocalRandom;

public class NativeLibrarySlice {
   private final long startOffset;
   private final long endOffset;
   private final byte[] key;

   public NativeLibrarySlice(long startOffset, long endOffset, byte[] key) {
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      this.key = key;
   }

   public NativeLibrarySlice(long startOffset, long endOffset) {
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      this.key = new byte[32];
      ThreadLocalRandom.current().nextBytes(this.key);
   }

   public long getStartOffset() {
      return this.startOffset;
   }

   public long getEndOffset() {
      return this.endOffset;
   }

   public byte[] getKey() {
      return this.key;
   }
}
