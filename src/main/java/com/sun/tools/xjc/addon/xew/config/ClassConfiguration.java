package com.sun.tools.xjc.addon.xew.config;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Per-class or per-field configuration.
 */
public class ClassConfiguration extends CommonConfiguration {

	public ClassConfiguration(CommonConfiguration configuration) {
		super(configuration);
	}

	/**
	 * Returns the value of {@code annotate} option. By default returns {@code true}.
	 */
	public boolean isAnnotatable() {
		return ObjectUtils.defaultIfNull((Boolean) configurationValues.get(ConfigurationOption.ANNOTATE), Boolean.TRUE)
		            .booleanValue();
	}

	public void setAnnotatable(boolean annotate) {
		configurationValues.put(ConfigurationOption.ANNOTATE, Boolean.valueOf(annotate));
	}

	/**
	 * Returns the value of {@code annotate} option. By default returns {@code true}.
	 */
	public Pair<String, String> toMap() {
		return (Pair<String, String>) configurationValues.get(ConfigurationOption.TO_MAP);
	}

	public void setToMap(Pair<String, String> keyValue) {
		configurationValues.put(ConfigurationOption.TO_MAP, keyValue);
	}

	@Override
	protected ToStringBuilder appendProperties(ToStringBuilder builder) {
		super.appendProperties(builder);
		builder.append("excluded", isAnnotatable());

		return builder;
	}
}
