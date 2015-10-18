package com.insideaem.maven.plugin.aempackagediff;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class IncludeExclude {
	@XStreamAsAttribute
	String pattern = "";

	public IncludeExclude(final String pattern) {
		this.pattern = pattern;
	}
}
