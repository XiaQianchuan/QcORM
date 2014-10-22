package com.xqc.qcorm.test;

import com.ecward.pay.entity.Merchant;
import com.xqc.qcorm.sql.Select;

public class SQLTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Select sql = new Select(Merchant.class);
		
		System.out.println(sql.getClass().getCanonicalName());
		
		Merchant m  = new Merchant();
		
		sql.setField(m.getBalance(),m.getAddress());
		
		System.out.println(sql.genSql());
		
	}
}
