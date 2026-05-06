package cn.nodesoft.utils;

import com.spdb.speed4j.util.common.SpringContextUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * EhCache缓存操作工具
 * @author 小懒虫
 * @date 2018/11/7
 */
public class EhCacheUtil {

    /**
     * 获取EhCacheManager管理对象
     */
    public static CacheManager getCacheManager(){
        return (CacheManager)SpringContextUtil.getBean(CacheManager.class);
    }

    /**
     * 获取EhCache缓存对象
     */
    public static Cache getCache(String name){
        return getCacheManager().getCache(name);
    }


    /**
     * 获取资源缓存集合
     */
    public static Cache getFunctionInfosCache(){
        return getCacheManager().getCache("functionInfos");
    }

}
