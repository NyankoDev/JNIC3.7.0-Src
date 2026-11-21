package dev.jnic.compiler;

import dev.jnic.Jnic;
import dev.jnic.archive.InputArchive;
import dev.jnic.bytecode.TranslatedMethodIndex;
import dev.jnic.config.JnicConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class CompilationPlan {
   private final JnicConfig config;
   private final Map<ClassNode, List<MethodNode>> methods = new HashMap<>();
   private final TranslatedMethodIndex translatedMethods = new TranslatedMethodIndex();
   private static final String EVALUATION_EDITION = "evaluation";

   public CompilationPlan(InputArchive inputArchive, JnicConfig config) {
      this.config = config;
      boolean evaluationMode = Jnic.getLicense().licenseType().equals(EVALUATION_EDITION);

      for (ClassReader reader : inputArchive.getClasses()) {
         ClassNode classNode = new ClassNode();
         reader.accept(classNode, 2);
         ArrayList selectedMethods = new ArrayList();

         for (MethodNode methodNode : classNode.methods) {
            if (config.isIncluded(classNode, methodNode)) {
               selectedMethods.add(methodNode);
               this.translatedMethods.registerMethod(classNode.name, methodNode.name, methodNode.desc);
               if (evaluationMode) {
                  break;
               }
            }
         }

         if (!selectedMethods.isEmpty()) {
            this.methods.put(classNode, selectedMethods);
            if (evaluationMode) {
               break;
            }
         }
      }
   }

   public JnicConfig getConfig() {
      return this.config;
   }

   public Map<ClassNode, List<MethodNode>> getMethods() {
      return Collections.unmodifiableMap(this.methods);
   }

   public TranslatedMethodIndex getTranslatedMethods() {
      return this.translatedMethods;
   }
}
