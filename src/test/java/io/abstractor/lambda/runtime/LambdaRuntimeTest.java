package io.abstractor.lambda.runtime;

import io.abstractor.lambda.runtime.port.ExecutionContext;
import io.abstractor.lambda.runtime.port.ExecutionRelay;
import io.abstractor.lambda.runtime.port.ExecutionResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class LambdaRuntimeTest {
    private LambdaRuntime.Builder<ExecutionContext> lambdaRuntimeBuilder;
    private ConsumableExecutionRelay<ExecutionContext> consumableExecutionRelay;

    static LambdaRuntime.Builder<ExecutionContext> lambdaRuntimeBuilder() {
        final LambdaRuntime.Builder<ExecutionContext> lambdaRuntimeBuilder = new LambdaRuntime.Builder<>();

        lambdaRuntimeBuilder.setMethodSupplier(() -> null);
        lambdaRuntimeBuilder.setExecutionRelay(new ConsumableExecutionRelay<>());
        lambdaRuntimeBuilder.setMethodExecutor((method, executionContext) -> new SettableExecutionResult());
        lambdaRuntimeBuilder.setExecutionContextSupplier(SettableExecutionContext::new);

        return lambdaRuntimeBuilder;
    }

    @BeforeEach
    void beforeEach() {
        consumableExecutionRelay = new ConsumableExecutionRelay<>();
        lambdaRuntimeBuilder = lambdaRuntimeBuilder().setExecutionRelay(consumableExecutionRelay);
    }

    @Test
    void lambdaRuntimeInstantiationThrowsWhenIncomplete() {
        assertThrows(NullPointerException.class, () -> {
            lambdaRuntimeBuilder().setMethodExecutor(null).build();
        });

        assertThrows(NullPointerException.class, () -> {
            lambdaRuntimeBuilder().setExecutionContextSupplier(null).build();
        });

        assertThrows(NullPointerException.class, () -> {
            lambdaRuntimeBuilder().setMethodSupplier(null).build();
        });

        assertThrows(NullPointerException.class, () -> {
            lambdaRuntimeBuilder().setExecutionRelay(null).build();
        });
    }

    @Test
    void executionExceptionIsRelayedAndBubble() {
        final RuntimeException runtimeException = new RuntimeException();
        final AtomicReference<Throwable> relayedException = new AtomicReference<>();

        consumableExecutionRelay.setExecutionExceptionConsumer((e, executionContext) -> {
            relayedException.set(e);
        });

        lambdaRuntimeBuilder.setMethodExecutor((method, executionContext) -> {
            throw runtimeException;
        });
        lambdaRuntimeBuilder.setExecutionRelay(consumableExecutionRelay);
        lambdaRuntimeBuilder.setMethodSupplier(() -> null);

        assertThrows(runtimeException.getClass(),() -> {
            lambdaRuntimeBuilder.build().exec();
        });

        assertSame(runtimeException, relayedException.get());
    }

    @Test
    void initExceptionIsRelayedAndBubble() {
        final RuntimeException runtimeException = new RuntimeException();
        final AtomicReference<Throwable> initException = new AtomicReference<>();

        consumableExecutionRelay.setInitExceptionConsumer(initException::set);

        lambdaRuntimeBuilder.setExecutionRelay(consumableExecutionRelay);
        lambdaRuntimeBuilder.setMethodSupplier(() -> {
            throw runtimeException;
        });

        assertThrows(runtimeException.getClass(), () -> {
            lambdaRuntimeBuilder.build().init();
        });

        assertSame(runtimeException, initException.get());
    }

    @Test
    void executionResultIsRelayed() {
        final SettableExecutionResult executionResult = new SettableExecutionResult();
        final AtomicReference<ExecutionResult> executionResultAtomicReference = new AtomicReference<>();

        lambdaRuntimeBuilder.setMethodExecutor((method, executionContext) -> executionResult);

        consumableExecutionRelay.setExecutionResultConsumer(executionResultAtomicReference::set);

        lambdaRuntimeBuilder.build().exec();

        assertSame(executionResult, executionResultAtomicReference.get());
    }

    @Test
    void initExecutionResultIsRelayed() {
        final SettableExecutionResult executionResult = new SettableExecutionResult();
        final Thread thread = new Thread(() -> {
            lambdaRuntimeBuilder.build().init();
        });
        final CompletableFuture<ExecutionResult> executionResultCompletableFuture = new CompletableFuture<>();

        lambdaRuntimeBuilder.setMethodExecutor((method, executionContext) -> executionResult);

        consumableExecutionRelay.setExecutionResultConsumer((result) -> {
            thread.interrupt();

            executionResultCompletableFuture.complete(result);
        });

        thread.start();

        try {
            assertSame(executionResult, executionResultCompletableFuture.get());
        }
        catch (ExecutionException | InterruptedException e) {
            fail(e);
        }
    }

    @Test
    void executionContextRetrievalExceptionIsIgnored() {
        final Thread thread = new Thread(() -> {
            lambdaRuntimeBuilder.build().init();
        });
        final AtomicInteger count = new AtomicInteger();
        final int expectedCallsCount = 2;
        final CompletableFuture<Integer> executionContextCallsCount = new CompletableFuture<>();

        lambdaRuntimeBuilder.setExecutionContextSupplier(() -> {
            if (count.incrementAndGet() == expectedCallsCount) {
                executionContextCallsCount.complete(count.get());
            }

            thread.interrupt();

            throw new RuntimeException();
        });

        thread.start();

        try {
            assertEquals(expectedCallsCount, executionContextCallsCount.get());
        }
        catch (ExecutionException | InterruptedException e) {
            fail(e);
        }
    }

    public static class SettableExecutionResult implements ExecutionResult {
        private String id = UUID.randomUUID().toString();
        private Object value;

        public SettableExecutionResult(String id, Object value) {
            this.id = id;
            this.value = value;
        }

        public SettableExecutionResult() {}

        public void setId(String id) {
            this.id = id;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public String getExecutionId() {
            return id;
        }

        @Override
        public Object getExecutionValue() {
            return value;
        }
    }

    private static class ConsumableExecutionRelay<T extends ExecutionContext> implements ExecutionRelay<T> {
        private Consumer<Throwable> throwableConsumer;
        private Consumer<ExecutionResult> executionResultConsumer;
        private BiConsumer<Throwable, T> executionExceptionConsumer;

        @Override
        public void relayExecutionResult(ExecutionResult executionResult) {
            if (executionResultConsumer != null) {
                executionResultConsumer.accept(executionResult);
            }
        }

        @Override
        public void relayExecutionException(Throwable e, T executionContext) {
            if (executionExceptionConsumer != null) {
                executionExceptionConsumer.accept(e, executionContext);
            }
        }

        @Override
        public void relayInitException(Throwable e) {
            if (throwableConsumer != null) {
                throwableConsumer.accept(e);
            }
        }

        public void setInitExceptionConsumer(Consumer<Throwable> throwableConsumer) {
            this.throwableConsumer = throwableConsumer;
        }

        public void setExecutionResultConsumer(Consumer<ExecutionResult> executionResultConsumer) {
            this.executionResultConsumer = executionResultConsumer;
        }

        public void setExecutionExceptionConsumer(BiConsumer<Throwable, T> executionExceptionConsumer) {
            this.executionExceptionConsumer = executionExceptionConsumer;
        }
    }
}