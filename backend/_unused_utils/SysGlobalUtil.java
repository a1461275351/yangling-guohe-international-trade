package cn.nodesoft.utils;

import cn.nodesoft.biz.sysConfigs.dao.SysGlobalDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 全局变量
 * 查询报表时调用 为true时才可以查询数据，为false时提示稍后查询
 *
 *
 * szq 2022-07-26
 */
@Component
public class SysGlobalUtil {
    @Autowired
    SysGlobalDao sysGlobalDao;

    public boolean getGlobalValue(){
        return Boolean.parseBoolean(sysGlobalDao.getValue());
    }
}
