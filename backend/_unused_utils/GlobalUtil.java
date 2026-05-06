package cn.nodesoft.utils;

import com.spdb.speed4j.util.common.SpringContextUtil;


/**
 * 公用的配置文件处理类
 *
 */

public class GlobalUtil extends SpringContextUtil {

	/**
	 * 获取yml配置项的值
	 * @param key 配置项key
	 */
	public static String getEnvironmentProperty(String key){
		return getApplicationContext().getEnvironment().getProperty(key);
	}
}
