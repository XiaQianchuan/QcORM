package com.xqc.qcorm.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 对象缓存池
 * @author XQC
 * @date 2014.2.20
 * @param <K>
 * @param <V>
 */
public class ObjectCachePool<K, V> {

	//Least Recently Used 最近最少使用算法排序
	public static final boolean LRU = true;

	//Cache Size 缓存对象数量
	private static final int CACHE_DEFAUTLT_SIZE = 100;

	private Map<K, V> cacheObject;

	public ObjectCachePool() {
		this(CACHE_DEFAUTLT_SIZE);
	}

	public ObjectCachePool(final int size) {
		this(size, LRU);
	}

	public ObjectCachePool(final int size, final boolean accessOrder) {
		if(accessOrder) {
			cacheObject = new LinkedHashMap<K, V>(size, 0.75f, true) {
				private static final long serialVersionUID = 1L;
				@Override
				protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
					return size() > size;
				}
			};
		} else {
			//First In First Out 先进先出
			cacheObject = new LinkedHashMap<K, V>(size) {
				private static final long serialVersionUID = 1L;
				@Override
				protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
					return size() > size;
				}
			};
		}
	}

	public void put(K key, V value) {
		cacheObject.put(key, value);
	}

	public V get(K key) {
		return cacheObject.get(key);
	}

	public void remove(K key) {
		cacheObject.remove(key);
	}

	public void clear() {
		cacheObject.clear();
	}

	public Collection<V> listValue() {
		return cacheObject.values();
	}
}
