package io.abstractor.lambda.runtime;

import io.abstractor.lambda.runtime.port.ExecutionContext;

import java.util.UUID;

public class SettableExecutionContext implements ExecutionContext {
    private String id = UUID.randomUUID().toString();
    private String input;
    private String inputMimeType;

    public SettableExecutionContext(String input, String inputMimeType) {
        this.input = input;
        this.inputMimeType = inputMimeType;
    }

    public SettableExecutionContext(String input) {
        this.input = input;
    }

    public SettableExecutionContext() {}

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getInput() {
        return input;
    }

    @Override
    public String getInputMimeType() {
        return inputMimeType;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public void setInputMimeType(String inputMimeType) {
        this.inputMimeType = inputMimeType;
    }
}
