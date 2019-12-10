package io.abstractor.lambda.runtime.port;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * A relay is a way to handle (relay) the 3 different modes of a
 * method execution cycle (invocation of a lambda handler). One for relaying
 * an execution result, one for relaying an exception during execution
 * and a final one for relaying any initialization (such as method lookup) errors.
 */
public interface ExecutionRelay<T extends ExecutionContext> {
	void relayExecutionResult(ExecutionResult executionResult);

	void relayExecutionException(Throwable e, T executionContext);

	void relayInitException(Throwable e);

	static URL create(String url) {
		try {
			return URI.create(url).toURL();
		}
		catch (MalformedURLException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}
}
