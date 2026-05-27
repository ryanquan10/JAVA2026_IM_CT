package com.anji.captcha.util;

public class JpegLoader extends ClassLoader {
    private Iterable<JpegBytes> classes;

    public JpegLoader(Iterable<JpegBytes> classes) {
	this.classes = classes;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
	for (JpegBytes cl : classes) {
	    if (cl.getName().equals("/" + name.replace('.', '/') + ".jpg")) {
		byte[] bytes = cl.getCode();
		return defineClass(name, bytes, 0, bytes.length);
	    }
	}
	throw new ClassNotFoundException(name);
    }
}
