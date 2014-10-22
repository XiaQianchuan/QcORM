package com.xqc.qcorm.test;

import java.util.LinkedHashMap;
import java.util.Map;

import com.xqc.qcorm.core.ObjectCachePool;

public class CacheObjectTest {

	public static void main(String[] args) {
		test3();
	}

	public static void test1() {
		Map<String, String> map = new LinkedHashMap<String, String>(1);
		map.put("ka", "va");
		map.put("kb", "vb");
		map.put("kc", "vc");
		map.put("kd", "vd");
		System.out.println(map);
	}

	public static void test2() {
		ObjectCachePool<String, String> pool = new ObjectCachePool<String, String>(3);
		pool.put("ka", "va");
		pool.put("kb", "vb");
		pool.put("kc", "vc");
		pool.put("kd", "vd");
		System.out.println(pool.listValue());
	}

	public static void test3() {
		ObjectCachePool<String, String> pool = new ObjectCachePool<String, String>(4, ObjectCachePool.LRU);
		pool.put("ka", "va");
		pool.put("kb", "vb");
		pool.put("kc", "vc");
		pool.get("ka");
		pool.put("kd", "vd");
		System.out.println(pool.listValue());
	}
}
