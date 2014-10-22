package com.xqc.qcorm.sql;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.xqc.qcorm.QcORM;
import com.xqc.qcorm.annotation.Ignore;
import com.xqc.qcorm.exception.QcORMException;

public class Select extends SQLModel implements SQLContext {
	
	private ResourceBundle qcormConf = ResourceBundle.getBundle("qcorm");
	
	Class<?> refClass = null; //对象实体类
	
	StringBuilder sqlBuilder = new StringBuilder();
	
	private String where = "";
	
	private boolean distinct;
	
	private String orderBy;
	
	private String groupBy;
	
	private String having;
	
	private String leftJoin;
	
	private String rightJoin;
	
	private int limit;
	
	/**
	 * 构造函数: 默认
	 */
	public Select() {
	}
	
	/**
	 * 构造函数：传入查询对象类
	 * @param refClass
	 */
	public Select(Class<?> refClass) {
		this.refClass = refClass;
	}
	
	/**
	 * 手动设置查询对象
	 * @param obj
	 * @return
	 */
	public Select setObject(Class<?> refClass) {
		this.refClass = refClass;
		return this;
	}
	
	/**
	 * 获取表名称
	 * @return
	 * @throws QcORMException 
	 */
	public String getTableName() throws QcORMException {
		if(refClass == null) throw new QcORMException("Object class cannot be leave to null");
		String qcKey = refClass.getName();
		String tableName = "";
		try {
			tableName = qcormConf.getString(qcKey);
		} catch (MissingResourceException e) {
			throw new QcORMException("can't find a key named: " + qcKey + " in your config file");
		}
		if(tableName.isEmpty()) {
			throw new QcORMException("can't defined the " + qcKey + " to empty in your config file");
		}
		return tableName;
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
	
	public Select setCondition() {
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
	
	/**
	 * 生成SQL语句
	 * @return
	 * @throws QcORMException 
	 */
	public String genSql() throws QcORMException {
		StringBuffer sqlBuffer = new StringBuffer();
		List<Object> objList = new ArrayList<Object>();
		objList.add(implode(getTableField().toArray(),","));
		objList.add(getTableName());
		if(getWhere().length() > 0) {
			sqlBuffer.append("SELECT %s FROM %s WHERE %s");
			objList.add(getWhere());
		} else {
			sqlBuffer = new StringBuffer("SELECT %s FROM %s");
		}
		return String.format(sqlBuffer.toString(), objList.toArray());
	}
	
	public String[][] query() throws SQLException, QcORMException {
		QcORM qcorm = new QcORM();
		return qcorm.getAllByTDArray(genSql());
	}
	
	/**
	 * 获取对象所有属性
	 * @return
	 */
	private List<String> getAllField() {
		Field[] fields = refClass.getDeclaredFields();
		for(Field field : fields) {
			if(field.isAnnotationPresent(Ignore.class)) continue;
			fieldList.add(field.getName());
		}
		return fieldList;
	}
	
	/**
	 * 解析属性字段，获取查询的数据库表字段名
	 * @return
	 */
	private List<String> getTableField() {
		List<String> filedList = null;
		if(fieldList == null || fieldList.size() == 0) {
			filedList = getAllField();
		} else {
			filedList = fieldList;
		}
		List<String> list = new ArrayList<>();
		for (String field : filedList) {
			String key = refClass.getName()+"."+field;
			list.add(qcormConf.getString(key));
		}
		return list;
	}
	
	public void setWhere(String where) {
//		String filed = where.
//		String newWhere = where.replace("", newChar);
		this.where = where;
	}

	public String getWhere() {
		return where;
	}
}
