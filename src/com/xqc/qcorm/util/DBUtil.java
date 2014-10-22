package com.xqc.qcorm.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.rowset.CachedRowSet;

import com.xqc.qcorm.util.CryptoUtil;
import com.xqc.qcorm.util.LoggerUtil;
import com.sun.rowset.CachedRowSetImpl;

/**
 * 数据库工具类
 * @author Xqc
 * @date 2013.07.22
 */
public class DBUtil {

	private static String database = "";
	private static String driver = "";
	private static String url = "";
	private static String username = "";
	private static String password = "";

	static {
		try {
			database = DBHelper.dbcfg.getString("db.database");
			driver = DBHelper.dbcfg.getString("db.driver");
			url = CryptoUtil.generalDecrypt(DBHelper.dbcfg.getString("db.url"));
			username = CryptoUtil.generalDecrypt(DBHelper.dbcfg.getString("db.username"));
			password = CryptoUtil.generalDecrypt(DBHelper.dbcfg.getString("db.password"));
			Class.forName(driver);
		} catch(Exception e) {
			LoggerUtil.log.error("Database init error!", e);
		}
	}

	/**
	 * 建立JDBC基本连接
	 * @return 基本的JDBC链接对象
	 * @throws ClassNotFoundException 驱动类加载异常
	 * @throws SQLException 数据库操作异常
	 * @return JDBC Connection
	 */
	protected static Connection getBaseConnection() throws ClassNotFoundException, SQLException {
		return DriverManager.getConnection(url, username, password);
	}

	/***
	 * 从连接池里获得连接
	 * @return Connection of pool
	 */
	public static Connection getConnection() {
		Connection conn = null;
//		try {
//			conn = connectionPool.getConnection();
//		} catch (SQLException e) {
//			LoggerUtil.log.error("Get connection from pool error!",e);
//			if (conn == null) {
//				try {
//					conn = getBaseConnection();
//					LoggerUtil.log.warn("Are using the basic connections!");
//				} catch (Exception e1) {
//					LoggerUtil.log.error("Basic connections did not work!",e1);
//				}
//			}
//		}
		try {
			conn = getBaseConnection();
		} catch (Exception e) {
			LoggerUtil.log.error("JDBC basic connections did not work!", e);
		}
		return conn;
	}
	
	/**
	 * 关闭数据库连接
	 * @param conn
	 * @param ps
	 * @param rs
	 * @throws SQLException
	 */
	public static void closeConnection(Connection conn, PreparedStatement ps, ResultSet rs) throws SQLException {
		if(null != rs) {
			rs.close();
		}
		if(null != ps) {
			ps.close();
		}
		if(null != conn) {
			conn.close();
		}
	}

