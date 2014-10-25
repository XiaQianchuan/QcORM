package com.xqc.qcorm.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.xqc.qcorm.entity.FieldsEntry;

/**
 * JavaBean工具类
 * @author XQC
 * @date 2014.11.11
 */
public class PojoUtil {
	
	private static ResourceBundle qcormConf = ResourceBundle.getBundle("qcorm");
	
	/**
	 * 对比两个对象
	 * @param obj1
	 * @param obj2
	 * @return Different field's name
	 */
	public static List<String> contrastObject(Object obj1, Object obj2) {
		List<String> diffFieldList = new ArrayList<String>();
		Class<? extends Object> cls1 = obj1.getClass();
		Class<? extends Object> cls2 = obj2.getClass();
		// 判断是否为同一个实体类
		if(cls1.equals(cls2)) {
			Field[] fields = cls1.getDeclaredFields();
			
				for (Field field : fields) {
					try {
						// 通过内省获取属性信息
						PropertyDescriptor pd = new PropertyDescriptor(field.getName(), cls1);
						Method getMethod = pd.getReadMethod(); //获取对象属性的get方法
						Object o1 = getMethod.invoke(obj1);
						Object o2 = getMethod.invoke(obj2);
						String displayName = pd.getDisplayName(); //获取属性名称
						if(o1 != o2) {
							// 添加不同的属性到列表
							if(o1 == null || o2 == null)
								diffFieldList.add(displayName);
							else if(!o1.toString().equals(o2.toString()))
								diffFieldList.add(displayName);
						}
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (IntrospectionException e) {
						continue;
					}
				}
		}
		return diffFieldList;
	}
	
	/**
	 * 对比两个对象
	 * @param obj1
	 * @param obj2
	 * @return Different field's name and the value of obj2
	 */
	public static FieldsEntry getDifferentFields(Object obj1, Object obj2) {
		FieldsEntry diffFieldsMap = new FieldsEntry();
		Class<? extends Object> cls1 = obj1.getClass();
		Class<? extends Object> cls2 = obj2.getClass();
		// 判断是否为同一个实体类
		if(cls1.equals(cls2)) {
			Field[] fields = cls1.getDeclaredFields();
			for (Field field : fields) {
				try {
					// 通过内省获取属性信息
					PropertyDescriptor pd = new PropertyDescriptor(field.getName(), cls1);
					Method getMethod = pd.getReadMethod(); //获取对象属性的get方法
					Object o1 = getMethod.invoke(obj1);
					Object o2 = getMethod.invoke(obj2);
					String displayName = pd.getDisplayName(); //获取属性名称
					if(o1 != o2) {
						String qcormKey = qcormConf.getString(cls1.getName()+"."+displayName);
						if(qcormConf.containsKey(qcormKey)) {
//							String filedName = qcormConf.getString(cls1.getName()+"."+displayName+"."+"refer"); //roldId
//							PropertyDescriptor pd2 = new PropertyDescriptor(filedName, o2.getClass());
//							Object val = pd2.getReadMethod().invoke(o2);
//							if(null != val) {
//								diffFieldsMap.put(filedName, val);
//							}
							FieldsEntry kv = getDifferentFields(o1,o2);
							diffFieldsMap.putAll(kv);
						} else {
							// 添加不同的属性到列表
//							if(isEquals(o1, o2)) {
//								String columnName = qcormConf.getString(qcormKey);
//								diffFieldsMap.put(columnName, o2);
//							}
							
							if(o1 == null || o2 == null)
								diffFieldsMap.put(displayName, o2);
							else if(!o1.toString().equals(o2.toString()))
								diffFieldsMap.put(displayName, o2);
						}						
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (IntrospectionException e) {
					continue;
				}
			}
		}
		return diffFieldsMap;
	}
	
	private static boolean isEquals(Object obj1, Object obj2) {
		if (obj1 == null)
			return obj2 == null;
		else
			return obj1.equals(obj2);
	}
}
