package dev.jnic.codegen;

import static org.objectweb.asm.Opcodes.ATHROW;

import dev.jnic.analysis.NullnessAnalyzer;
import dev.jnic.analysis.NullnessFrame;
import dev.jnic.analysis.NullnessValue;
import dev.jnic.analysis.TypedBasicInterpreter;
import dev.jnic.compiler.CompilationContext;
import dev.jnic.compiler.CompilationException;
import dev.jnic.logging.JnicLogger;
import dev.jnic.utils.JniTypes;
import dev.jnic.utils.SourceText;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;

public class NativeMethodGenerator implements SourceGenerator {
   private final ClassNode owner;
   private final MethodNode method;
   private final String functionName;

   NativeMethodGenerator(ClassNode owner, MethodNode method, String functionName) {
      this.owner = owner;
      this.method = method;
      this.functionName = functionName;
   }

   private Map<AbstractInsnNode, Integer> createDispatchTable() {
      Set<AbstractInsnNode> blockStarts = new HashSet<>();
      blockStarts.add(this.method.instructions.getFirst());

      for (AbstractInsnNode currentInstruction : this.method.instructions) {
         if (currentInstruction instanceof LabelNode) {
            blockStarts.add(currentInstruction);
         }

         if (currentInstruction instanceof JumpInsnNode && currentInstruction.getNext() != null) {
            blockStarts.add(currentInstruction.getNext());
         }
      }

      int count = blockStarts.size() + ThreadLocalRandom.current().nextInt(blockStarts.size());
      List<Integer> dispatchKeys = IntStream.rangeClosed(-count, count).boxed().collect(Collectors.toList());
      Collections.shuffle(dispatchKeys);
      Map<AbstractInsnNode, Integer> dispatchTable = new HashMap<>();

      for (AbstractInsnNode currentInstruction2 : blockStarts) {
         dispatchTable.put(currentInstruction2, dispatchKeys.get(dispatchTable.size()));
      }

      return dispatchTable;
   }

   private Map<LabelNode, String> createLabels() {
      Map<LabelNode, String> labels = new HashMap<>();

      for (AbstractInsnNode currentInstruction : this.method.instructions) {
         if (currentInstruction instanceof LabelNode) {
            LabelNode label = (LabelNode)currentInstruction;
            labels.put(label, "L" + labels.size());
         }
      }

      return labels;
   }

   private String emitExceptionHandlers(CompilationContext context, List<TryCatchBlockNode> handlers, JumpEmitter jump) {
      if (handlers == null) {
         return this.returnOnException(context) + "\n";
      }

      StringBuilder output = new StringBuilder("        ");
      output.append(JniTypes.stackVariable("stack", 0, Type.getObjectType("java/lang/Object")))
         .append(" = ")
         .append(context.jniCall("ExceptionOccurred"))
         .append(";\n");
      if (JnicLogger.TRACE_ENABLED) {
         output.append("        ").append(context.jniCall("ExceptionDescribe")).append(";\n");
      }

      output.append("        ").append(context.jniCall("ExceptionClear")).append(";\n");

      for (TryCatchBlockNode handler : handlers) {
         output.append("        ");
         if (handler.type == null) {
            if (JnicLogger.TRACE_ENABLED) {
               output.append("puts(\"[JNIC: exception caught [ALL] trace \");")
                  .append(context.jniCall("ExceptionDescribe"))
                  .append(";")
                  .append("puts(\"-- end trace]\");\n");
            }

            output.append(context.jniCall("ExceptionClear")).append(";\n        ").append(jump.jumpIf("1", handler.handler)).append('\n');
         } else {
            output.append("if (")
               .append(
                  context.jniCall(
                     "IsInstanceOf",
                     JniTypes.stackVariable("stack", 0, Type.getObjectType("java/lang/Object")),
                     context.getClassCache().classReference(handler.type)
                  )
               )
               .append(") {\n");
            if (JnicLogger.TRACE_ENABLED) {
               output.append("puts(\"[JNIC: exception caught [")
                  .append(SourceText.diagnosticText(handler.type))
                  .append("] trace \");")
                  .append(context.jniCall("ExceptionDescribe"))
                  .append(";")
                  .append("puts(\"-- end trace]\");\n");
            }

            output.append("            ")
               .append(context.jniCall("ExceptionClear"))
               .append(";\n        ")
               .append(jump.jumpIf("1", handler.handler))
               .append("\n        }\n");
         }
      }

      output.append("        ")
         .append("(void)(" + context.jniCall("Throw", JniTypes.stackVariable("stack", 0, Type.getObjectType("java/lang/Object"))) + ")")
         .append(";\n");
      return output.append(this.returnOnException(context)).append('\n').toString();
   }

