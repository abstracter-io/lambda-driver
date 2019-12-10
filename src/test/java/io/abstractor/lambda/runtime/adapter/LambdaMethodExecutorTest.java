package io.abstractor.lambda.runtime.adapter;

import io.abstractor.lambda.runtime.SettableExecutionContext;
import io.abstractor.lambda.runtime.port.ExecutionContext;
import io.abstractor.lambda.runtime.port.ExecutionResult;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class LambdaMethodExecutorTest {
    private static final LambdaMethodExecutor<ExecutionContext> lambdaMethodExecutor = new LambdaMethodExecutor<>();

    @Test
    void nullArgumentsThrows() {
        assertThrows(NullPointerException.class, () -> {
            lambdaMethodExecutor.exec(null, new SettableExecutionContext());
        });

        assertThrows(NullPointerException.class, () -> {
            lambdaMethodExecutor.exec(BadConstructorClass.METHOD, null);
        });
    }

    @Test
    void badConstructorThrows() {
        final Throwable exception = assertThrows(RuntimeException.class, () -> {
            lambdaMethodExecutor.exec(BadConstructorClass.METHOD, new SettableExecutionContext());
        });

        assertSame(NoSuchMethodException.class, exception.getCause().getClass());
    }

    @Test
    void invokeExceptionThrows() {
        final Exception e = new Exception();

        final Throwable exception = assertThrows(RuntimeException.class, () -> {
            new LambdaMethodExecutor<ExecutionContext>() {
                @Override
                protected Object invoke(Method method, Object methodInvokeContext, Object[] args) throws Exception {
                    throw e;
                }
            }.exec(Lambda.METHOD, new SettableExecutionContext());
        });

        assertSame(e, exception.getCause());
    }

    @Test
    void staticClassMethodUsesNullInvokeContext() {
        final AtomicReference<Object> invokeContext = new AtomicReference<>();

        new LambdaMethodExecutor<ExecutionContext>() {
            @Override
            protected Object invoke(Method method, Object methodInvokeContext, Object[] args) throws Exception {
                invokeContext.set(methodInvokeContext);

                return null;
            }
        }.exec(Lambda.STATIC_METHOD, new SettableExecutionContext());

        assertNull(invokeContext.get());
    }

    @Test
    void executionResult() {
        final ExecutionContext executionContext = new SettableExecutionContext("3", "text/plain");
        final LambdaParameterSerializer<ExecutionContext> parameterSerializer = new LambdaParameterSerializer<>();

        parameterSerializer.addSerializer(String.class, ExecutionContext::getInput);

        LambdaMethodExecutor<ExecutionContext> lambdaMethodExecutor = new LambdaMethodExecutor<>(parameterSerializer);
        ExecutionResult executionResult = lambdaMethodExecutor.exec(Lambda.METHOD, executionContext);

        assertEquals(executionContext.getId(), executionResult.getExecutionId());

        assertSame(executionContext.getInput(), executionResult.getExecutionValue());
    }

    @Test
    void method() {
    }

    private static class BadConstructorClass {
        static final Method METHOD;

        static {
            try {
                METHOD = BadConstructorClass.class.getMethod("handle");
            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        private BadConstructorClass() {}

        private BadConstructorClass(String y) {}

        public void handle() {}
    }

    private static class Lambda {
        static final Method METHOD;
        static final Method STATIC_METHOD;

        static {
            try {
                METHOD = Lambda.class.getMethod("handle", String.class);
                STATIC_METHOD = Lambda.class.getMethod("staticHandle");

            }
            catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        // LambdaMethodExecutor assumes a no-args constructor is present
        public Lambda() {}

        public Object handle(String str) {
            return str;
        }

        public static void staticHandle() {}
    }
}