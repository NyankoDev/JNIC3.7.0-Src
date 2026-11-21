package dev.jnic.analysis;

import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;

public class NullnessAnalyzer extends Analyzer<NullnessValue> {
   public NullnessAnalyzer() {
      super(NullnessInterpreter.INSTANCE);
   }

   @Override
   protected Frame<NullnessValue> newFrame(int index, int index2) {
      return new NullnessFrame(index, index2);
   }

   @Override
   protected Frame<NullnessValue> newFrame(Frame<? extends NullnessValue> frame) {
      return new NullnessFrame(frame);
   }
}
