
package org.tio.sitexxx.service.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 负载数据
 * @author tanyaowu 
 * 2016年10月8日 下午4:39:03
 */
public class LoadData implements java.io.Serializable {
	private static final long	serialVersionUID	= 6605697801713948836L;
	@SuppressWarnings("unused")
	private static Logger		log					= LoggerFactory.getLogger(LoadData.class);

	/**
	 * 
	 * @author: tanyaowu
	 */
	public LoadData() {
	}

	private int	pcCount;
	private int	appCount;
	//	private int iosCount;

	public int getPcCount() {
		return pcCount;
	}

	public void setPcCount(int pcCount) {
		this.pcCount = pcCount;
	}

	public int getAppCount() {
		return appCount;
	}

	public void setAppCount(int appCount) {
		this.appCount = appCount;
	}

	//	public int getIosCount() {
	//		return iosCount;
	//	}
	//
	//	public void setIosCount(int iosCount) {
	//		this.iosCount = iosCount;
	//	}

	//	/**
	//	 * @param args
	//	 * @author: tanyaowu
	//	 */
	//	public static void main(String[] args) {
	//
	//	}

}
