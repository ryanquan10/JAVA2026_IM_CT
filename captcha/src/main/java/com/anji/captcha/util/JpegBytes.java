package com.anji.captcha.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tanyaowu 2021年1月21日 下午3:33:57
 */
public class JpegBytes extends SimpleJavaFileObject {
    private static Logger log = LoggerFactory.getLogger(JpegBytes.class);

    private ByteArrayOutputStream out;

    /**
     * 
     * @param name xxx.yyy.zzz.User defaultImages.jigsaw.original.JpegFactoryBinder
     * @author tanyaowu
     */
    JpegBytes(String name) {
	super(URI.create("bytes:///" + name.replace('.', '/') + ".class"), Kind.CLASS);
    }

    public byte[] getCode() {
	return out.toByteArray();
    }

    public OutputStream openOutputStream() throws IOException {
	out = new ByteArrayOutputStream();
	return out;
    }

    public static void main(String[] args) throws IOException {
	JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

	ArrayList<JpegBytes> classFileObjects = new ArrayList<>();

	DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
	StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
	JavaFileManager fileManager = new ForwardingJavaFileManager<JavaFileManager>(standardFileManager) {
	    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
		    FileObject sibling) throws IOException {
		if (kind == Kind.CLASS) {
		    JpegBytes fileObject = new JpegBytes(className);
		    classFileObjects.add(fileObject);
		    return fileObject;
		} else
		    return super.getJavaFileForOutput(location, className, kind, sibling);
	    }
	};

	JpegBytes jpegBytes = new JpegBytes("defaultImages.jigsaw.original.JpegFactoryBinder");
	List<JavaFileObject> list = new ArrayList<>();
	list.add(jpegBytes);
	

//	JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, list);
//	Boolean result = task.call();

	for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics())
	    System.out.println(d.getKind() + ": " + d.getMessage(null));
	fileManager.close();

	//
	JpegLoader loader = new JpegLoader(classFileObjects);
	try {
	    @SuppressWarnings("unused")
	    Class<?> cl = Class.forName("defaultImages.jigsaw.original.JpegFactoryBinder", true, loader);
	} catch (Exception e) {
	    log.error("", e);
	}
    }

}
