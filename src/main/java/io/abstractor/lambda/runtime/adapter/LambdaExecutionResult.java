package io.abstractor.lambda.runtime.adapter;

import io.abstractor.lambda.runtime.port.ExecutionResult;

public class LambdaExecutionResult implements ExecutionResult {
    private final String id;
    private final Object methodExecutionValue;

    public LambdaExecutionResult(String id, Object methodExecutionValue) {
        this.id = Utils.StringUtils.requireNonBlank(id);
        this.methodExecutionValue = methodExecutionValue;
    }

    @Override
    public String getExecutionId() {
        return id;
    }

    @Override
    public Object getExecutionValue() {
        return methodExecutionValue;
    }
}
