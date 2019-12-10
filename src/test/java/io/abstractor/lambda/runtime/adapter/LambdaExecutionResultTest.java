package io.abstractor.lambda.runtime.adapter;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LambdaExecutionResultTest {
    @Test
    void emptyIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LambdaExecutionResult(null, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LambdaExecutionResult("", null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LambdaExecutionResult("  ", null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LambdaExecutionResult("\t", null);
        });
    }

    // Public interfaces should always be tested
    @Test
    void testGetters() {
        final String id = UUID.randomUUID().toString();
        final String value = "test value";
        final LambdaExecutionResult lambdaExecutionResult = new LambdaExecutionResult(id, value);

        assertEquals(id, lambdaExecutionResult.getExecutionId());
        assertEquals(value, lambdaExecutionResult.getExecutionValue());;
    }
}