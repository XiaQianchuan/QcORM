package com.xqc.qcorm;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.sun.xml.internal.fastinfoset.util.StringArray;
import com.xqc.qcorm.annotation.Ignore;
import com.xqc.qcorm.entity.RelationQuery;
import com.xqc.qcorm.exception.QcORMException;
import com.xqc.qcorm.util.DBHelper;
import com.xqc.qcorm.util.DBUtil;

public class QcORM {
	
	private ResourceBundle qcormConf = ResourceBundle.getBundle("qcorm");
	
	private RelationQuery relationQuery = null;

	/**
	 * 查询数据，封装对象
	 * @param refClass
	 * @param sql
	 * @param args
	 * @return
	 * @throws SQLException
	 * @throws QcORMException
	 */
	private List<?> queryObjectData(Class<?> refClass, String sql, Object... args) throws SQLException, QcORMException {
		List<Object> objList = new ArrayList<Object>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Object obj = null;
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(sql);
			for(int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			rs = ps.executeQuery();
			while(rs.next()) {
				obj = refClass.newInstance();
				Field[] fields = refClass.getDeclaredFields();
				for(Field field : fields) {
					if(field.isAnnotationPresent(Ignore.class)) continue;
					Method method = null;
					try {
						method = refClass.getMethod("set" + DBUtil.getFirstUpperName(field.getName()), field.getType());
					} catch (NoSuchMethodException e) {
						continue;
					}
					if(field.getType().getName().equals(qcormConf.getString(refClass.getName()+"."+field.getName()))) {
						Object obj2 = field.getType().newInstance();
						obj2 = this.getObject(field.getType(), qcormConf.getString(refClass.getName()+"."+field.getName()+".refer")+"=?", rs.getObject(qcormConf.getString(refClass.getName()+"."+field.getName()+".field")));
						method.invoke(obj, obj2);
					} else {
						Object fieldValue = null;
						if(field.getType() == int.class || field.getType() == Integer.class) {
							fieldValue = rs.getInt(qcormConf.getString(refClass.getName()+"."+field.getName()));
						} else if(field.getType() == Boolean.class || field.getType() == boolean.class) {
							fieldValue = rs.getBoolean(qcormConf.getString(refClass.getName()+"."+field.getName()));
						} else if(field.getType() == Long.class) {
							fieldValue = rs.getLong(qcormConf.getString(refClass.getName()+"."+field.getName()));
						} else if(field.getType() == String.class) {
							fieldValue = rs.getString(qcormConf.getString(refClass.getName()+"."+field.getName()));
						} else {
							fieldValue = rs.getObject(qcormConf.getString(refClass.getName()+"."+field.getName()));
						}
						method.invoke(obj, fieldValue);
					}
				}
				objList.add(obj);
			}
		} catch(InstantiationException e) {
			throw new QcORMException("Can't instantiation the class " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new QcORMException("Illegal access the class or method of " + e.getMessage());
		} catch (InvocationTargetException e) {
			throw new QcORMException("Invocation target exception of " + e.toString());
		} finally {
			DBUtil.closeConnection(conn, ps, rs);
		}
		return objList;
	}
	
	/**
	 * 获取单个对象
	 * @param refClass 对象类
	 * @param qql  QCORM条件查询语句
	 * @param args 条件参数
	 * @return 对象
	 * @throws SQLException
	 * @throws QcORMException
	 */
	public Object getObject(Class<?> refClass, String qql, Object... args) throws SQLException, QcORMException {
		String sql = createSQL(refClass, qql, 0, 0);
		List<?> objList = queryObjectData(refClass, sql, args);
		return objList.size() > 0 ? objList.get(0) : null;
	}
	
	/**
	 * 获取多个对象
	 * @param refClass
	 * @return 对象List
	 * @throws SQLException
	 * @throws QcORMException
	 */
	public List<?> getObjects(Class<?> refClass) throws SQLException, QcORMException {
		return getObjects(refClass, "");
	}
	
	/**
	 * 获取多个对象
	 * @param refClass
	 * @param qql
	 * @param args
	 * @return
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws QcORMException
	 */
	public List<?> getObjects(Class<?> refClass, String qql, Object... args) throws SQLException, QcORMException {
		String sql = createSQL(refClass, qql, 0, 0);
		return queryObjectData(refClass, sql, args);
	}
	
	/**
	 *  分页获取多个对象
	 * @param refClass
	 * @param pageSize
	 * @param pageIndex
	 * @param orderBy 排序参数
	 * @param daysParam
	 * @return List [0]:总数 [1]:对象列表
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws QcORMException
	 */
	public List<?> getObjects(Class<?> refClass, int pageSize, int pageIndex) throws SQLException, QcORMException {
		List<Object> list = new ArrayList<Object>();
		String sql1 = createSQL(refClass, "", 0, 0);
		list.add(this.getSingleFiled(sql1.replaceFirst("\\*", "COUNT(*)")));
		String sql2 = createSQL(refClass, "", pageSize, pageIndex);
		list.add(queryObjectData(refClass, sql2));
		return list;
	}
	
	/**
	 * 分页获取多个对象
	 * @param refClass
	 * @param pageSize
	 * @param pageIndex
	 * @param qql
	 * @param args
	 * @return
	 * @throws SQLException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws QcORMException
	 */
	public List<?> getObjects(Class<?> refClass, int pageSize, int pageIndex, String qql, Object... args) throws SQLException, QcORMException {
		List<Object> list = new ArrayList<Object>();
		String sql1 = createSQL(refClass, qql, 0, 0);
		list.add(this.getSingleFiled(sql1.replaceFirst("(?i)(SELECT\\s)(\\S+)(?=\\sFROM)", "SELECT COUNT(*)"),args));
		String sql2 = createSQL(refClass, qql, pageSize, pageIndex);
		list.add(queryObjectData(refClass, sql2, args));
		return list;
	}
	
	/**
	 * 获得位于查询结果第一行第一列单元格内的数据(单个值)
	 * @param sql 查询命令
	 * @param args 格式化参数
	 * @return 查询到的结果 如为null已转换为""
	 * @throws SQLException 数据库操作异常
	 */
	public Object getSingleFiled(String sql, Object... args) throws SQLException {
		showSql(sql, args);
		Object result = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(sql);
			for(int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			rs = ps.executeQuery();
			if(rs.next()) {
				if(null != rs.getObject(1) && rs.getObject(1).getClass() == Boolean.class) {
					result = rs.getInt(1);
				} else {
					result = rs.getObject(1);
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
			DBUtil.closeConnection(conn, ps, rs);
		}
		return result;
	}
	
	/**
	 * 获得位于查询结果第一列的数据
	 * @param sql 查询列命令
	 * @param args 格式化参数
	 * @return 查询到的单列结果 String数组
	 * @throws SQLException 数据库操作异常
	 */
	public String[] getSingleColumn(String sql, Object... args) throws SQLException {
		showSql(sql, args);
		String[] array;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(sql);
			for(int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			rs = ps.executeQuery();
			rs.last();
			array = new String[rs.getRow()];
			rs.beforeFirst();
			int i = 0;
			while(rs.next()) {
				array[i++] = rs.getString(1);
			}
		} finally {
			DBUtil.closeConnection(conn, ps, rs);
		}
		return array;
	}
	
	/**
	 * 查询返回字符二维数组数据 示例：findMany("select * from UserInfo");
	 * @param sql 查询命令
	 * @param args 格式化参数
	 * @return 查询结果的String二维数组形式
	 * @throws SQLException 数据库操作异常
	 */
	public String[][] getAllByTDArray(String sql, Object... args) throws SQLException {
		showSql(sql, args);
		String[][] result = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(sql);
			for(int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			rs = ps.executeQuery();
			rs.last();
			int row = rs.getRow();
			rs.beforeFirst();
			ResultSetMetaData rsmd = rs.getMetaData();
			int col = rsmd.getColumnCount();
			result = new String[row][col];
			int i = 0;
			while(rs.next()) {
				for(int j = 0; j < col; j++) {
					if("TIMESTAMP".equals(rsmd.getColumnTypeName(j+1))) {
						String timeStr = rs.getTimestamp(j+1).toString();
						result[i][j] = timeStr.substring(0, timeStr.indexOf('.'));
					} else {
						result[i][j] = rs.getString(j+1);
					}
				}
				i++;
			}
		} finally {
			if (null != rs) {
				rs.close();
			}
			if (null != ps) {
				ps.close();
			}
			if (null != conn) {
				conn.close();
			}
		}
		return result;
	}
	
	/**
	 * 保存对象
	 * @param obj
	 * @return 保存插入的ID
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws SQLException
	 * @throws QcORMException 
	 */
	public int saveObject(Object obj) throws SQLException, QcORMException {
		String tabName = qcormConf.getString(obj.getClass().getName());
		StringBuffer insertSql = new StringBuffer("INSERT INTO `"+tabName+"`(");
		StringBuffer valuesSql = new StringBuffer(" VALUES(");
		Boolean fieldFlag = false;
		Iterator<Entry<String, Object>> iterator = getObjectDatabase(obj).entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
			String colName = entry.getKey();
			Object colVal = entry.getValue();
			if(fieldFlag) {
				insertSql.append(",");
				valuesSql.append(",");
			}
			insertSql.append("`" + colName + "`");
			if(colVal instanceof String || colVal instanceof Timestamp) {
				valuesSql.append("'"+colVal.toString()+"'");
			} else {
				valuesSql.append(colVal.toString());
			}
			fieldFlag = true;
		}
		insertSql.append(")").append(valuesSql).append(")");
		
		return insert(insertSql.toString());
	}
	
	/**
	 * 保存多个对象
	 * @param objList
	 * @return
	 * @throws SQLException
	 * @throws QcORMException
	 */
	public int saveObjects(Object[] objList) throws SQLException, QcORMException {
		StringBuffer insertSql = new StringBuffer("INSERT INTO `");
		StringBuffer valuesSql = new StringBuffer(" VALUES(");
		boolean firstFlag = true;
		for(Object obj : objList) {
			if(firstFlag) {
				String tabName = qcormConf.getString(obj.getClass().getName());
				insertSql.append(tabName+"`(");
			} else {
				valuesSql.append(",(");
			}
			Boolean fieldFlag = false;
			Iterator<Entry<String, Object>> iterator = getObjectDatabase(obj).entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
				String colName = entry.getKey();
				Object colVal = entry.getValue();
				if(firstFlag) {
					if(fieldFlag) insertSql.append(",");
					insertSql.append("`" + colName + "`");
				}
				if(fieldFlag) valuesSql.append(",");
				if(colVal instanceof String || colVal instanceof Timestamp) {
					valuesSql.append("'"+colVal.toString()+"'");
				} else {
					valuesSql.append(colVal.toString());
				}
				fieldFlag = true;
			}
			valuesSql.append(")");
			firstFlag = false;
		}
		insertSql.append(")").append(valuesSql);
		return insert(insertSql.toString());
	}
	
	/**
	 * 更新对象
	 * @param obj
	 * @return 保存插入的ID
	 * @throws SQLException
	 * @throws QcORMException 
	 */
	public int updateObject(Object obj, String qql, Object... args) throws SQLException, QcORMException {
		String tabName = qcormConf.getString(obj.getClass().getName());
		StringBuffer updateSql = new StringBuffer("UPDATE `"+tabName+"` SET ");
		Boolean fieldFlag = false;
		String[] params = qql.split("\\s?,\\s?");
		StringArray sa = new StringArray();
		for(String s : params) {
			sa.add(s.split("\\s?=\\s?")[0]);
		}
		Iterator<Entry<String, Object>> iterator = getObjectDatabase(obj,sa.getArray()).entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
			String colName = entry.getKey();
			Object colVal = entry.getValue();
			if(fieldFlag) updateSql.append(",");
			updateSql.append("`" + colName + "`=");
			if(colVal instanceof String || colVal instanceof Timestamp) {
				updateSql.append("'"+colVal.toString()+"'");
			} else {
				updateSql.append(colVal.toString());
			}
			fieldFlag = true;
		}
		updateSql.append(analyseQql(obj.getClass(), qql));
		return execute(updateSql.toString(), args);
	}
	
