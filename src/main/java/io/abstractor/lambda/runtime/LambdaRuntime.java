package io.abstractor.lambda.runtime;

import io.abstractor.lambda.runtime.port.ExecutionContext;
import io.abstractor.lambda.runtime.port.ExecutionResult;
import io.abstractor.lambda.runtime.port.MethodExecutor;
import io.abstractor.lambda.runtime.port.ExecutionRelay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

import java.util.Objects;
import java.util.function.Supplier;

public class LambdaRuntime<T extends ExecutionContext> {
    private static final Logger logger = LogManager.getLogger(LambdaRuntime.class);

    private final Supplier<Method> methodSupplier;
    private final ExecutionRelay<T> executionRelay;
    private final MethodExecutor<T> methodExecutor;
    private final Supplier<T> executionContextSupplier;

    private LambdaRuntime(Builder<T> builder) {
        this.methodSupplier = Objects.requireNonNull(builder.methodSupplier);
        this.methodExecutor = Objects.requireNonNull(builder.methodExecutor);
        this.executionRelay = Objects.requireNonNull(builder.executionRelay);
        this.executionContextSupplier = Objects.requireNonNull(builder.executionContextSupplier);
    }

    private T getExecutionContext() {
        T executionContext = null;

        try {
            executionContext = executionContextSupplier.get();
        }
        catch (Exception e) {
            logger.error("An error occurred while retrieving execution context", e);
        }

        return executionContext;
    }

    private ExecutionResult exec(Method method, T executionContext) {
        try {
            final ExecutionResult executionResult = methodExecutor.exec(method, executionContext);

            executionRelay.relayExecutionResult(executionResult);

            return executionResult;
        }
        catch (Throwable e) {
            executionRelay.relayExecutionException(e, executionContext);

            throw e;
        }
    }

    public ExecutionResult exec() {
        return exec(methodSupplier.get(), getExecutionContext());
    }

    // TODO: Examine the behaviour of the "next invocation" API for the sake of concurrency.
    public void init() {
        try {
            final Method method = methodSupplier.get();

            //noinspection InfiniteLoopStatement
            while (true) {
                final T executionContext = getExecutionContext();

                if (executionContext != null) {
                    try {
                        exec(method, executionContext);
                    }
                    catch (Throwable e) {
                        logger.debug("An exception occurred during execution (id: {})", executionContext.getId(), e);
                    }
                }
            }
        }
        catch (Throwable e) {
            logger.debug("An exception occurred during init", e);

            executionRelay.relayInitException(e);

            throw e;
        }
    }

    public static class Builder<T extends ExecutionContext> {
        private Supplier<Method> methodSupplier;
        private MethodExecutor<T> methodExecutor;
        private ExecutionRelay<T> executionRelay;
        private Supplier<T> executionContextSupplier;

        public Builder<T> setMethodExecutor(MethodExecutor<T> methodExecutor) {
            this.methodExecutor = methodExecutor;

            return this;
        }

        public Builder<T> setMethodSupplier(Supplier<Method> methodSupplier) {
            this.methodSupplier = methodSupplier;

            return this;
        }

        public Builder<T> setExecutionRelay(ExecutionRelay<T> executionRelay) {
            this.executionRelay = executionRelay;

            return this;
        }

        public Builder<T> setExecutionContextSupplier(Supplier<T> executionContextSupplier) {
            this.executionContextSupplier = executionContextSupplier;

            return this;
        }

        public LambdaRuntime<T> build() {
            return new LambdaRuntime<T>(this);
        }
    }
}
