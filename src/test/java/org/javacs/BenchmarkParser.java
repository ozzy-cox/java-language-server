package org.javacs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.openjdk.jmh.annotations.*;

@Warmup(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class BenchmarkParser {

    @State(Scope.Benchmark)
    public static class CompilerState {
        public Path file = Paths.get("src/main/java/org/javacs/Artifact.java").normalize();
        public JavaCompilerService compiler = createCompiler();

        private static JavaCompilerService createCompiler() {
            LOG.info("Create new compiler...");

            var workspaceRoot = Paths.get(".").normalize().toAbsolutePath();
            FileStore.setWorkspaceRoots(Set.of(workspaceRoot));
            var classPath = new InferConfig(workspaceRoot).classPath();
            return new JavaCompilerService(classPath, Collections.emptySet(), Collections.emptySet());
        }

        @Setup
        public void setup() {
            Profiler.quiet = true;
        }

        @TearDown
        public void teardown() {
            Profiler.quiet = false;
        }
    }

    @Benchmark
    public void parse(CompilerState state) {
        state.compiler.parseFile(state.file.toUri());
    }

    public static void main(String[] args) {
        var state = new CompilerState();
        state.setup();
        while (true) {
            state.compiler.parseFile(state.file.toUri());
        }
    }

    private static final Logger LOG = Logger.getLogger("main");
}