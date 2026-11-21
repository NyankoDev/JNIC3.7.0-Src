package dev.jnic.codegen;

import dev.jnic.compiler.CompilationContext;

public interface SourceGenerator {
   String generate(CompilationContext context);
}
