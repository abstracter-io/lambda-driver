package io.abstractor.lambda.runtime.adapter;

import io.abstractor.lambda.runtime.port.ExecutionContext;
import io.abstractor.lambda.runtime.port.ExecutionResult;
import io.abstractor.lambda.runtime.port.MethodExecutor;
import io.abstractor.lambda.runtime.port.ParameterSerializer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.Objects;

public class LambdaMethodExecutor<T extends ExecutionContext> implements MethodExecutor<T> {
    private final ParameterSerializer<T> parameterSerializer;

    private Object methodInvokeContext = null;

    public LambdaMethodExecutor(ParameterSerializer<T> parameterSerializer) {
        this.parameterSerializer = Objects.requireNonNull(parameterSerializer);
    }

    public LambdaMethodExecutor() {
        this(LambdaParameterSerializer.createDefaultInstance());
    }

    protected Object[] resolveMethodArguments(T executionContext, Method method) {
        final int parameterCount = method.getParameterCount();
        final Object[] parameters = new Object[parameterCount];
        final Class<?>[] parameterTypes = method.getParameterTypes();

        for (int i = 0; i < parameterCount; i += 1) {
            final Class<?> parameterClass = parameterTypes[i];

            parameters[i] = getParameterSerializer().serialize(parameterClass, executionContext);
        }

        return parameters;
    }

    protected Object invoke(Method method, Object methodInvokeContext, Object[] args) throws Exception {
        // We assume that the method about to be invoke is "public".
        // This helps keeps things simple and avoid reflection voodoo
        // especially when dealing with JDK > 9.
        return method.invoke(methodInvokeContext, args);
    }

    protected ParameterSerializer<T> getParameterSerializer() {
        return parameterSerializer;
    }

    /**
     * Create an instance of the handler method class or null if the method is static
     *
     * @param method the method to create invoke context for
     * @return Object an instance of the handler method class or null if the method is static
     * @throws Exception in case class instantiation failed.
     * @see Method#invoke(Object, Object...)
     */
    protected Object createMethodInvokeContext(Method method) throws Exception {
        final Object context;

        // When the handling method is static we don't need need an instance
        if (Modifier.isStatic(method.getModifiers())) {
            context = null;
        }
        else {
            // We need an instance :|
            context = method.getDeclaringClass().getConstructor().newInstance();
        }

        return context;
    }

    @Override
    public ExecutionResult exec(Method method, T executionContext) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(executionContext);

        try {
            if (methodInvokeContext == null) {
                methodInvokeContext = createMethodInvokeContext(method);
            }

            final Object value = invoke(method, methodInvokeContext, resolveMethodArguments(executionContext, method));

            return new LambdaExecutionResult(executionContext.getId(), value);
        }
        catch (Throwable e) {
            throw new RuntimeException("An error occurred during method execution", e);
        }
    }
}
