package dev.jnic.codegen;

import org.objectweb.asm.tree.LabelNode;

@FunctionalInterface
public interface JumpEmitter {
   String jumpIf(String condition, LabelNode target);

   default String jump(LabelNode target) {
      return this.jumpIf("1", target);
   }
}
