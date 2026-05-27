
package org.tio.mg.service.utils.svn;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.hutool.StrUtil;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;

/**
 * @author tanyaowu
 */
public class SvnKit {
	private static Logger log = LoggerFactory.getLogger(SvnKit.class);

	public SvnKit() {
	}

	static {
		DAVRepositoryFactory.setup();
		/*
		 * For using over svn:// and svn+xxx://
		 */
		SVNRepositoryFactoryImpl.setup();
		/*
		 * For using over file:///
		 */
		FSRepositoryFactory.setup();
	}

	// 更新状态 true:没有程序在执行更新，反之则反
	public static Boolean DoUpdateStatus = true;

	// 声明SVN客户端管理类
	private static SVNClientManager ourClientManager;

	/**
	 * 参考：https://wiki.svnkit.com/Printing_Out_Repository_History
	 * @param svnUsername 
	 * @param svnPwd 
	 * @param svnPath https://t-io.org:2443/svn/tio-site-qijian
	 * @param limit 
	 * @return
	 * @author tanyaowu
	 */
	public static String getCommitHistory(String svnUsername, String svnPwd, String svnPath, long limit) {
		long start = System.currentTimeMillis();
		try {
			long startRevision = 0;
			long endRevision = -1;
			SVNRepository repository = null;
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnPath));
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(svnUsername, svnPwd.toCharArray());
			repository.setAuthenticationManager(authManager);
			
			try {
				endRevision = repository.getLatestRevision();
				startRevision = endRevision - limit;
				startRevision = Math.max(0, startRevision);
			} catch (SVNException e) {
				log.error("", e);
				return "";
			}

			StringBuilder sb = new StringBuilder(1024);

