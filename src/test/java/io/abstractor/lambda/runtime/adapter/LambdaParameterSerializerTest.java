package io.abstractor.lambda.runtime.adapter;

import io.abstractor.lambda.runtime.SettableExecutionContext;
import io.abstractor.lambda.runtime.port.ExecutionContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

class LambdaParameterSerializerTest {
    private LambdaParameterSerializer<ExecutionContext> parameterSerializer;

    @BeforeEach
    void beforeEach() {
        parameterSerializer = new LambdaParameterSerializer<>();
    }

    @Test
    void serializersCanBeOverridden() {
        final SettableExecutionContext executionContext = new SettableExecutionContext("b", null);

        parameterSerializer.addSerializer(String.class, ctx -> "");

        parameterSerializer.addSerializer(String.class, ExecutionContext::getInput);

        assertEquals(executionContext.getInput(), parameterSerializer.serialize(String.class, executionContext));
    }

    @Test
    void unknownTypesReturnNull() {
        final ExecutionContext executionContext = new SettableExecutionContext("b", null);

        parameterSerializer.serialize(UnknownType.class, executionContext);
    }

    @Test
    void emptyInputReturnsNull() {
        assertNull(parameterSerializer.serialize(String.class, new SettableExecutionContext()));
    }

    @Test
    void unknownTypeIsSerialized() {
        final String field = "yo";
        final String JSON = String.format("{\"field\": \"%s\"}", field);
        final ExecutionContext executionContext = new SettableExecutionContext(JSON, "application/json");
        final UnknownType unknownType = (UnknownType)parameterSerializer.serialize(UnknownType.class, executionContext);

        assertEquals(field, unknownType.getField());
    }

    @Test
    void unknownTypeSerializationExceptionThrown() {
        assertThrows(RuntimeException.class, () -> {
            final String JSON = String.format("{\"unknown_field\": \"%s\"}", "");
            final ExecutionContext executionContext = new SettableExecutionContext(JSON, "application/json");

            parameterSerializer.serialize(UnknownType.class, executionContext);
        });
    }

    @Test
    void testDefaultInstance() {
        final LambdaParameterSerializer<ExecutionContext> pr = LambdaParameterSerializer.createDefaultInstance();

        assertTrue(pr.serialize(String.class, new SettableExecutionContext("some string")) instanceof String);

        assertTrue(pr.serialize(InputStream.class, new SettableExecutionContext("s")) instanceof InputStream);
        assertTrue(pr.serialize(OutputStream.class, new SettableExecutionContext("s")) instanceof OutputStream);

        assertTrue(pr.serialize(int.class, new SettableExecutionContext("3")) instanceof Integer);
        assertTrue(pr.serialize(Integer.class, new SettableExecutionContext("3")) instanceof Integer);

        assertTrue(pr.serialize(float.class, new SettableExecutionContext("3.3")) instanceof Float);
        assertTrue(pr.serialize(Float.class, new SettableExecutionContext("3.3")) instanceof Float);

        assertTrue(pr.serialize(double.class, new SettableExecutionContext("3.3")) instanceof Double);
        assertTrue(pr.serialize(Double.class, new SettableExecutionContext("3.3")) instanceof Double);

        assertTrue(pr.serialize(boolean.class, new SettableExecutionContext("true")) instanceof Boolean);
        assertTrue(pr.serialize(Boolean.class, new SettableExecutionContext("false")) instanceof Boolean);
    }

    @Test
    void serializerExceptionThrown() {
        assertThrows(NumberFormatException.class, () -> {
            parameterSerializer.addSerializer(Long.class, executionContext -> {
                return Long.parseLong(executionContext.getInput());
            });

            parameterSerializer.serialize(Long.class,new SettableExecutionContext("bad long", null));
        });
    }

    @Test
    void addSerializerThrowsWithNullArguments() {
        assertThrows(NullPointerException.class, () -> {
            parameterSerializer.addSerializer((Class)null, executionContext -> null);
        });

        assertThrows(NullPointerException.class, () -> {
            parameterSerializer.addSerializer(String.class, null);
        });
    }

    private static class UnknownType {
        private String field;

        public UnknownType() {}

        public UnknownType setField(String field) {
            this.field = field;

            return this;
        }

        public String getField() {
            return field;
        }
    }
}