package org.jbehave.eclipse.util;

public class StringDecorator {

	public static StringDecorator decorate(String value) {
		return new StringDecorator(value);
	}

	private final String underlying;

	public StringDecorator(String underlying) {
		this.underlying = underlying;
	}

	public boolean isNull() {
		return underlying == null;
	}

	public boolean isStartOfOneOfIgnoringCase(String... values) {
		return isStartOfOneOf(true, values);
	}

	public boolean isStartOfOneOf(String... values) {
		return isStartOfOneOf(false, values);
	}

	public boolean isStartOfOneOf(boolean ignoreCase, String... values) {
		if (isNull()) {
			return false;
		}
		for (String value : values) {
			if (underlying.regionMatches(ignoreCase, 0, value, 0,
					underlying.length())) {
				return true;
			}
		}
		return false;
	}

	public boolean startsWithIgnoringCase(String prefix) {
		return startsWith(true, prefix);
	}

	public boolean startsWithOneOfIgnoringCase(String... prefixes) {
		return startsWithOneOf(true, prefixes);
	}

	public boolean startsWithOneOf(String... prefixes) {
		return startsWithOneOf(false, prefixes);
	}

	public boolean startsWithOneOf(boolean ignoreCase, String... prefixes) {
		if (isNull())
			return false;
		for (String prefix : prefixes) {
			if (underlying.regionMatches(ignoreCase, 0, prefix, 0,
					prefix.length()))
				return true;
		}
		return false;
	}

	public boolean startsWith(boolean ignoreCase, String prefix) {
		if (isNull())
			return false;
		if (underlying.regionMatches(ignoreCase, 0, prefix, 0, prefix.length()))
			return true;
		return false;
	}

	public boolean endsWithOneOf(String... suffixes) {
		if (isNull())
			return false;
		for (String suffix : suffixes) {
			if (underlying.endsWith(suffix))
				return true;
		}
		return false;
	}

	public boolean equalsToOneOf(String... values) {
		if (isNull())
			return false;
		for (String value : values) {
			if (underlying.equals(value))
				return true;
		}
		return false;
	}

}