			repository.log(new String[] { "" }, endRevision, startRevision, true, true, limit, new ISVNLogEntryHandler() {

				@Override
				public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
					
					/*
					 * gets the revision number
					 */
					//					System.out.println("revision: " + logEntry.getRevision());
					/*
					 * gets the author of the changes made in that revision
					 */
					//					System.out.println("author: " + logEntry.getAuthor());
					/*
					 * gets the time moment when the changes were committed
					 */
					//					System.out.println("date: " + logEntry.getDate());
					sb.append("<span style='color:#009688;'>").append(DateUtil.formatDate(logEntry.getDate())).append("</span>\r\n");
					sb.append("---------------------------------------------\r\n");
					/*
					 * gets the commit log message
					 */
					//					System.out.println("log message: " + logEntry.getMessage());
					if (StrUtil.isNotBlank(logEntry.getMessage())) {  //注释
						sb.append("<span style='color:#777;font-size:14px;'>");
						sb.append(logEntry.getMessage()).append("\r\n");
						sb.append("</span>");
						sb.append("---------------------------------------------\r\n");
					}
					
					/*
					 * displaying all paths that were changed in that revision; cahnged
					 * path information is represented by SVNLogEntryPath.
					 */
					if (logEntry.getChangedPaths().size() > 0) {
//						sb.append("\r\n");
						Set<String> changedPathsSet = logEntry.getChangedPaths().keySet();

						sb.append("<span style='color:#777;font-size:14px;'>");
						for (Iterator<String> changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
							SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths().get(changedPaths.next());
							sb.append(" ").append(entryPath.getType()).append("	").append(entryPath.getPath());
							sb.append((entryPath.getCopyPath() != null) ? " (from " + entryPath.getCopyPath() + " revision " + entryPath.getCopyRevision() + ")" : "");
							sb.append("\r\n");
						}
						sb.append("</span>");
					}
					sb.append("\r\n");
				}
			});

			String ret = sb.toString();
			System.out.println(ret);
			return ret;

		} catch (Exception e) {
			log.error("", e);
			return "";
		} finally {
			long end = System.currentTimeMillis();
			long iv = end - start;
			log.error("获取提交日志耗时{}毫秒\r\n仓库:{}\r\n", iv, svnPath);
		}
	}

	/**
	 * SVN检出
	 * 
	 * @return Boolean
	 */
	public static Boolean checkOut() {

		// 相关变量赋值
		SVNURL repositoryURL = null;
		try {
			repositoryURL = SVNURL.parseURIEncoded(SvnConst.SVN_URL);
		} catch (SVNException e) {
			log.error("", e);
			return false;
		}

		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);

		// 实例化客户端管理类
		ourClientManager = SVNClientManager.newInstance((DefaultSVNOptions) options, SvnConst.SVN_USERNAME, SvnConst.SVN_PWD);

		// 要把版本库的内容check out到的目录
		File wcDir = new File(SvnConst.SVN_LOCAL_DIR);

		// 通过客户端管理类获得updateClient类的实例。
		SVNUpdateClient updateClient = ourClientManager.getUpdateClient();

		updateClient.setIgnoreExternals(false);

		// 执行check out 操作，返回工作副本的版本号。
		long workingVersion = -1;
		try {
			if (!wcDir.exists()) {
				workingVersion = updateClient.doCheckout(repositoryURL, wcDir, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
			} else {
				ourClientManager.getWCClient().doCleanup(wcDir);
				workingVersion = updateClient.doCheckout(repositoryURL, wcDir, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
			}
		} catch (SVNException e) {
			log.error("", e);
			return false;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}

		System.out.println("把版本：" + workingVersion + " check out 到目录：" + wcDir + "中。");
		return true;

	}

	/**
	 * 
	 * @param svnUsername 
	 * @param svnPwd 
	 * @param svnPath https://t-io.org:2443/svn/tio-site-qijian
	 * @param outputDir "E:/resources"
	 * @return
	 * @author tanyaowu
	 */

	public static Boolean doExport(String svnUsername, String svnPwd, String svnPath, String outputDir) {
		long start = System.currentTimeMillis();
		try {
			// 相关变量赋值
			SVNURL repositoryURL = null;
			try {
				repositoryURL = SVNURL.parseURIEncoded(svnPath);
			} catch (SVNException e) {
				log.error("", e);
				return false;
			}
			ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
			// 实例化客户端管理类
			ourClientManager = SVNClientManager.newInstance((DefaultSVNOptions) options, svnUsername, svnPwd);
			// 要把版本库的内容check out到的目录
			File wcDir = new File(outputDir);
			// 通过客户端管理类获得updateClient类的实例。
			SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
			updateClient.setIgnoreExternals(false);
			@SuppressWarnings("unused")
			long workingVersion = -1;
			try {
				if (wcDir.exists()) {
					FileUtil.del(wcDir);
				}
				workingVersion = updateClient.doExport(repositoryURL, wcDir, SVNRevision.HEAD, SVNRevision.HEAD, "downloadModel", true, SVNDepth.INFINITY);
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
			return true;
		} finally {
			long end = System.currentTimeMillis();
			long iv = end - start;
			log.error("导出svn仓库耗时{}毫秒\r\n仓库:{}\r\n本地目录：{}", iv, svnPath, outputDir);
		}
	}

	/**
	 * 解除svn Luck
	 * 
	 * @return Boolean
	 */
	public static Boolean doCleanup() {
		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		// 实例化客户端管理类
		ourClientManager = SVNClientManager.newInstance((DefaultSVNOptions) options, SvnConst.SVN_USERNAME, SvnConst.SVN_PWD);

		// 要把版本库的内容check out到的目录
		File wcDir = new File(SvnConst.SVN_LOCAL_DIR);
		if (wcDir.exists()) {
			try {
				ourClientManager.getWCClient().doCleanup(wcDir);
			} catch (SVNException e) {
				log.error("", e);
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * 更新svn
	 * 
	 * @return int(-1更新失败，1成功，0有程序在占用更新)
	 */
	public static int doUpdate() {
		if (!SvnKit.DoUpdateStatus) {
			System.out.println("更新程序已经在运行中，不能重复请求！");
			return 0;
		}
		SvnKit.DoUpdateStatus = false;
		/*
		 * For using over http:// and https://
		 */
		try {
			DAVRepositoryFactory.setup();

			ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
			// 实例化客户端管理类
			ourClientManager = SVNClientManager.newInstance((DefaultSVNOptions) options, SvnConst.SVN_USERNAME, SvnConst.SVN_PWD);
			// 要更新的文件
			File updateFile = new File(SvnConst.SVN_LOCAL_DIR);
			// 获得updateClient的实例
			SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
			updateClient.setIgnoreExternals(false);
			// 执行更新操作
			long versionNum = updateClient.doUpdate(updateFile, SVNRevision.HEAD, SVNDepth.INFINITY, false, false);
			System.out.println("工作副本更新后的版本：" + versionNum);
			DoUpdateStatus = true;
			return 1;
		} catch (SVNException e) {
			DoUpdateStatus = true;
			log.error("", e);
			return -1;
		}
	}

	/**
	 * Svn提交 list.add("a.txt")也可直接添加单个文件; list.add("aaa")添加文件夹将添加夹子内所有的文件到svn,预添加文件必须先添加其所在的文件夹;
	 * 
	 * @param fileRelativePathList文件相对路径
	 * @return Boolean
	 */
	public static Boolean doCommit(List<String> fileRelativePathList) {
		// 注意：执行此操作要先执行checkout操作。因为本地需要有工作副本此范例才能运行。
		// 初始化支持svn://协议的库
		SVNRepositoryFactoryImpl.setup();

		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		// 实例化客户端管理类
		ourClientManager = SVNClientManager.newInstance((DefaultSVNOptions) options, SvnConst.SVN_USERNAME, SvnConst.SVN_PWD);
		// 要提交的文件夹子
		File commitFile = new File(SvnConst.SVN_LOCAL_DIR);
		// 获取此文件的状态（是文件做了修改还是新添加的文件？）
		SVNStatus status = null;
		File addFile = null;
		String strPath = null;
		try {
			if (fileRelativePathList != null && fileRelativePathList.size() > 0) {
				for (int i = 0; i < fileRelativePathList.size(); i++) {
					strPath = fileRelativePathList.get(i);
					addFile = new File(SvnConst.SVN_LOCAL_DIR + "/" + strPath);
					status = ourClientManager.getStatusClient().doStatus(addFile, true);
					// 如果此文件是新增加的则先把此文件添加到版本库，然后提交。
					if (null == status || status.getContentsStatus() == SVNStatusType.STATUS_UNVERSIONED) {
						// 把此文件增加到版本库中
						ourClientManager.getWCClient().doAdd(addFile, false, false, false, SVNDepth.INFINITY, false, false);
						System.out.println("add");
					}
				}
				// 提交此文件
				ourClientManager.getCommitClient().doCommit(new File[] { commitFile }, true, "", null, null, true, false, SVNDepth.INFINITY);
				System.out.println("commit");
			}
			// 如果此文件不是新增加的，直接提交。
			else {
				ourClientManager.getCommitClient().doCommit(new File[] { commitFile }, true, "", null, null, true, false, SVNDepth.INFINITY);
				System.out.println("commit");
			}
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
		// System.out.println(status.getContentsStatus());
		return true;
	}

	/**
	 * Svn提交
	 * 
	 * @param fileRelativePath文件相对路径
	 * @return Boolean
	 */
	public static Boolean doCommit(String fileRelativePath) {
		// 注意：执行此操作要先执行checkout操作。因为本地需要有工作副本此范例才能运行。
		// 初始化支持svn://协议的库
		SVNRepositoryFactoryImpl.setup();

		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		// 实例化客户端管理类
		ourClientManager = SVNClientManager.newInstance((DefaultSVNOptions) options, SvnConst.SVN_USERNAME, SvnConst.SVN_PWD);
		// 要提交的文件夹子
		File commitFile = new File(SvnConst.SVN_LOCAL_DIR);
		// 获取此文件的状态（是文件做了修改还是新添加的文件？）
		SVNStatus status = null;
		File addFile = null;
		try {
			if (fileRelativePath != null && fileRelativePath.trim().length() > 0) {
				addFile = new File(SvnConst.SVN_LOCAL_DIR + "/" + fileRelativePath);
				status = ourClientManager.getStatusClient().doStatus(addFile, true);
				// 如果此文件是新增加的则先把此文件添加到版本库，然后提交。
				if (null == status || status.getContentsStatus() == SVNStatusType.STATUS_UNVERSIONED) {
					// 把此文件增加到版本库中
					ourClientManager.getWCClient().doAdd(addFile, false, false, false, SVNDepth.INFINITY, false, false);
					System.out.println("add");
				}
				// 提交此文件
				ourClientManager.getCommitClient().doCommit(new File[] { commitFile }, true, "", null, null, true, false, SVNDepth.INFINITY);
				System.out.println("commit");
			}
			// 如果此文件不是新增加的，直接提交。
			else {
				ourClientManager.getCommitClient().doCommit(new File[] { commitFile }, true, "", null, null, true, false, SVNDepth.INFINITY);
				System.out.println("commit");
			}
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
		// System.out.println(status.getContentsStatus());
		return true;
	}

	/**
	 * 将文件导入并提交到svn 同路径文件要是已经存在将会报错
	 * 
	 * @param dirPath文件所在文件夹的路径
	 * @return Boolean
	 */
	public static Boolean doImport(String dirPath) {
		/*
		 * For using over http:// and https://
		 */
		DAVRepositoryFactory.setup();
		// 相关变量赋值
		SVNURL repositoryURL = null;
		try {
			repositoryURL = SVNURL.parseURIEncoded(SvnConst.SVN_URL);
		} catch (SVNException e) {
			log.error("", e);
		}

		ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		// 实例化客户端管理类
		ourClientManager = SVNClientManager.newInstance((DefaultSVNOptions) options, SvnConst.SVN_USERNAME, SvnConst.SVN_PWD);
		// 要把此目录中的内容导入到版本库
		File impDir = new File(dirPath);
		// 执行导入操作
		SVNCommitInfo commitInfo = null;
		try {
			commitInfo = ourClientManager.getCommitClient().doImport(impDir, repositoryURL, "import operation!", null, false, false, SVNDepth.INFINITY);
		} catch (SVNException e) {
			log.error("", e);
			return false;
		}
		System.out.println(commitInfo.toString());
		return true;
	}

	public static void main(String[] args) {
		// System.out.println(checkOut());
		doExport(SvnConst.SVN_USERNAME, SvnConst.SVN_PWD, SvnConst.SVN_URL, SvnConst.SVN_LOCAL_DIR);
		//        System.out.println(doCleanup());
		// System.out.println(doCleanup());
	}

}
