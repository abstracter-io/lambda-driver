package io.abstractor.lambda.runtime.port;

import java.lang.reflect.Method;

public interface MethodExecutor<T extends ExecutionContext> {
    ExecutionResult exec(Method method, T executionContext);
}
