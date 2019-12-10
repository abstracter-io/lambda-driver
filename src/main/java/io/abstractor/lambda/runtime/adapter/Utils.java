package io.abstractor.lambda.runtime.adapter;

import java.util.function.Supplier;

public class Utils {
	public static class StringUtils {
		private static Supplier<String> REQUIRED_MESSAGE_SUPPLIER = () -> "Argument must be a non empty string";

		public static boolean isBlank(String str) {
			return str == null || str.trim().isEmpty();
		}

		public static String requireNonBlank(String str, Supplier<String> messageSupplier) {
			if (isBlank(str)) {
				final String message = (messageSupplier != null ? messageSupplier : REQUIRED_MESSAGE_SUPPLIER).get();

				throw new IllegalArgumentException(message);
			}

			return str;
		}

		public static String requireNonBlank(String str) {
			return requireNonBlank(str, REQUIRED_MESSAGE_SUPPLIER);
		}
	}
}
