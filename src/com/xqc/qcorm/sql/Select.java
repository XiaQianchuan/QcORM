package com.xqc.qcorm.sql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.xqc.qcorm.annotation.Ignore;

public class Select extends SQLModel implements SQLContext {
	
	Class<?> refClass = null;
	
	StringBuilder sqlBuilder = new StringBuilder();
	
	private String where;
	
	private boolean distinct;
	
	private String orderBy;
	
	private String groupBy;
	
	private String having;
	
	private String leftJoin;
	
	private String rightJoin;
	
	private int limit;
	
	public Select() {
		
	}
	
	public Select(Class<?> refClass) {
		this.refClass = refClass;
	}
	
	public Select setObject(Object obj) {
		return this;
	}
	
	/**
	 * 获取对象所有属性
	 * @return
	 */
	public List<String> getAllField() {
		Field[] fields = refClass.getDeclaredFields();
		for(Field field : fields) {
			if(field.isAnnotationPresent(Ignore.class)) continue;
			fieldList.add(field.getName());
		}
		return fieldList;
	}
	
	/**
	 * 设置属性
	 * @param obj
	 * @return
	 */
	public Select setField(Object... obj) {
		for(Object o : obj) {
			fieldList.add(o.toString());
		}
		return this;
	}
	
	public Select distinct() {
		distinct = true;
		return this;
	}
	
	public Select setFUNC(String funcName) {
		return this;
	}
	
	public Select leftJoin(String tableName) {
		leftJoin = "";
		return this;
	}
	
	public Select as() {
		return this;
	}
	
	public Select on() {
		return this;
	}
	
	public Select groupBy() {
		return this;
	}
	
	public Select orderBy() {
		return this;
	}
	
	public Select rightJoin() {
		return this;
	}
	
	public Select limit(int limit) {
		return this;
	}
	
	public Select where() {
		
		return this;
	}
	
	public Select having() {
		return this;
	}
	
	public String genSql() {
		StringBuffer sqlBuffer = new StringBuffer();
		List<Object> objList = new ArrayList<Object>();
		objList.add(implode(fieldList.toArray(),","));
		objList.add("Merchant");
		if(where.length() > 0) {
			sqlBuffer.append("SELECT %s FROM %s WHERE %s");
			objList.add("a=b");
		} else if(orderBy.length() > 0) {
			sqlBuffer.append("SELECT %s FROM %s WHERE %s");
			objList.add("a=b");
		} else {
			sqlBuffer = new StringBuffer("SELECT %s FROM %s");
		}
		return String.format(sqlBuffer.toString(), objList.toArray());
	}
	
	private String implode(Object[] array, String separator) {
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
