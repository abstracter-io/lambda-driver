package io.abstractor.lambda.runtime.port;

/**
 * A serialize that takes a parameter type (of a given method)
 * an execution context, and returns an object with the parameter value.
 *
 * @param <T> ExecutionContext type / sub-type
 */
public interface ParameterSerializer<T extends ExecutionContext> {
    <R> R serialize(Class<?> type, T executionContext);
}
