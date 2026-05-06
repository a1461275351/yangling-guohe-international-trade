package cn.nodesoft.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.regex.Pattern;
/**
 * @ProjectName: spdb-bm
 * @PackageName: cn.nodesoft.utils
 * @ClassName: EmailUtil
 * @Description: 邮箱工具类
 * @author: chenkang
 * @date: 2022-11-17 09:45
 * @Copyright: 西安捷点科技开发有限责任公司 Copyright (c) 2022
 */
@Slf4j
public class EmailUtil {

    private static String ip = "10.19.5.33";
    private static String port = "8899";
    private static String sendAddress = "XANITSERVICE@spdb.com.cn";
    private static String mailSenderUsername = "XANITSERVICE";
    private static String mailSenderPassword = "kjb@2019!";

//    public static String emailMatcher="[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+";

    public static String emailMatcher="^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z0-9]{2,6}$";
    /**
     * 验证邮箱格式
     * @param str
     * @return
     */
    public static boolean check(String str){
        return Pattern.matches(emailMatcher,str);
    }

}
