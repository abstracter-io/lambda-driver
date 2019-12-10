package io.abstractor.lambda.runtime.adapter;

import io.abstractor.lambda.runtime.port.ExecutionContext;
import io.abstractor.lambda.runtime.port.ParameterSerializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * A basic serializer backed by Jackson
 *
 * @param <T> type / sub-type of execution context
 */
public class LambdaParameterSerializer<T extends ExecutionContext> implements ParameterSerializer<T> {
    private static final Logger logger = LogManager.getLogger(LambdaParameterSerializer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Function<T, ?>> serializers = new HashMap<>();

    public void addSerializer(Class<?> cls, Function<T, Object> serializer) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(serializer);

        final String canonicalClassName = cls.getCanonicalName();

        if (serializers.containsKey(canonicalClassName)) {
            logger.debug("Overriding serializer definition of {}", canonicalClassName);
        }

        serializers.put(canonicalClassName, serializer);
    }

    public void addSerializer(Class<?>[] classes, Function<T, Object> serializer) {
        for (Class<?> cls : classes) {
            addSerializer(cls, serializer);
        }
    }

    public Function<T, ?> getSerializer(Class<?> cls) {
        return serializers.get(Objects.requireNonNull(cls).getCanonicalName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R serialize(Class<R> cls, T executionContext) {
        final String classCanonicalName = Objects.requireNonNull(cls).getCanonicalName();
        final String executionContextBody = executionContext.getInput();

        R serializedValue = null;

        if (serializers.containsKey(classCanonicalName)) {
            return (R)serializers.get(classCanonicalName).apply(executionContext);
        }
        else if (executionContext.isJson()) {
            try {
                serializedValue = (R)objectMapper.readValue(executionContextBody, cls);
            }
            catch (JsonProcessingException e) {
                logger.error("An error occurred during serialization of {} type", cls, e);

                throw new RuntimeException(e);
            }
        }

        return serializedValue;
    }

    public static <T extends ExecutionContext> LambdaParameterSerializer<T> createDefaultInstance() {
        final LambdaParameterSerializer<T> parameterSerializer = new LambdaParameterSerializer<>();

        parameterSerializer.addSerializer(String.class, executionContext -> {
            return executionContext.getInput();
        });

        parameterSerializer.addSerializer(InputStream.class, executionContext -> {
            return new ByteArrayInputStream(executionContext.getInput().getBytes());
        });

        parameterSerializer.addSerializer(OutputStream.class, executionContext -> {
            return new ByteArrayOutputStream();
        });

        parameterSerializer.addSerializer(new Class[]{int.class, Integer.class}, executionContext -> {
            return Integer.valueOf(executionContext.getInput());
        });

        parameterSerializer.addSerializer(new Class[]{float.class, Float.class}, executionContext -> {
            return Float.parseFloat(executionContext.getInput());
        });

        parameterSerializer.addSerializer(new Class[]{double.class, Double.class}, executionContext -> {
            return Double.parseDouble(executionContext.getInput());
        });

        parameterSerializer.addSerializer(new Class[]{boolean.class, Boolean.class}, executionContext -> {
            return Boolean.parseBoolean(executionContext.getInput());
        });

        return parameterSerializer;
    }
}
