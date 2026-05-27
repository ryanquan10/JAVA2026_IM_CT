package org.tio.utils.hutool;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

public class StrJavaObject extends SimpleJavaFileObject {
	private String content;

	StrJavaObject(String name, String content) {
		super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
		this.content = content;
	}

	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return content;
	}
}