	/**
	 * 执行插入、更新、删除SQL命令
	 * @param sql 增删改命令
	 * @param args 格式化参数
	 * @return 执行影响数据条数
	 * @throws SQLException 数据库操作异常
	 * @throws ClassNotFoundException 驱动类加载异常
	 * @throws IOException 自动启动数据库异常
	 */
	public static int execute(String sql, Object... args) throws SQLException{
		int result = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			result = ps.executeUpdate();
		} finally {
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
	 * 插入并返回序列ID
	 * @param sql 插入命令
	 * @param args 格式化参数
	 * @return 插入记录的序列ID
	 * @throws SQLException 数据库操作异常
	 */
	public static int insert(String sql, Object... args) throws SQLException {
		int result = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql,1);
			for (int i = 0; i < args.length; ++i){
				ps.setObject(i + 1, args[i]);
			}
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()){
				result = rs.getInt(1);
			}
		} finally {
			if (rs != null){
				rs.close();
			}
			if (ps != null){
				ps.close();
			}
			if (conn != null){
				conn.close();
			}
		}
		return result;
	}

	/**
	 * 插入并返回序列ID 事务
	 * @param sql 支持事务插入命令
	 * @param args 格式化参数
	 * @return 插入记录的序列ID
	 * @throws SQLException 数据库操作异常
	 */
	public static int transInsert(Connection conn,String sql, Object... args) throws SQLException {
		int result = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql,1);
			for (int i = 0; i < args.length; ++i){
				ps.setObject(i + 1, args[i]);
			}
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()){
				result = rs.getInt(1);
			}
		} finally {
			if (rs != null){
				rs.close();
			}
			if (ps != null){
				ps.close();
			}
		}
		return result;
	}

	/**
	 * 支持事务 执行插入、更新、删除SQL命令
	 * @param conn 请传入setAutoCommit(false)的连接对象 ,sql SQL命令,预编译参数
	 * @param sql 增删改命令
	 * @param args 格式化参数
	 * @return 影响数据条数 最后请手动conn.commit()并关闭连接
	 * @throws SQLException 数据库操作异常
	 */
	public static int transExecute(Connection conn, String sql,Object... args) throws SQLException {
		int count = -1;
		PreparedStatement ps = null;
		try {
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
	 * 获得位于查询结果第一行第一列单元格内的数据(单个值)
	 * @param sql 查询命令
	 * @param args 格式化参数
	 * @return 查询到的结果 如为null已转换为""
	 * @throws SQLException 数据库操作异常
	 */
	public static String findField(String sql, Object... args) throws SQLException{
		String result = "";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getString(1);
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
		result = result == null ? "" : result;
		return result;
	}

	/**
	 * 获得一条数据 示例：findOne("select * from UserInfo where userId=1",new UserInfo());
	 * @param sql 查询行命令
	 * @param obj 可直接传递new Object()和Object.getClass().newInstance()
	 * @param args 格式化参数
	 * @return 已经装填了值的参数对应对象
	 * @throws SQLException 数据库操作异常
	 * @throws SecurityException 反射安全限制异常
	 * @throws NoSuchMethodException 为指定参数异常
	 * @throws InvocationTargetException  错误目标异常
	 * @throws IllegalArgumentException 参数不合法异常
	 * @throws IllegalAccessException 反射异常
	 */
	public static Object findOne(String sql, Object obj, Object... args) throws SQLException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			rs = ps.executeQuery();
			if (rs.next()) {
				Field[] fd = obj.getClass().getDeclaredFields();
				for (Field f : fd) {
					Method m = obj.getClass().getMethod("set" + getFirstUpperName(f.getName()),f.getType());
					m.invoke(obj, rs.getObject(f.getName()));
				}
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
		return obj;
	}
	
	public static String getFirstUpperName(String name) {
		StringBuffer buff = new StringBuffer();
		buff.append(name.substring(0, 1).toUpperCase());
		buff.append(name.substring(1));
		return buff.toString();
	}

	/**
	 * 查询返回多条数据 示例：findMany("select * from UserInfo",new UserInfo());
	 * @param sql 查询命令
	 * @param obj 可直接传递new Object()和Object.getClass().newInstance()
	 * @param args 格式化参数
	 * @return 已经装填了值的参数对应集合
	 * @throws SQLException 数据库操作异常
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 参数不合法异常
	 * @throws IllegalAccessException 反射异常
	 * @throws NoSuchMethodException 未指定参数异常
	 * @throws SecurityException 反射访问限制异常
	 * @throws InvocationTargetException 错误目标异常
	 */
	public static List<?> findMany(String sql, Object obj, Object... args) throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		List<Object> objList = new ArrayList<Object>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				Object o = null;
				o = obj.getClass().newInstance();
				Field[] fd = obj.getClass().getDeclaredFields();
				for (Field f : fd) {
					Method m = obj.getClass().getMethod("set" + getFirstUpperName(f.getName()),f.getType());
					m.invoke(o, rs.getObject(f.getName()));
				}
				objList.add(o);
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
		return objList;
	}

	/**
	 * 分页查询数据 示例：findMany("select * from UserInfo",new UserInfo(),8,1);
	 * @param sql 已支持 MySQL、Oracle、SQLServer
	 * @param obj 可直接传递new Object()和Object.getClass().newInstance()
	 * @param pageSize 每页显示条数
	 * @param pageIndex 当前页码
	 * @param args 格式化参数
	 * @return 返回两个长度的集合 1为集合结果，2为总记录数量
	 * @throws SQLException 数据库操作异常
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 参数不合法异常
	 * @throws IllegalAccessException 反射异常
	 * @throws NoSuchMethodException 未指定参数异常
	 * @throws SecurityException 反射访问限制异常
	 * @throws InvocationTargetException 错误目标异常
	 */
	public static Object[] findMany(String sql, Object obj, int pageSize, int pageIndex, Object... args) throws SQLException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException{
		String countSql = "select count(1) " + sql.substring(sql.indexOf("from"));//record count
		List<Object> objList = new ArrayList<Object>();
		if (database.equals("mysql")) {
			sql += " limit " + pageSize * (pageIndex - 1) + "," + pageSize;
		} else if (database.equals("oracle")) {
			if (sql.indexOf("where") < 0) {
				sql += " where rownum between " + (pageSize * (pageIndex - 1)) + " and " + pageSize * pageIndex;
			} else {
				sql = sql.replaceFirst("where", "where rownum between " + (pageSize * (pageIndex - 1)) + " and " + pageSize * pageIndex + " and");
			}
		} else if (database.equals("sqlserver")) {
			String unique = obj.getClass().getFields()[0].getName();
			String factor = sql.indexOf("where") > 0 ? " and " : " where ";
			sql = "select top " + pageSize + sql.substring(6) + factor + unique + " not in" + "(select top " + (pageSize * (pageIndex - 1)) + " " + unique + " " + sql.substring(sql.indexOf("from")) + ")";
		}
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				Object o = null;
				o = obj.getClass().newInstance();
				Field[] fd = obj.getClass().getDeclaredFields();
				for (Field f : fd) {
					Method m = obj.getClass().getMethod("set" + getFirstUpperName(f.getName()),f.getType());
					m.invoke(o, rs.getObject(f.getName()));
				}
				objList.add(o);
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
		Object[] objs = {objList,DBUtil.findField(countSql, args)};
		return objs;
	}

	/**
	 * 获得位于查询结果第一列的数据
	 * @param sql 查询列命令
	 * @param args 格式化参数
	 * @return 查询到的单列结果 String数组
	 * @throws SQLException 数据库操作异常
	 */
	public static String[] findColumn(String sql, Object... args) throws SQLException{
		String[] arr;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			rs = ps.executeQuery();
			rs.last();
			arr = new String[rs.getRow()];
			rs.beforeFirst();
			int i = 0;
			while (rs.next()) {
				arr[i++] = rs.getString(1);
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
		return arr;
	}

	/**
	 * 查询返回字符二维数组数据 示例：findMany("select * from UserInfo");
	 * @param sql 查询命令
	 * @param args 格式化参数
	 * @return 查询结果的String二维数组形式
	 * @throws SQLException 数据库操作异常
	 */
	public static String[][] findArray(String sql, Object... args) throws SQLException {
		String[][] result = null;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
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
			while (rs.next()) {
				for (int j = 0; j < col; j++) {
					result[i][j] = rs.getString(j + 1);
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
	 * 分页查询返回字符二维数组数据 示例：findMany("select * from UserInfo");
	 * @param sql 查询命令
	 * @param pageSize 分页大小
	 * @param pageIndex 页码
	 * @param args 格式化参数
	 * @return 返回两个长度的数组1为String二维数组结果，2为总记录数量
	 * @throws SQLException 数据库操作异常
	 */
	public static Object[] findArray(String sql, int pageSize, int pageIndex, Object... args) throws SQLException{
		String[][] result = null;
		String countSql = "select count(1) " + sql.substring(sql.indexOf("from"));//record count
		if (database.equals("mysql")) {
			sql += " limit " + pageSize * (pageIndex - 1) + "," + pageSize;
		} else if (database.equals("oracle")) {
			if (sql.indexOf("where") < 0) {
				sql += " where rownum between " + (pageSize * (pageIndex - 1)) + " and " + pageSize * pageIndex;
			} else {
				sql = sql.replaceFirst("where", "where rownum between " + (pageSize * (pageIndex - 1)) + " and " + pageSize * pageIndex + " and");
			}
		} else if (database.equals("sqlserver")) {
			String unique = "id";
			String factor = sql.indexOf("where") > 0 ? " and " : " where ";
			sql = "select top " + pageSize + sql.substring(6) + factor + unique + " not in" + "(select top " + (pageSize * (pageIndex - 1)) + " " + unique + " " + sql.substring(sql.indexOf("from")) + ")";
		}
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
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
			while (rs.next()) {
				for (int j = 0; j < col; j++) {
					result[i][j] = rs.getString(j + 1);
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
		Object[] objs = { result, DBUtil.findField(countSql, args) };
		return objs;
	}

	/**
	 * 返回离线SET对象
	 * @param sql 查询命令
	 * @param args 格式化参数
	 * @return 离线SET对象(ResultSet离线版)
	 * @throws SQLException 数据库操作异常
	 */
	public static CachedRowSet findSet(String sql, Object... args) throws SQLException{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		CachedRowSet crs = new CachedRowSetImpl();
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			rs = ps.executeQuery();
			crs.populate(rs);
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
		return crs;
	}

	/***
	 * 返回多个SET对象结果 (need connection string add 'allowMultiQueries=true')
	 * @param sql 查询命令
	 * @param args 格式化参数
	 * @return 离线Set对象list(类似.NET的DateSet)
	 * @throws SQLException 数据库操作异常
	 */
	public static List<CachedRowSet> findSetList(String sql, Object... args) throws SQLException {
		List<CachedRowSet> rsList = new ArrayList<CachedRowSet>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = -1;
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; i++) {
				ps.setObject((i + 1), args[i]);
			}
			ps.execute();
			do {
				rs = ps.getResultSet();
				if(null != rs) {
					CachedRowSet crs = new CachedRowSetImpl();
					crs.populate(rs);
					rs.close();
					rsList.add(crs);
					ps.getMoreResults();
					continue;
				}
				count = ps.getUpdateCount();
				if(-1 == count) {
					ps.getMoreResults();
					continue;
				}
			} while (!(null == rs && -1 == count));
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
		return rsList;
	}

	/**
	 * 将对象信息直接存入数据库（类似 update/delete/select 尚未完成）
	 * @param obj 需要存数数据库的实体对象
	 * @return 成功插入数据条数
	 * @throws IllegalArgumentException 参数不正确异常
	 * @throws IllegalAccessException 反射异常
	 * @throws SQLException 数据库操作异常
	 */
	public static int insert(Object obj) throws IllegalArgumentException, IllegalAccessException, SQLException{
		int result = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = getConnection();
			Field[] fd = obj.getClass().getFields();
			StringBuffer sb = new StringBuffer("insert into ");
			sb.append(obj.getClass().getSimpleName());
			sb.append("(");
			for (Field f : fd) {
				if (null != f.get(obj)) {
					sb.append(f.getName());
					sb.append(",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(") ");
			sb.append("values(");
			for (Field f : fd) {
				if (null != f.get(obj)) {
					if (f.getType().getSimpleName().equals("String") || f.getType().getSimpleName().equals("Date") || f.getType().getSimpleName().equals("char")) {
						sb.append("'");
					}
					sb.append(f.get(obj));
					if (f.getType().getSimpleName().equals("String") || f.getType().getSimpleName().equals("Date") || f.getType().getSimpleName().equals("char")) {
						sb.append("'");
					}
					sb.append(",");
				}
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(") ");
			ps = conn.prepareStatement(sb.toString());
			result = ps.executeUpdate();
		} finally {
			if (null != ps) {
				ps.close();
			}
			if (null != conn) {
				conn.close();
			}
		}
		return result;
	}
}