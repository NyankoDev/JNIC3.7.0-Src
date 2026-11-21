package dev.jnic.codegen;

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
      AbstractInsnNode instruction, Frame before, Frame after, NullnessFrame nullness, JumpEmitter jumps, String ownerName, boolean enabled
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
      StringBuilder source = new StringBuilder();
      if (!JnicLogger.TRACE_ENABLED) {
         InstructionTransformer transformer2 = this.transformer;
         AbstractInsnNode instruction2 = this.instruction;
         Frame before2 = this.before;
         Frame after2 = this.after;
         NullnessFrame nullness2 = this.nullness;
         OperandStack stack = new OperandStack(before2, after2, nullness2);
         JumpEmitter jumps2 = this.jumps;
         String ownerName2 = this.ownerName;
         String instructionSource = transformer2.transform(context, instruction2, stack, jumps2, ownerName2);
         source.append(instructionSource);
         return source.toString();
      } else {
         source.append("puts(\"[JNIC: exec ");
         String diagnosticText = SourceText.diagnosticText(BytecodeFormatter.format(this.instruction));
         source.append(diagnosticText);
         source.append("]\");\n    ");
         InstructionTransformer transformer3 = this.transformer;
         AbstractInsnNode instruction3 = this.instruction;
         Frame before3 = this.before;
         Frame after3 = this.after;
         NullnessFrame nullness3 = this.nullness;
         OperandStack stack2 = new OperandStack(before3, after3, nullness3);
         JumpEmitter jumps3 = this.jumps;
         String ownerName3 = this.ownerName;
         String instructionSource2 = transformer3.transform(context, instruction3, stack2, jumps3, ownerName3);
         source.append(instructionSource2);
         return source.toString();
      }
   }

   public boolean canThrow() {
      switch (this.instruction.getOpcode()) {
         case 108:
         case 109:
         case 112:
         case 113:
            return true;
         case 110:
         case 111:
         default:
            return this.transformer.canThrow();
      }
   }

   public boolean isReturn() {
      InstructionTransformer transformer2 = this.transformer;
      if (transformer2 == null) {
         return false;
      } else {
         return transformer2 == null ? true : transformer2 instanceof ReturnTransformer;
      }
   }
}
