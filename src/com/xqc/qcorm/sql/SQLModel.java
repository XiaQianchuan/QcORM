package com.xqc.qcorm.sql;

public class SQLModel {
	
	/**
	 * 执行命令
	 */
	 String cmd;
	
	 /**
	  * 用指定的符号将集合组成字符串
	  * @param array
	  * @param separator
	  * @return
	  */
	 protected String implode(Object[] array, String separator) {
		StringBuffer sb = new StringBuffer();
		boolean flag = false;
		for(Object s : array) {
			if(flag) sb.append(separator);
			else flag = true;
			sb.append(s.toString());
		}
		return sb.toString();
	}
}
