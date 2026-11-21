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
         for (InstructionTransformer var15 : INTRINSICS) {
            if (var15.accepts(instruction)) {
               this.transformer = var15;
               return;
            }
         }
      }

      for (InstructionTransformer var17 : TRANSFORMERS) {
         if (var17.accepts(instruction)) {
            this.transformer = var17;
            return;
         }
      }

      throw new CompilationException("No translator found for " + BytecodeFormatter.format(instruction));
   }

   @Override
   public String generate(CompilationContext context) {
      StringBuilder var2 = new StringBuilder();
      if (!JnicLogger.TRACE_ENABLED) {
         InstructionTransformer var20 = this.transformer;
         AbstractInsnNode var4 = this.instruction;
         Frame var5 = this.before;
         Frame var6 = this.after;
         NullnessFrame var7 = this.nullness;
         OperandStack var14 = new OperandStack(var5, var6, var7);
         JumpEmitter var11 = this.jumps;
         String var17 = this.ownerName;
         String value3 = var20.transform(context, var4, var14, var11, var17);
         var2.append(value3);
         return var2.toString();
      } else {
         var2.append("puts(\"[JNIC: exec ");
         String value8 = SourceText.diagnosticText(BytecodeFormatter.format(this.instruction));
         var2.append(value8);
         var2.append("]\");\n    ");
         InstructionTransformer var10000 = this.transformer;
         AbstractInsnNode var10 = this.instruction;
         Frame var12 = this.before;
         Frame var15 = this.after;
         NullnessFrame var18 = this.nullness;
         OperandStack var13 = new OperandStack(var12, var15, var18);
         JumpEmitter var16 = this.jumps;
         String var19 = this.ownerName;
         String value9 = var10000.transform(context, var10, var13, var16, var19);
         var2.append(value9);
         return var2.toString();
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
      InstructionTransformer var1 = this.transformer;
      if (var1 == null) {
         return false;
      } else {
         return var1 == null ? true : var1 instanceof ReturnTransformer;
      }
   }
}