   private String returnOnException(CompilationContext context) {
      Type returnType = Type.getReturnType(this.method.desc);
      return (JnicLogger.TRACE_ENABLED ? "puts(\"[JNIC: exception unhandled]\");" : "")
         + (returnType.getSort() == Type.VOID ? "        return;" : "        return " + JniTypes.stackVariable("temp", 0, returnType) + ";");
   }

   @Override
   public String generate(CompilationContext context) {
      Analyzer<BasicValue> typeAnalyzer = new Analyzer<>(new TypedBasicInterpreter());
      NullnessAnalyzer nullnessAnalyzer = new NullnessAnalyzer();

      try {
         typeAnalyzer.analyze(this.owner.name, this.method);
         nullnessAnalyzer.analyze(this.owner.name, this.method);
      } catch (AnalyzerException analyzerFailure) {
         String expression = "Method failed verification: " + this.owner.name + "." + this.method.name + this.method.desc;
         throw new CompilationException(expression, analyzerFailure);
      }

      Frame<BasicValue>[] typeFrames = typeAnalyzer.getFrames();
      Frame<NullnessValue>[] nullnessFrames = nullnessAnalyzer.getFrames();
      StringBuilder source = new StringBuilder();
      source.append(new MethodDeclarationGenerator(this.owner, this.method, this.functionName).generate(context)).append(" {\n");
      source.append(new LocalVariablesGenerator(this.method).generate(context));
      if (JnicLogger.TRACE_ENABLED) {
         source.append("puts(\"[JNIC: begin ").append(SourceText.diagnosticText(this.owner.name + "#" + this.method.name + this.method.desc)).append("]\");\n");
      }

      boolean useDispatcher = context.getOptions().isFlowObfuscationEnabled();
      Map<AbstractInsnNode, Integer> dispatchTable = useDispatcher ? this.createDispatchTable() : null;
      Map<LabelNode, String> labels = useDispatcher ? null : this.createLabels();
      if (useDispatcher) {
         source.append("    int flow = ")
            .append(context.getLiterals().encryptedInteger(dispatchTable.get(this.method.instructions.getFirst())))
            .append(";\n    dispatch:; switch (flow) {");
      }

      for (int index = 0; index < this.method.instructions.size(); index++) {
         AbstractInsnNode currentInstruction = this.method.instructions.get(index);
         if (useDispatcher) {
            if (dispatchTable.containsKey(currentInstruction)) {
               source.append("    case ").append(dispatchTable.get(currentInstruction)).append(":\n");
            }
         } else if (currentInstruction instanceof LabelNode) {
            source.append("    ").append(labels.get((LabelNode)currentInstruction)).append(":;\n");
         }

         if (currentInstruction.getOpcode() != -1) {
            int nextDispatchKey = useDispatcher ? dispatchTable.getOrDefault(currentInstruction.getNext(), 0) : 0;
            JumpEmitter jumpEmitter = useDispatcher
               ? (text, labelNode) -> dispatchJump(context, dispatchTable, nextDispatchKey, text, labelNode)
               : (text, labelNode) -> labelJump(labels, text, labelNode);
            Frame<BasicValue> before = typeFrames[index];
            Frame<BasicValue> after = index + 1 < typeFrames.length ? typeFrames[index + 1] : null;
            NullnessFrame nullness = (NullnessFrame)nullnessFrames[index];
            InstructionGenerator instructionGenerator = new InstructionGenerator(
               currentInstruction, before, after, nullness, jumpEmitter, this.owner.name, context.getOptions().isIntrinsicsEnabled());
            source.append("    ").append(instructionGenerator.generate(context));
            if (instructionGenerator.canThrow()) {
               boolean isThrow = currentInstruction.getOpcode() == ATHROW;
               if (!isThrow) {
                  source.append(useDispatcher ? "    if (" : "    if (__builtin_expect(");
                  source.append(context.jniCall("ExceptionCheck"));
                  source.append(useDispatcher ? ") {\n" : ", 0)) {\n");
               }

               source.append(this.emitExceptionHandlers(context, typeAnalyzer.getHandlers(index), jumpEmitter));
               if (!isThrow) {
                  source.append("    }\n");
               }
            }
         }
      }

      if (useDispatcher) {
         source.append("    } __builtin_unreachable();");
      }

      return source.append('}').toString();
   }

   private static String labelJump(Map<LabelNode, String> labels, String condition, LabelNode target) {
      return (condition == null ? "" : "if (" + condition + ") ") + "goto " + labels.get(target) + ";";
   }

   private static String dispatchJump(CompilationContext context, Map<AbstractInsnNode, Integer> map, int index, String text, LabelNode labelNode) {
      String encryptedValue = context.getLiterals().encryptedInteger(map.get(labelNode));
      return "1".equals(text)
         ? "flow = " + encryptedValue + "; goto dispatch;"
         : "flow = (" + text + ") ? " + encryptedValue + " : " + context.getLiterals().encryptedInteger(index) + "; goto dispatch;";
   }
}
