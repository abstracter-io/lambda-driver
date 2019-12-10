package io.abstractor.lambda.runtime.adapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.function.Supplier;

public class LambdaMethodSupplier implements Supplier<Method> {
    private static final Logger logger = LogManager.getLogger(LambdaMethodSupplier.class);

    private final String methodFullyQualifiedName;

    public LambdaMethodSupplier(String methodFullyQualifiedName) {
        this.methodFullyQualifiedName = Utils.StringUtils.requireNonBlank(methodFullyQualifiedName, () -> {
            return "method fully qualified name must be a non empty string";
        });
    }

    /**
     * Find the first method with a given name.
     * May return null.
     *
     * @param cls The class to search a method in
     * @param methodName The method name to search for
     *
     * @return Method the found method or null
     */
    protected Method getClassMethod(Class<?> cls, String methodName) {
        Method method = null;

        // Perhaps its better to use both "getMethods" & "getDeclaredMethods" methods?
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                if (Modifier.isPrivate(m.getModifiers())) {
                    logger.warn("handler with private access modifier is not supported");
                }
                else {
                    method = m;

                    break;
                }
            }
        }

        return method;
    }

    public String getMethodFullyQualifiedName() {
        return methodFullyQualifiedName;
    }

    @Override
    public Method get() {
        final int split = methodFullyQualifiedName.lastIndexOf(".");
        final Class<?> methodClass = forName(methodFullyQualifiedName.substring(0, split));
        final String methodName = methodFullyQualifiedName.substring(split + 1);
        final Method method = getClassMethod(methodClass, methodName);

        if (method == null) {
            throw new RuntimeException(methodFullyQualifiedName + " handler lookup failed");
        }

        return method;
    }

    private static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
