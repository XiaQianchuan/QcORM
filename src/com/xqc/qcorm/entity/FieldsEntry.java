package com.xqc.qcorm.entity;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class FieldsEntry extends HashMap<String,Object> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 生成SQL字段
	 * @return
	 */
	public String genSqlSection() {
		StringBuffer sb = new StringBuffer();
		boolean flag = false;
		for(Map.Entry<String, Object> entry : this.entrySet()) {
			if(flag) sb.append(",");
			sb.append(entry.getKey()).append("=");
			Object val = entry.getValue();
			if(val instanceof Boolean) val = (boolean)val ? "1" : "0";
			if(val instanceof String || val instanceof Timestamp) val = "'"+val+"'";
			sb.append(val);
			flag = true;
		}
		return sb.toString();
	}
}
