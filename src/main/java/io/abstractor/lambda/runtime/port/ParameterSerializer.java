package io.abstractor.lambda.runtime.port;

/**
 * @param <T> type / sub-type that implements ExecutionContext
 */
public interface ParameterSerializer<T extends ExecutionContext> {
    /**
     * @param <R> The expected type of the serialized object
     * @param cls A class reference of type R
     * @param executionContext an execution context.
     */
    <R> R serialize(Class<R> cls, T executionContext);
}
