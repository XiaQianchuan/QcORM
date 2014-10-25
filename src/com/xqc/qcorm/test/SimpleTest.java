package com.xqc.qcorm.test;

import com.ecward.pay.entity.Admin;
import com.ecward.pay.entity.Role;
import com.xqc.qcorm.entity.FieldsEntry;
import com.xqc.qcorm.util.PojoUtil;

public class SimpleTest {
	public static void main(String[] args) {
//		Orders m1 = new Orders();
//		m1.setIsDcc(false);
//		Orders m2 = (Orders)m1.clone();
//		m2.setIsDcc(true);
//		m2.setAmountCNY(new BigDecimal("0.00"));
		Role r1 = new Role();
		r1.setRoleId(1);
		r1.setRoleName("角色2");
		
		Role r2 = new Role();
		r2.setRoleId(2);
		r2.setRoleName("角色2");
		
		Admin m1 = new Admin();
		m1.setAdminName("xqc");
		m1.setRole(r1);
		
		Admin m2 = (Admin)m1.clone();
//		r1.setRoleId(2);
		m2.setAdminName("cqx");
		m2.setRole(r2);
		
//		System.out.println(m1.getRole().getRoleId());
		
		FieldsEntry map = PojoUtil.getDifferentFields(m1, m2);
//		Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
//		while (iterator.hasNext()) {
//			Map.Entry<String, Object> entry = iterator.next();
//			System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
//		}
//		for (Map.Entry<String, Object> entry : map.entrySet()) {
//			System.out.println(entry.getKey() + "=>" + entry.getValue());
//		}
		String sql = map.genSqlSection();
		System.out.println(sql);
	}
}
