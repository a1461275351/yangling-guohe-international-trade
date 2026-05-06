package cn.nodesoft.utils.bean;

import com.fasterxml.jackson.databind.JavaType;
import com.spdb.speed4j.httpassist.cache.Cache;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @ProjectName: spdb-bm
 * @PackageName: cn.nodesoft.utils.bean
 * @ClassName: CustomCache
 * @Description: 自定义缓存类型
 * @author: chenkang
 * @date: 2022-09-28 12:16
 * @Copyright: 西安捷点科技开发有限责任公司 Copyright (c) 2022
 */
public class CustomCache implements Cache {
    @Override
    public void init() {

    }

    @Override
    public Map<Object, Object> asMap() {
        return null;
    }

    @Override
    public Set<Object> keys() {
        return null;
    }

    @Override
    public Boolean expireAt(Object o, Date date) {
        return null;
    }

    @Override
    public Boolean expire(Object o, long l) {
        return null;
    }

    @Override
    public void set(Object o, Object o1, long l, TimeUnit timeUnit) {

    }

    @Override
    public Object get(Object o, JavaType javaType) {
        return null;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public ValueWrapper get(Object key) {
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {

    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return null;
    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public void clear() {

    }
}
