
package org.tio.mg.service.utils;

import java.util.Date;
import java.util.Objects;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.tio.sitexxx.service.utils.ChineseYuanUtil;
import org.tio.sitexxx.service.vo.Const;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

/**
 * 周期工具类
 * @author xufei
 */
public class PeriodUtils {

	/**
	 * 周期转换
	 * @param date
	 * @param periodType
	 * @return
	 * @author xufei
	 */
	public static String dateToPeriodByType(Date date, short periodType) {
		if (periodType == Const.PeriodType.TOTAL) {
			return Const.PeriodType.TOTAL_PERIOD;
		}
		String periodFormat = "yyyyMMdd";
		if (Objects.equals(periodType, Const.PeriodType.YEAR)) {
			periodFormat = periodFormat.substring(0, 4);
		} else if (Objects.equals(periodType, Const.PeriodType.MONTH)) {
			periodFormat = periodFormat.substring(0, 6);
		} else if (Objects.equals(periodType, Const.PeriodType.WEEK)) {
			date = DateUtil.beginOfWeek(date);
		} else if (Objects.equals(periodType, Const.PeriodType.QUARTER)) {
			date = DateUtil.beginOfQuarter(date);
		} else if (Objects.equals(periodType, Const.PeriodType.HOUR)) {
			periodFormat = "HH";
		} else if (Objects.equals(periodType, Const.PeriodType.TIME)) {
			periodFormat = "HH:mm";
		}
		String period = DateUtil.format(date, periodFormat);
		if (Objects.equals(periodType, Const.PeriodType.WEEK)) {
			period += "W";
		} else if (Objects.equals(periodType, Const.PeriodType.QUARTER)) {
			period += "Q";
		}

		return period;
	}

	/**
	 * 根据周期转换时间
	 * @param period
	 * @return
	 * @author xufei
	 */
	public static DateTime getDateByPeriod(String period) {
		int length = period.length();
		if (length == 5) {
			return new DateTime();
		}
		if (period.indexOf("W") >= 0) {
			period = period.substring(0, period.length() - 1);
		}
		if (period.indexOf("Q") >= 0) {
			period = period.substring(0, period.length() - 1);
		}
		if (period.length() == 4) {
			period = period + "0101";
		}
		if (period.length() == 6) {
			period = period + "01";
		}

		if (period.length() == 10) {
			period = period + "0000";
		}
		return DateUtil.parse(period);
	}

