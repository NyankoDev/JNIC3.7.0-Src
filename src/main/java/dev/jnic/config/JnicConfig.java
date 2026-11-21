package dev.jnic.config;

import dev.jnic.compiler.TargetPlatform;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.RecordComponentNode;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "jnic")
public class JnicConfig {
   @Element(name = "options", required = false)
   private CompilerOptions options;
   @ElementList(name = "targets", entry = "target", required = false)
   private List<TargetPlatform> targets;
   @Element(name = "includeAnnotation", required = false)
   private String includeAnnotation;
   @Element(name = "excludeAnnotation", required = false)
   private String excludeAnnotation;
   @ElementList(name = "include", required = false)
   private List<MethodPattern> include;
   @ElementList(name = "exclude", required = false)
   private List<MethodPattern> exclude;

   public CompilerOptions getOptions() {
      return this.options == null ? CompilerOptions.DEFAULT : this.options;
   }

   public List<TargetPlatform> getTargets() {
      return this.targets;
   }

   public boolean isIncluded(ClassNode owner, MethodNode method) {
      String var3 = method.name;
      if (var3 == null) {
         throw new NullPointerException("");
      }

      if (!var3.equals("<init>")
         && (owner.access & 512) == 0
         && (method.access & 1280) == 0
         && ((owner.access & 16384) == 0 || !var3.equals("values"))
         && (this.excludeAnnotation == null || !this.hasExcludeAnnotation(method))) {
         if (this.includeAnnotation == null || !this.hasIncludeAnnotation(method)) {
            if (this.excludeAnnotation != null && this.hasExcludeAnnotation(owner)) {
               return false;
            }

            if (this.includeAnnotation == null || !this.hasIncludeAnnotation(owner)) {
               return (this.include == null || matchesAny(this.include, owner, method)) && (this.exclude == null || !matchesAny(this.exclude, owner, method));
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean annotationPresent(List<AnnotationNode> list, String text) {
      if (list != null && !list.isEmpty()) {
         Stream var10000 = list.stream();
         String capturedValue2 = "L" + text + ";";
         Stream<AnnotationNode> matchingValues = var10000;
         Predicate<AnnotationNode> function4 = annotationNode -> annotationNode.desc.equals(capturedValue2);
         return matchingValues.anyMatch(function4);
      } else {
         return false;
      }
   }

   private boolean hasIncludeAnnotation(MethodNode method) {
      return annotationPresent(method.visibleAnnotations, this.includeAnnotation) || annotationPresent(method.invisibleAnnotations, this.includeAnnotation);
   }

   private boolean hasExcludeAnnotation(MethodNode method) {
      return annotationPresent(method.visibleAnnotations, this.excludeAnnotation) || annotationPresent(method.invisibleAnnotations, this.excludeAnnotation);
   }

   private boolean hasIncludeAnnotation(ClassNode owner) {
      return annotationPresent(owner.visibleAnnotations, this.includeAnnotation) || annotationPresent(owner.invisibleAnnotations, this.includeAnnotation);
   }

   private boolean hasExcludeAnnotation(ClassNode owner) {
      return annotationPresent(owner.visibleAnnotations, this.excludeAnnotation) || annotationPresent(owner.invisibleAnnotations, this.excludeAnnotation);
   }

   private static boolean lambda$isIncluded$1(ClassNode owner, MethodNode method, MethodPattern methodPattern) {
      String var3 = owner.name;
      String var4 = method.name;
      String var5 = method.desc;
      return methodPattern.matches(var3, var4, var5);
   }

   private static boolean lambda$isIncluded$0(ClassNode owner, MethodNode method, MethodPattern methodPattern) {
      String var3 = owner.name;
      String var4 = method.name;
      String var5 = method.desc;
      return methodPattern.matches(var3, var4, var5);
   }

   private static boolean matchesAny(List<MethodPattern> items0, ClassNode owner, MethodNode method) {
      Iterator var3 = items0.iterator();

      while (var3.hasNext()) {
         if (((MethodPattern)var3.next()).matches(owner.name, method.name, method.desc)) {
            return true;
         }
      }

      return false;
   }

   public void stripSelectionAnnotations(Collection<ClassNode> classes) {
      Iterator<ClassNode> iterator = classes.iterator();

      while (iterator.hasNext()) {
         ClassNode type = iterator.next();
         if ((type.access & 8192) != 0 && this.isSelectionType(type.name)) {
            iterator.remove();
         } else {
            this.clearSelectionAnnotations(type.visibleAnnotations);
            this.clearSelectionAnnotations(type.invisibleAnnotations);
            this.clearSelectionAnnotations(type.visibleTypeAnnotations);
            this.clearSelectionAnnotations(type.invisibleTypeAnnotations);

            for (FieldNode field : type.fields) {
               this.clearSelectionAnnotations(field.visibleAnnotations);
               this.clearSelectionAnnotations(field.invisibleAnnotations);
               this.clearSelectionAnnotations(field.visibleTypeAnnotations);
               this.clearSelectionAnnotations(field.invisibleTypeAnnotations);
            }

            for (MethodNode method : type.methods) {
               this.clearSelectionAnnotations(method.visibleAnnotations);
               this.clearSelectionAnnotations(method.invisibleAnnotations);
               this.clearSelectionAnnotations(method.visibleTypeAnnotations);
               this.clearSelectionAnnotations(method.invisibleTypeAnnotations);
               this.clearSelectionAnnotations(method.visibleLocalVariableAnnotations);
               this.clearSelectionAnnotations(method.invisibleLocalVariableAnnotations);
               this.clearParameterAnnotations(method.visibleParameterAnnotations);
               this.clearParameterAnnotations(method.invisibleParameterAnnotations);
            }

            if (type.recordComponents != null) {
               for (RecordComponentNode component : type.recordComponents) {
                  this.clearSelectionAnnotations(component.visibleAnnotations);
                  this.clearSelectionAnnotations(component.invisibleAnnotations);
                  this.clearSelectionAnnotations(component.visibleTypeAnnotations);
                  this.clearSelectionAnnotations(component.invisibleTypeAnnotations);
               }
            }
         }
      }
   }

   private boolean isSelectionType(String name) {
      return name.equals(this.includeAnnotation) || name.equals(this.excludeAnnotation);
   }

   private void clearSelectionAnnotations(List<? extends AnnotationNode> annotations) {
      if (annotations != null) {
         Predicate<AnnotationNode> function2 = annotation -> this.isSelectionType(annotation.desc.substring(1, annotation.desc.length() - 1));
         annotations.removeIf(function2);
      }
   }

   private void clearParameterAnnotations(List<AnnotationNode>[] parameters) {
      if (parameters != null) {
         for (List<AnnotationNode> annotations : parameters) {
            this.clearSelectionAnnotations(annotations);
         }
      }
   }
}
