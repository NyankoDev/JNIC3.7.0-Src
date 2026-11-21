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
      boolean var17 = Jnic.getLicense().licenseType().equals(EVALUATION_EDITION);

      for (ClassReader var10000 : inputArchive.getClasses()) {
         ClassNode var20 = new ClassNode();
         var10000.accept(var20, 2);
         ArrayList var21 = new ArrayList();

         for (MethodNode var23 : var20.methods) {
            if (config.isIncluded(var20, var23)) {
               var21.add(var23);
               this.translatedMethods.registerMethod(var20.name, var23.name, var23.desc);
               if (var17) {
                  break;
               }
            }
         }

         if (!var21.isEmpty()) {
            this.methods.put(var20, var21);
            if (var17) {
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
