package io.abstractor.lambda.runtime.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LambdaMethodSupplierTest {
    @Test
    void badArgumentThrows() {
        assertThrows(IllegalArgumentException.class,() -> {
            new LambdaMethodSupplier(null);
        });

        assertThrows(IllegalArgumentException.class,() -> {
            new LambdaMethodSupplier("");
        });

        assertThrows(IllegalArgumentException.class,() -> {
            new LambdaMethodSupplier(" ");
        });

        assertThrows(IllegalArgumentException.class,() -> {
            new LambdaMethodSupplier("\t");
        });
    }

    @Test
    void privateMethodIsIgnored() {
        assertThrows(RuntimeException.class, () -> {
            final String method = Lambda.class.getName() + ".privateMethod";
            final LambdaMethodSupplier lambdaMethodSupplier = new LambdaMethodSupplier(method);

            lambdaMethodSupplier.get();
        });
    }

    @Test
    void publicMethodIsRetrieved() {
        final String name = Lambda.class.getName() + ".publicMethod";

        assertNotNull(new LambdaMethodSupplier(name).get());
    }

    @Test
    void testGetters() {
        final String name = "expected value";

        assertEquals(new LambdaMethodSupplier(name).getMethodFullyQualifiedName(), name);
    }

    @Test
    void invalidMethodQualifierThrows() {
        Throwable e = assertThrows(RuntimeException.class, () -> {
            new LambdaMethodSupplier("this.does.not.exists").get();
        });

        assertSame(ClassNotFoundException.class, e.getCause().getClass());
    }

    private static class Lambda {
        public Lambda() {}

        private void privateMethod() {}

        public void publicMethod() {}
    }
}