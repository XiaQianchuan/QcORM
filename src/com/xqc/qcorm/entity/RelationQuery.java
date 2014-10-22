package com.xqc.qcorm.entity;

import java.util.HashMap;
import java.util.Map;

public class RelationQuery {
	
	public RelationQuery(){}
	
	public RelationQuery(String mainTableName) {
		this.mainTableName = mainTableName;
		this.relationSql = genRelationSql();
		slaveTableNames = new HashMap<String, String>();
	}
	
	/**
	 * 关联SQL语句
	 */
	private StringBuffer relationSql;
	
	/**
	 * 关联查询主表名称
	 */
	private String mainTableName;
	
	/**
	 * 管理查询从表名称
	 */
	private Map<String, String> slaveTableNames;
	
	public StringBuffer genRelationSql() {
		return new StringBuffer("SELECT * FROM `" + this.getMainTableName() + "` t1 ");
	}
	
	public void setLeftJoin(String mainTable, String slaveTable, String joinLeft, String joinRight) {
		if(!slaveTableNames.containsKey(slaveTable)) {
			String mainAlias = slaveTableNames.containsKey(mainTable) ? slaveTableNames.get(mainTable) : "t1";
			String slaveAlias = "t"+(slaveTableNames.size()+2);
			slaveTableNames.put(slaveTable, slaveAlias);
			this.relationSql.append("LEFT JOIN `"+slaveTable+"` "+slaveAlias+" ON "+mainAlias+".`"+ joinLeft +"`="+slaveAlias+".`"+joinRight+"` ");
		}
	}

	public StringBuffer getRelationSql() {
		return relationSql;
	}

	public void setRelationSql(StringBuffer relationSql) {
		this.relationSql = relationSql;
	}

	public String getMainTableName() {
		return mainTableName;
	}

	public void setMainTableName(String mainTableName) {
		this.mainTableName = mainTableName;
	}

	public Map<String, String> getSlaveTableNames() {
		return slaveTableNames;
	}

	public void setSlaveTableNames(Map<String, String> slaveTableNames) {
		this.slaveTableNames = slaveTableNames;
	}
}