	/**
	 * 删除对象
	 * @param obj
	 * @return 保存删除的ID
	 * @throws SQLException
	 * @throws QcORMException 
	 */
	public int deleteObject(Object obj, String qql, Object... args) throws SQLException, QcORMException {
		String tabName = qcormConf.getString(obj.getClass().getName());
		StringBuffer deleteSql = new StringBuffer("DELETE FROM `"+tabName+"`");
		Boolean fieldFlag = false;
		String[] params = qql.split("\\s?,\\s?");
		StringArray sa = new StringArray();
		for(String s : params) {
			sa.add(s.split("\\s?=\\s?")[0]);
		}
		Iterator<Entry<String, Object>> iterator = getObjectDatabase(obj,sa.getArray()).entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
			String colName = entry.getKey();
			Object colVal = entry.getValue();
			if(fieldFlag) deleteSql.append(",");
			deleteSql.append("`" + colName + "`=");
			if(colVal instanceof String || colVal instanceof Timestamp) {
				deleteSql.append("'"+colVal.toString()+"'");
			} else {
				deleteSql.append(colVal.toString());
			}
			fieldFlag = true;
		}
		deleteSql.append(analyseQql(obj.getClass(), qql));
		return execute(deleteSql.toString(), args);
	}
	
	/**
	 * 获取对象数据库结构
	 * @param obj
	 * @return Map<key:数据库字段名,value:数据库值>
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws QcORMException 
	 */
	public Map<String, Object> getObjectDatabase(Object obj, String... except) throws QcORMException {
		Class<?> refClass = obj.getClass();
		Map<String, Object> hashMap = new HashMap<String, Object>();
		Field[] fields = refClass.getDeclaredFields();
		for(Field field : fields) {
			if(except.length > 0) {
				boolean ifContinue = false;
				for(int i=0; i < except.length; i++) {
					if(field.getName().equals(except[i])) {
						ifContinue = true;
						break;
					}
				}
				if(ifContinue) continue;
			}
			Method method = null;
			try {
				method = refClass.getMethod("get" + DBUtil.getFirstUpperName(field.getName()));
			} catch(NoSuchMethodException e) {
				continue;
			}
			Object val = null;
			try {
				val = method.invoke(obj);
				if(null != val) {
					String key = refClass.getName()+"."+field.getName();
					String value = qcormConf.getString(key);
					if(qcormConf.containsKey(value)) {
						Object val2 = val.getClass().getMethod("get" + DBUtil.getFirstUpperName(qcormConf.getString(key+".refer"))).invoke(val);
						hashMap.put(qcormConf.getString(key+".field"), val2);
					} else {
						hashMap.put(value, val);
					}
				}
			} catch (IllegalAccessException e) {
				throw new QcORMException("Illegal access the method of " + method.getName());
			} catch (InvocationTargetException e) {
				throw new QcORMException("Invocation method of " + method.getName() + "" + e.toString());
			} catch (NoSuchMethodException e) {
				throw new QcORMException("Cant find method Error:" + e.toString());
			}
		}
		return hashMap;
	}
	
	/**
	 * 插入并返回序列ID
	 * @param sql 插入命令
	 * @param args 格式化参数
	 * @return 插入记录的序列ID
	 * @throws SQLException 数据库操作异常
	 */
	public int insert(String sql, Object... args) throws SQLException {
		showSql(sql, args);
		int result = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			for(int i = 0; i < args.length; ++i) {
				ps.setObject(i + 1, args[i]);
			}
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				result = rs.getInt(1);
			}
		} finally {
			DBUtil.closeConnection(conn, ps, rs);
		}
		return result;
	}
	
	public int execute(String sql, Object... args) throws SQLException {
		showSql(sql, args); //显示调试SQL
		int count = -1;
		PreparedStatement ps = null;
		Connection conn = null;
		try {
			conn = DBUtil.getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			count = ps.executeUpdate();
		} finally {
			if (null != ps) {
				ps.close();
			}
		}
		return count;
	}
	
	/**
	 * 生成SQL语句
	 * @param refClass
	 * @param qql qcorm查询语句
	 * @param pageSize 每页显示数
	 * @param pageIndex 当前页数
	 * @return
	 * @throws QcORMException
	 */
	private String createSQL(Class<?> refClass, String qql, int pageSize, int pageIndex) throws QcORMException {
		String sql = "";
		if(qql.toLowerCase().trim().indexOf("select")==0) {
			sql = qql;
			if(pageSize > 0 && pageIndex > 0 && sql.toLowerCase().indexOf("limit") == -1) {
				sql += " LIMIT " + pageSize * (pageIndex - 1) + "," + pageSize;
			}
		} else {
			sql = analyseQql(refClass, qql);
			if(pageSize > 0 && pageIndex > 0 && sql.toLowerCase().indexOf("limit") == -1) {
				sql += " LIMIT " + pageSize * (pageIndex - 1) + "," + pageSize;
			}
			if(null != relationQuery && null != relationQuery.getRelationSql()) {
				sql = relationQuery.getRelationSql().toString() + sql;
				relationQuery = null;
			} else {
				String headSql = "SELECT * FROM ";
				try {
					String tabName = qcormConf.getString(refClass.getName());
					headSql += "`" + tabName + "`";
				} catch(MissingResourceException e) {
					throw new QcORMException("Can't find the object of " + e.getKey());
				}
				sql = headSql + sql;
			}
		}
		return sql;
	}
	
	/**
	 * 解析QQL语句
	 * @param refClass
	 * @param qql
	 * @return
	 */
	private String analyseQql(Class<?> refClass, String qql) throws QcORMException {
		String sql = "";
		if(qql.trim().length() > 0) {
			if(qql.toLowerCase().startsWith("where ")) {
				sql = " " + qql;
			} else {
				boolean flagAnd = false;
				String where = "";
				String order = "";
				String limit = "";
				String[] params = qql.split("\\s?;\\s?");
				for(String param : params) {
					param = param.trim();
					if(param.toLowerCase().startsWith("limit ")) {
						limit = " " + param;
					} else if(param.toLowerCase().startsWith("order by ")) {
						order = " ORDER BY ";
						boolean flagMulti = false;
						for(String attr : param.replaceAll("(?i)order\\s+by\\s+","").split("\\s?,\\s?")) {
							if(flagMulti) order += ",";
							String way = attr.toLowerCase().indexOf(" desc") > 0 ? " DESC" : " ASC"; 
							attr = attr.replaceAll("(?i)\\s(desc|asc)", "").trim();
							order += getFieldName(refClass, attr) + way;
							flagMulti = true;
						}
					} else if(param.toLowerCase().startsWith("interval ")) {
						where += where.indexOf("WHERE ")>0 ? " AND " : " WHERE ";
						String[] info = param.replaceAll("(?i)interval\\s+", "").split("\\s+");
						int interval = Integer.parseInt(info[0]);
						String dayColumn = getFieldName(refClass, info[2]);
						String symbol = info[1];
						if("eq".equals(symbol)) symbol = "=";
						else if("gt".equals(symbol)) symbol = ">";
						else if("lt".equals(symbol)) symbol = "<";
						else if("ge".equals(symbol)) symbol = ">=";
						else if("le".equals(symbol)) symbol = "<=";
						where += "DATE_FORMAT(DATE_SUB(NOW(), INTERVAL "+interval+" DAY),'%y%m%d')"+symbol+"DATE_FORMAT("+dayColumn+",'%y%m%d')";
					} else {
						where += where.indexOf("WHERE ")>0 ? " AND " : " WHERE ";
						for(String attr : param.split("\\s?,\\s*(?!\\d[,\\)])")) {
							if(flagAnd) where += " AND ";
							if(attr.toLowerCase().indexOf(" in") > 0 && attr.toLowerCase().indexOf("(") > 0) {
								String[] qqlAttr = attr.split("(?i)\\sin\\s?(?=\\()");
								String colName = getFieldName(refClass, qqlAttr[0]);
								String colVal = qqlAttr[1];
								where += (colName+" IN "+colVal);
							} else {
								String[] qqlAttr = attr.split("\\s?=\\s?");
								String colName = getFieldName(refClass, qqlAttr[0]);
								String colVal = qqlAttr[1];
								where += (colName+"="+colVal);
							}
							flagAnd = true;
						}
					}
				}
				sql = where + order + limit;
			}
		}
		return sql;
	}
	
	/**
	 * 获取数据库字段名
	 * @param key
	 * @return
	 */
	private String getFieldName(Class<?> refClass, String name) throws QcORMException {
		String fieldName = "";
		String baseClassQualifiedName = refClass.getName();
		if(name.indexOf(".") > 0) {
			try {
				String[] sa = name.split("\\.");
				String attrRefer = qcormConf.getString(baseClassQualifiedName+"."+sa[0]+".refer");
				//是否有关联查询
				if(sa[1].equals(attrRefer)) {
					fieldName = qcormConf.getString(baseClassQualifiedName+"."+sa[0]+".field");
				} else {
					//主表名称
					String mainTable = qcormConf.getString(baseClassQualifiedName);
					if(null == relationQuery) relationQuery = new RelationQuery(mainTable);
					//从表名称
					String refClassQualifiedName = qcormConf.getString(baseClassQualifiedName+"."+sa[0]);
					String slaveTable = qcormConf.getString(refClassQualifiedName);
					String leftCase = qcormConf.getString(baseClassQualifiedName+"."+sa[0]+".field");
					String rightCase = qcormConf.getString(refClassQualifiedName+".pkey");
					relationQuery.setLeftJoin(mainTable, slaveTable, leftCase, rightCase);
					for(int i=1; i<sa.length; i++) {
						if(i == sa.length-1) {
							fieldName = qcormConf.getString(refClassQualifiedName+"."+sa[i]);
						} else {
							if(sa[i+1].equals(qcormConf.getString(refClassQualifiedName+"."+sa[i]+".refer"))) {
								fieldName = qcormConf.getString(refClassQualifiedName+"."+sa[i]+".field");
								break;
							} else {
								leftCase = qcormConf.getString(refClassQualifiedName+"."+sa[i]+".field");
								refClassQualifiedName = qcormConf.getString(refClassQualifiedName+"."+sa[i]);
								mainTable = slaveTable;
								slaveTable = qcormConf.getString(refClassQualifiedName);
								rightCase = qcormConf.getString(refClassQualifiedName+".pkey");
								relationQuery.setLeftJoin(mainTable, slaveTable, leftCase, rightCase);
							}
						}
					}
					fieldName = relationQuery.getSlaveTableNames().get(slaveTable)+"."+fieldName;
				}
			} catch(MissingResourceException e) {
				throw new QcORMException("Can't find Object's attribute Exception:" + e.getKey());
			}
		} else {
			try {
				fieldName = qcormConf.getString(baseClassQualifiedName+"."+name);
			} catch(MissingResourceException e) {
				fieldName = name.indexOf("`")>-1 ? name : "`"+name+"`";
			}
		}
		return fieldName;
	}
	
	/**
	 * 显示调试SQL
	 * @param sql
	 */
	private void showSql(String sql, Object... params) {
		if(DBHelper.dbcfg.containsKey("qcorm.showSql") && Boolean.parseBoolean(DBHelper.dbcfg.getString("qcorm.showSql"))){
			if(params.length > 0) {
				try {
					sql = String.format(sql.replace("?","%s"), params);
				} catch (Exception e) {}				
			}
			System.out.println("[QCORM_SQL]" + sql);
		}
	}
}
