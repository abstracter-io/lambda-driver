package io.abstractor.lambda.runtime.port;

/**
 * An object holding information regarding the current execution cycle of a method
 */
public interface ExecutionContext {
    /**
     * @return String a unique identifier to distinguish the current execution cycle.
     *
     * This method must always return a non empty string.
     */
    String getId();

    /**
     * @return String a representation of the current execution cycle input
     *
     * This method may return null
     */
    String getInput();

    /**
     * @return String the mime type of the input (application/json for instance)
     *
     * This method may return null
     */
    String getInputMimeType();

    default boolean isJson() {
        final String mimeType = getInputMimeType();

        return mimeType != null && mimeType.toLowerCase().contains("json");
    }
}
