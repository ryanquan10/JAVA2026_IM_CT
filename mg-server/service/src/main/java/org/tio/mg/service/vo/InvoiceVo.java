
package org.tio.mg.service.vo;

/**
 * 
 * @author xufei
 * 2020年8月14日 下午4:49:18
 */
public class InvoiceVo implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7314697851001193124L;

	private String period;
	
	private String name;
	
	private Double amount;
	
	private Integer num;
	
	private String remark;
	
	private String nick;

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

}
