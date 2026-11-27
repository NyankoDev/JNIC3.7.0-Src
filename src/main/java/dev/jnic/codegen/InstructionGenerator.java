package dev.jnic.codegen;

import static org.objectweb.asm.Opcodes.*;

import dev.jnic.analysis.NullnessFrame;
import dev.jnic.analysis.OperandStack;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.compiler.CompilationException;
import dev.jnic.logging.JnicLogger;
import dev.jnic.transformers.InstructionTransformer;
import dev.jnic.transformers.ReturnTransformer;
import dev.jnic.transformers.intrinsics.ObjectGetClassIntrinsic;
import dev.jnic.transformers.intrinsics.StringEqualsIntrinsic;
import dev.jnic.transformers.intrinsics.StringIsEmptyIntrinsic;
import dev.jnic.transformers.intrinsics.StringLengthIntrinsic;
import dev.jnic.utils.BytecodeFormatter;
import dev.jnic.utils.SourceText;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

public class InstructionGenerator implements SourceGenerator {
   public static Set<InstructionTransformer> INTRINSICS = Set.of(
      new StringLengthIntrinsic(), new ObjectGetClassIntrinsic(), new StringEqualsIntrinsic(), new StringIsEmptyIntrinsic()
   );
   public static Set<InstructionTransformer> TRANSFORMERS = new HashSet<>();
   private final AbstractInsnNode instruction;
   private final Frame<BasicValue> before;
   private final Frame<BasicValue> after;
   private final NullnessFrame nullness;
   private final JumpEmitter jumps;
   private final String ownerName;
   private final InstructionTransformer transformer;

   public InstructionGenerator(
      AbstractInsnNode instruction, Frame<BasicValue> before, Frame<BasicValue> after, NullnessFrame nullness, JumpEmitter jumps, String ownerName, boolean enabled
   ) {
      this.instruction = instruction;
      this.before = before;
      this.after = after;
      this.nullness = nullness;
      this.jumps = jumps;
      this.ownerName = ownerName;
      if (enabled) {
         for (InstructionTransformer instructionTransformer : INTRINSICS) {
            if (instructionTransformer.accepts(instruction)) {
               this.transformer = instructionTransformer;
               return;
            }
         }
      }

      for (InstructionTransformer instructionTransformer2 : TRANSFORMERS) {
         if (instructionTransformer2.accepts(instruction)) {
            this.transformer = instructionTransformer2;
            return;
         }
      }

      throw new CompilationException("No translator found for " + BytecodeFormatter.format(instruction));
   }

   @Override
   public String generate(CompilationContext context) {
      OperandStack stack = new OperandStack(this.before, this.after, this.nullness);
      if (!JnicLogger.TRACE_ENABLED) {
         return this.transformer.transform(context, this.instruction, stack, this.jumps, this.ownerName);
      }
      StringBuilder source = new StringBuilder();
      source.append("puts(\"[JNIC: exec ")
         .append(SourceText.diagnosticText(BytecodeFormatter.format(this.instruction)))
         .append("]\");\n    ");
      return source.append(this.transformer.transform(context, this.instruction, stack, this.jumps, this.ownerName)).toString();
   }

   public boolean canThrow() {
      switch (this.instruction.getOpcode()) {
         case IDIV:
         case LDIV:
         case IREM:
         case LREM:
            return true;
         default:
            return this.transformer.canThrow();
      }
   }

   public boolean isReturn() {
      return this.transformer instanceof ReturnTransformer;
   }
}
