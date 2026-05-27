package org.tio.utils.file;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

import cn.hutool.core.io.FileUtil;

/**
 * @author tanyaowu 
 * 2021年9月29日 下午3:22:59
 */
public class ExcludedFileFilter implements FileFilter {
	private Set<String>	excludedDir		= null;
	private Set<String>	excludedFile	= null;
	private Set<String>	excludedExt		= null;

	/**
	 * @param excludedDir
	 * @param excludedFile
	 * @param excludedExt
	 * @author tanyaowu
	 */
	public ExcludedFileFilter(Set<String> excludedDir, Set<String> excludedFile, Set<String> excludedExt) {
		super();
		this.excludedDir = excludedDir;
		this.excludedFile = excludedFile;
		this.excludedExt = excludedExt;
	}

	/** 
	 * @param pathname
	 * @return
	 * @author tanyaowu
	 */
	@Override
	public boolean accept(File file) {
		String filename = file.getName();
		String ext = FileUtil.extName(filename);
		if (file.isDirectory()) {
			if (excludedDir != null) {
				if (excludedDir.contains(filename)) {
					return false;
				}
			}

			if (excludedExt != null) {
				if (excludedExt.contains(ext)) {
					return false;
				}
			}

			return true;
		} else {
			if (excludedFile != null) {
				if (excludedFile.contains(filename)) {
					return false;
				}
			}

		}
		return true;
	}

}
