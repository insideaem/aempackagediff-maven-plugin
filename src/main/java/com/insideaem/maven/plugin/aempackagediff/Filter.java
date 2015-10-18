package com.insideaem.maven.plugin.aempackagediff;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("filter")
public class Filter {
	@XStreamAsAttribute
	final String root;

	@XStreamAlias("include")
	IncludeExclude include = null;
	@XStreamAlias("exclude")
	IncludeExclude exclude = null;

	public Filter(final String root, final boolean isDir) {
		this.root = root;
		if (isDir) {
			this.include = new IncludeExclude(root);
			this.exclude = new IncludeExclude(root + "/.*");
		}
	}

	public String getRoot() {
		return this.root;
	}
}