	/**
	 * 获取天数
	 * @param periodType
	 * @param num
	 * @return
	 */
	public static int getDayByPeriodNum(Short periodType, Integer num) {
		int result = num;
		switch (periodType) {
		case Const.PeriodType.DAY:
			break;
		case Const.PeriodType.WEEK:
			result = num * 7;
			break;
		case Const.PeriodType.MONTH:
			result = num * 30;
			break;
		case Const.PeriodType.QUARTER:
			result = num * 90;
			break;
		case Const.PeriodType.YEAR:
			result = num * 365;
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * 卡劵周期创建
	 * @param periodType
	 * @param num
	 * @param date
	 * @return
	 */
	/**
	 * @param periodType
	 * @param num
	 * @param date
	 * @return
	 */
	public static Date getCouponPeriod(Short periodType, Integer num, Date date) {
		if (num <= 0) {
			num = 1;
		}
		if (date == null) {
			date = new DateTime();
		}
		Date result = null;
		switch (periodType) {
		case Const.PeriodType.DAY:
			result = DateUtil.offsetDay(date, num);
			break;
		case Const.PeriodType.WEEK:
			result = DateUtil.offsetWeek(date, num);
			break;
		case Const.PeriodType.MONTH:
			result = DateUtil.offsetMonth(date, num);
			break;
		case Const.PeriodType.QUARTER:
			result = DateUtil.offsetMonth(date, num * 3);
			break;
		case Const.PeriodType.YEAR:
			result = DateUtil.offsetMonth(date, num * 12);
			break;
		default:
			result = DateUtil.offsetDay(date, num);
			break;
		}
		return result;
	}

	public static class InnerVo {
		private String period;

		private String name;

		private Double accout;

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

		public Double getAccout() {
			return accout;
		}

		public void setAccout(Double accout) {
			this.accout = accout;
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

	@SuppressWarnings("unused")
	private static void setcell(XSSFRow creRow, XSSFRow row, int index, String value) {
		XSSFCell cell = row.getCell(index);
		CellType type = cell.getCellType();
		XSSFCell cell1 = creRow.createCell(index, type);
		cell1.setCellStyle(row.getCell(index).getCellStyle());
		cell1.setCellValue(value);
	}

	public static void main(String[] args) {
		//		List<InnerVo> rows = new ArrayList<PeriodUtils.InnerVo>();
		//		for(int i = 0 ; i < 5; i++) {
		//			InnerVo innerVo = new InnerVo();
		//			innerVo.setPeriod("2020.07.24");
		//			innerVo.setName("报销内容" + i);
		//			innerVo.setNum(i);
		//			innerVo.setAccout((i + 1) * 0.5d);
		//			innerVo.setRemark("");
		//			innerVo.setNick("");
		//			rows.add(innerVo);
		//		}
		//		InnerVo innerVo = new InnerVo();
		//		innerVo.setPeriod("");
		//		innerVo.setName("");
		//		innerVo.setNum(null);
		//		innerVo.setAccout(null);
		//		innerVo.setRemark("");
		//		innerVo.setNick("");
		//		rows.add(innerVo);
		//		InnerVo innerVo1 = new InnerVo();
		//		innerVo1.setPeriod("合计人民币");
		//		innerVo1.setName("");
		//		innerVo1.setNum(null);
		//		innerVo1.setAccout(10000d);
		//		innerVo1.setRemark("报销人:");
		//		innerVo1.setNick("屠玉明");
		//		rows.add(innerVo1);
		//		
		////		File excelTemplate = new File(ResourceUtil.getAbsolutePath(MgConst.INVOICE_EXCEL_TEMPLATE));
		//		ExcelReader reader = ExcelUtil.getReader("d:/template.xlsx");
		//		ExcelWriter writer = reader.getWriter();
		//		Workbook wrok = reader.getWorkbook();
		//		XSSFSheet sheet =  (XSSFSheet) wrok.cloneSheet(0);
		//		wrok.setSheetName(1, "报销"); // 给sheet命名
		//		List<Map<String, Object>> ineser = new ArrayList<>();
		//        for (int i = 0; i < 5; i++) {
		//            Map data = new HashMap<>();
		//            data.put("time", "2020.08.0" + i);
		//            data.put("type", "费用类别" + i);
		//            data.put("content", "报销内容" + i);
		//            data.put("num", (i + 1) * 1);
		//            data.put("ammout", 100d * (i + 1));
		//            ineser.add(data);
		//        }
		//        int insert = 5 - 1;
		//        int next = 4 + insert;
		//     // 插入行
		//        sheet.shiftRows(4, 4 + ineser.size(), insert, true, false);// 第1个参数是指要开始插入的行，第2个参数是结尾行数,第三个参数表示动态添加的行数
		//        XSSFRow row = sheet.getRow(next);
		//        for (int i = 0; i < insert; i++) {
		//        	XSSFRow creRow = sheet.createRow(4 + i);
		//        	setcell(creRow, row, 0, ineser.get(i).get("time").toString());
		//        	setcell(creRow, row, 1, ineser.get(i).get("type").toString());
		//        	setcell(creRow, row, 2, ineser.get(i).get("content").toString());
		//        	
		//        	XSSFCell cell4 = row.getCell(3);
		//    		CellType type4 = cell4.getCellType();
		//    		XSSFCell cell41 = creRow.createCell(3, type4);
		//    		cell41.setCellStyle(cell4.getCellStyle());
		//    		cell41.setCellValue((int)ineser.get(i).get("num"));
		//    		XSSFCell cell = row.getCell(4);
		//    		CellType type = cell.getCellType();
		//    		XSSFCell cell1 = creRow.createCell(4, type);
		//        	cell1.setCellStyle(cell.getCellStyle());
		//        	cell1.setCellValue((double)ineser.get(i).get("ammout"));
		//        }
		//        for (int i = 0; i < 1; i++) {
		//        	XSSFRow update = sheet.getRow(next + i);
		//        	update.getCell(0).setCellValue(ineser.get(insert + i).get("time").toString());
		//        	update.getCell(1).setCellValue(ineser.get(insert + i).get("type").toString());
		//        	update.getCell(2).setCellValue(ineser.get(insert + i).get("content").toString());
		//        	update.getCell(3).setCellValue((int)ineser.get(insert + i).get("num"));
		//        	update.getCell(4).setCellValue((double)ineser.get(insert + i).get("ammout"));
		//        }
		//        XSSFRow update = sheet.getRow(5 + 4);
		//        update.getCell(0).setCellValue("总金额（大写）：人民币" + ChineseYuanUtil.convert("12345.25") + "整");
		//        update.getCell(4).setCellValue(12345.25d);
		//        XSSFRow update1 = sheet.getRow(5 + 4 + 1);
		//        update1.getCell(0).setCellValue("报销人：徐飞");
		//        wrok.removeSheetAt(0);
		//        File destFile = FileUtil.file("d:/报销单模板2222.xlsx");
		//        writer.flush(destFile);
		////		ExcelWriter writer = ExcelUtil.getWriter("d:/writeTest.xlsx");
		////		writer.merge(5, "费用报销单20200814");
		////		writer.addHeaderAlias("period", "日期");
		////		writer.addHeaderAlias("name", "报销内容");
		////		writer.addHeaderAlias("num", "单据张数");
		////		writer.addHeaderAlias("accout", "金额");
		////		writer.addHeaderAlias("remark", "备注");
		////		writer.addHeaderAlias("nick", "");
		////		writer.write(rows, true);
		//		//关闭writer，释放内存
		//		writer.close();
		//		System.out.println(DateUtil.format(new Date(), "yyyy.MM.dd"));

		System.out.println(ChineseYuanUtil.convert("33.05"));

	}

}
