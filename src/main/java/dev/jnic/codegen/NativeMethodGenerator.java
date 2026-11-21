package dev.jnic.codegen;

import dev.jnic.analysis.NullnessAnalyzer;
import dev.jnic.analysis.NullnessFrame;
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
      Set<AbstractInsnNode> items2 = new HashSet();
      items2.add(this.method.instructions.getFirst());

      for (AbstractInsnNode var1 : this.method.instructions) {
         if (var1 instanceof LabelNode) {
            items2.add(var1);
         }

         if (var1 instanceof JumpInsnNode && var1.getNext() != null) {
            items2.add(var1.getNext());
         }
      }

      int var4 = items2.size() + ThreadLocalRandom.current().nextInt(items2.size());
      List var6 = IntStream.rangeClosed(-var4, var4).boxed().collect(Collectors.toList());
      Collections.shuffle(var6);
      HashMap var8 = new HashMap();

      for (AbstractInsnNode var5 : items2) {
         var8.put(var5, (Integer)var6.get(var8.size()));
      }

      return var8;
   }

   private Map<LabelNode, String> createLabels() {
      HashMap var2 = new HashMap();

      for (AbstractInsnNode var4 : this.method.instructions) {
         if (var4 instanceof LabelNode) {
            LabelNode var5 = (LabelNode)var4;
            var2.put(var5, "L" + var2.size());
         }
      }

      return var2;
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
      Type var2 = Type.getReturnType(this.method.desc);
      return (JnicLogger.TRACE_ENABLED ? "puts(\"[JNIC: exception unhandled]\");" : "")
         + (var2.getSort() == 0 ? "        return;" : "        return " + JniTypes.stackVariable("temp", 0, var2) + ";");
   }

   @Override
   public String generate(CompilationContext context) {
      Analyzer var2 = new Analyzer<>(new TypedBasicInterpreter());
      NullnessAnalyzer var4 = new NullnessAnalyzer();

      try {
         var2.analyze(this.owner.name, this.method);
         var4.analyze(this.owner.name, this.method);
      } catch (AnalyzerException var33) {
         String var22 = "Method failed verification: " + this.owner.name + "." + this.method.name + this.method.desc;
         throw new CompilationException(var22, var33);
      }

      Frame[] var24 = var2.getFrames();
      Frame[] var25 = var4.getFrames();
      StringBuilder var5 = new StringBuilder();
      String var8 = this.functionName;
      MethodNode var7 = this.method;
      ClassNode var6 = this.owner;
      var5.append(new MethodDeclarationGenerator(var6, var7, var8).generate(null)).append(" {\n");
      MethodNode var27 = this.method;
      String var26 = this.owner.name;
      var5.append(new LocalVariablesGenerator(var26, var27).generate(null));
      if (JnicLogger.TRACE_ENABLED) {
         var5.append("puts(\"[JNIC: begin ").append(SourceText.diagnosticText(this.owner.name + "#" + this.method.name + this.method.desc)).append("]\");\n");
      }

      boolean var19 = context.getOptions().isFlowObfuscationEnabled();
      Map<AbstractInsnNode, Integer> value28 = var19 ? this.createDispatchTable() : null;
      Map<LabelNode, String> value29 = var19 ? null : this.createLabels();
      if (var19) {
         var5.append("    int flow = ")
            .append(context.getLiterals().encryptedInteger((Integer)value28.get(this.method.instructions.getFirst())))
            .append(";\n    dispatch:; switch (flow) {");
      }

      for (int var9 = 0; var9 < this.method.instructions.size(); var9++) {
         AbstractInsnNode var10 = this.method.instructions.get(var9);
         if (var19) {
            if (value28.containsKey(var10)) {
               var5.append("    case ").append(value28.get(var10)).append(":\n");
            }
         } else if (var10 instanceof LabelNode) {
            var5.append("    ").append((String)value29.get((LabelNode)var10)).append(":;\n");
         }

         if (var10.getOpcode() != -1) {
            int var11 = var19 ? value28.getOrDefault(var10.getNext(), 0) : 0;
            JumpEmitter var20 = var19
               ? (text, labelNode) -> dispatchJump(context, value28, var11, text, labelNode)
               : (text, labelNode) -> labelJump(value29, text, labelNode);
            Frame var12 = var24[var9];
            Frame var13 = var9 + 1 < var24.length ? var24[var9 + 1] : null;
            NullnessFrame var10003 = (NullnessFrame)var25[var9];
            String var10005 = this.owner.name;
            boolean var18 = context.getOptions().isIntrinsicsEnabled();
            String var17 = var10005;
            JumpEmitter var16 = var20;
            NullnessFrame var15 = var10003;
            Frame var14 = var13;
            Frame var32 = var12;
            AbstractInsnNode var30 = var10;
            InstructionGenerator var31 = new InstructionGenerator(var30, var32, var14, var15, var16, var17, var18);
            var5.append("    ").append(var31.generate(context));
            if (var31.canThrow()) {
               boolean var21 = var10.getOpcode() == 191;
               if (!var21) {
                  var5.append(var19 ? "    if (" : "    if (__builtin_expect(");
                  var5.append(context.jniCall("ExceptionCheck"));
                  var5.append(var19 ? ") {\n" : ", 0)) {\n");
               }

               var5.append(this.emitExceptionHandlers(context, var2.getHandlers(var9), var20));
               if (!var21) {
                  var5.append("    }\n");
               }
            }
         }
      }

      if (var19) {
         var5.append("    } __builtin_unreachable();");
      }

      return var5.append('}').toString();
   }

   private static String labelJump(Map<LabelNode, String> map, String text, LabelNode labelNode) {
      return (text == null ? "" : "if (" + text + ") ") + "goto " + (String)map.get(labelNode) + ";";
   }

   private static String dispatchJump(CompilationContext context, Map<AbstractInsnNode, Integer> map, int index, String text, LabelNode labelNode) {
      String value5 = context.getLiterals().encryptedInteger((Integer)map.get(labelNode));
      return "1".equals(text)
         ? "flow = " + value5 + "; goto dispatch;"
         : "flow = (" + text + ") ? " + value5 + " : " + context.getLiterals().encryptedInteger(index) + "; goto dispatch;";
   }
}
