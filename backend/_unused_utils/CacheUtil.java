package cn.nodesoft.utils;

import cn.nodesoft.common.constant.CacheKeyConstant;
import com.spdb.speed4j.httpassist.cache.Cache;
import com.spdb.speed4j.httpassist.cache.CacheFactory;
import com.spdb.speed4j.httpassist.cache.caffeine.CaffeineCache;

/**
 * @ProjectName: spdb-bm
 * @PackageName: cn.nodesoft.utils
 * @ClassName: CacheUtil
 * @Description: 缓存操作
 * @author: chenkang
 * @date: 2022-09-26 10:16
 * @Copyright: 西安捷点科技开发有限责任公司 Copyright (c) 2022
 */
public class CacheUtil {

    public static final String cache_caffeine = "caffeine";

    /**
     * 根据缓存组名称获取缓存组对象
     * @return
     */
    public static Cache getCache(){
        if (CacheFactory.cacheNames().contains(cache_caffeine)) {
           return CacheFactory.use(cache_caffeine);
        } else {
            Cache cache = new CaffeineCache();
            CacheFactory.addCache(cache_caffeine,cache);
            return cache;
        }
    }


    /**
     * 根据cacheName获取到缓存组对象后，将key值所属的缓存对象立即失效
     * @param key
     */
    public static void clearCache(String key){
        Cache cache = getCache();
        if (null != cache) {
            cache.evict(key);
        }
    }


    /**
     * 根据工号清除所有缓存信息
     * @param userno
     */
    public static void cleanCacheByUserno(String userno){
        clearCache(CacheKeyConstant.USER_EXTEND+userno);
        System.out.println("根据工号：" + userno + "，删除用户UserExtend对象完成");
        clearCache(CacheKeyConstant.DEPTIDS_BY_USERNAME_FOR_AUTH+userno);
        System.out.println("根据工号：" + userno + "，删除用户授权机构权限完成");
    }
}
