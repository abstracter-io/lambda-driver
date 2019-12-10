package io.abstractor.lambda.runtime.port;

/**
 * An object holding execution information once a method has been invoked.
 */
public interface ExecutionResult {
    /**
     * @return String a unique identifier to distinguish the handled execution cycle.
     *
     * This method must always return a non empty string.
     */
    String getExecutionId();

    /**
     * @return Object the value returned after a method has been invoked.
     *
     * This method may return null.
     */
    Object getExecutionValue();
}
