package cn.nodesoft.utils;

import cn.nodesoft.biz.fileManage.bean.BizFile;
import cn.nodesoft.biz.fileManage.dao.BizFileDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.processor.RowListProcessor;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParser;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParserSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * FTP文件上传、下载
 */
@Slf4j
@Component
public class FtpUtil {

      /*110 重新启动标记应答。
        120 服务在多久时间内ready。
        125 数据链路埠开启，准备传送。
        150 文件状态正常，开启数据连接端口。
        200 命令执行成功。
        202 命令执行失败。
        211 系统状态或是系统求助响应。
        212 目录的状态。
        213 文件的状态。
        214 求助的讯息。
        215 名称系统类型。
        220 新的联机服务ready。
        221 服务的控制连接埠关闭，可以注销。
        225 数据连结开启，但无传输动作。
        226 关闭数据连接端口，请求的文件操作成功。
        227 进入passive mode。
        230 使用者登入。
        250 请求的文件操作完成。
        257 显示目前的路径名称。
        331 用户名称正确，需要密码。
        332 登入时需要账号信息。
        350 请求的操作需要进一部的命令。
        421 无法提供服务，关闭控制连结。
        425 无法开启数据链路。
        426 关闭联机，终止传输。

        450 请求的操作未执行。
        451 命令终止：有本地的错误。
        452 未执行命令：磁盘空间不足。
        500 格式错误，无法识别命令。
        501 参数语法错误。
        502 命令执行失败。
        503 命令顺序错误。
        504 命令所接的参数不正确。
        530 未登入。
        532 储存文件需要账户登入。
        550 未执行请求的操作。
        551 请求的命令终止，类*/

    //ftp连接参数
    private static final String IP = GlobalUtil.getEnvironmentProperty("ftp.ip");

    private static final int PORT = Integer.parseInt(GlobalUtil.getEnvironmentProperty("ftp.port"));

    private static final String USERNAME = GlobalUtil.getEnvironmentProperty("ftp.user");

    private static final String PASSWORD = GlobalUtil.getEnvironmentProperty("ftp.password");

@Autowired
private static BizFileDao bizFileDao;
    private static FTPClient ftpClient = null;
    /**
     * 初始化ftp服务器
     */
    public static void initFtpClient(){
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding("utf-8");
        try {
            log.info("准备连接FTP服务器:【"+IP+":"+PORT +"】...");
            //连接ftp服务器
            ftpClient.connect(IP, PORT);
            log.info("FTP服务器:【"+IP+":"+PORT +"】连接成功，准备登陆FTP服务器...");
            //登录ftp服务器
            ftpClient.login(USERNAME, PASSWORD);
            //是否成功登录服务器
            int replyCode = ftpClient.getReplyCode();
            if(!FTPReply.isPositiveCompletion(replyCode)){
                log.error("FTP服务器:【"+IP+":"+PORT +"】登陆失败！");
                ftpClient.disconnect();
            }else{
                log.info("FTP服务器:【"+IP+":"+PORT +"】登陆成功！");
            }
        }catch (MalformedURLException e) {
            log.error("出现错误：" + e.getMessage());
        }catch (IOException e) {
            log.error("出现错误：" + e.getMessage());
        }
    }

    /**
     * 上传文件
     * @param ftpDirName 文件夹路径
     * @param uploadName 文件名
     * @return
     */
    public static boolean uploadFile(String ftpDirName,String uploadName,InputStream input) {
        if (StringUtils.isEmpty(ftpDirName) || StringUtils.isEmpty(uploadName) || null == input){
            log.info("FTP服务器:【"+IP+":"+PORT +"】执行上传操作，未获取到可上传文件，终止执行上传。");
            return false;
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行上传操作开始...");
        long start = System.currentTimeMillis();
        ftpDirName = ftpDirName.replace(uploadName,"");
        //连接ftp服务器
        initFtpClient();
        boolean flag = false;
        try {
            //判断ftp是否存在文件夹
            createDirecroty(ftpDirName.replaceAll(Matcher.quoteReplacement(File.separator),"/"));
            try{
                ftpClient.setDataTimeout(120000);

                /*【主动模式】 工作的原理： FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
                登录成功后要list列表或者读取数据时，客户端随机开放一个端口（1024以上），发送 PORT命令到FTP服务器，
                告诉服务器客户端采用主动模式并开放端口；FTP服务器收到PORT主动模式命令和端口号后，
                通过服务器的20端口和客户端开放的端口连接，发送数据*/
//                 ftpClient.enterLocalActiveMode();


                /*【被动模式】工作原理：FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
                登录成功后要list列表或者读取数据时，发送PASV命令到FTP服务器， 服务器在本地随机开放一个端口（1024以上），
                然后把开放的端口告诉客户端， 客户端再连接到服务器开放的端口进行数据传输*/
                ftpClient.enterLocalPassiveMode();

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                if(ftpClient.storeFile(uploadName, input)) {
                    log.info("FTP服务器:【"+IP+":"+PORT +"】上传文件成功!，状态码：" + ftpClient.getReplyString());
                    flag = true;
                }else {
                    log.info("FTP服务器:【"+IP+":"+PORT +"】上传文件失败!，错误码：" + ftpClient.getReplyString());
                }
                input.close();
                log.info("FTP服务器:【"+IP+":"+PORT +"】上传文件流完成!");
                ftpClient.logout();
                log.info("FTP服务器:【"+IP+":"+PORT +"】退出成功！");
            } catch (IOException e) {
                flag = false;
                log.error("出现错误：" + e.getMessage());
            } finally {
                input.close();
            }
        } catch (Exception e) {
            flag = false;
            log.error("文件上传失败,失败原因："+e.getMessage());
        }finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                    log.info("FTP服务器:【"+IP+":"+PORT +"】断开连接成功！");
                }catch(IOException e){
                    flag = false;
                    log.error("出现错误：" + e.getMessage());
                }
            }
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行上传操作结束，耗时:" + (System.currentTimeMillis() - start) + "毫秒");
        return flag;
    }


    //创建多层目录文件，如果有ftp服务器已存在该文件，则不创建，如果无，则创建
   public static void createDirecroty(String dir) throws IOException {
       if (isDirExist(ftpClient, dir)) {
           ftpClient.changeWorkingDirectory(dir);
       } else {
           String[] flolder = dir.split("/");//示例  DestFolder： 2021/03/03
           for (int i = 0; i < flolder.length; i++) {
               if (!ftpClient.changeWorkingDirectory(flolder[i])) {//切换工作目录  返回true切换成功说明有这个文件夹， 反之
                   ftpClient.makeDirectory(flolder[i]);//  没有这个文件夹的话 创建这个文件夹
                   ftpClient.changeWorkingDirectory(flolder[i]); //创建好后再切换工作目录
               }
           }
       }
    }


    //改变目录路径
    public static boolean changeWorkingDirectory(String directory) {
        boolean flag = true;
        try {
            flag = ftpClient.changeWorkingDirectory(directory.replaceAll(Matcher.quoteReplacement(File.separator),"/"));
            if (flag) {
                log.info("进入文件夹" + directory + " 成功！");
            } else {
                log.error("进入文件夹" + directory + " 失败！开始创建文件夹");
            }
        } catch (IOException e) {
            log.error("出现错误：" + e.getMessage());
        }
        return flag;
    }


    //判断ftp服务器文件是否存在
    public static boolean existFile(String path) throws IOException {
        boolean flag = false;
        // 由于apache不支持中文语言环境，通过定制类解析中文日期类型
        ftpClient.configure(new FTPClientConfig("com.zznode.tnms.ra.c11n.nj.resource.ftp.UnixFTPEntryParser"));
        FTPFile[] ftpFileArr = ftpClient.listFiles(path);
        if (ftpFileArr.length > 0) {
            flag = true;
        }
        return flag;
    }

    private static boolean isDirExist(FTPClient ftpClient, String dir) throws IOException {
        boolean b = ftpClient.changeWorkingDirectory(dir);
        return b;
    }


    /**
     * 创建目录
     */
    public static boolean makeDirectory(String dir) {
        boolean flag = true;
        try {
            flag = ftpClient.makeDirectory(dir);
            if (flag) {
                log.info("创建文件夹" + dir + " 成功！");

            } else {
                log.error("创建文件夹" + dir + " 失败！");
            }
        } catch (Exception e) {
            log.error("出现错误：" + e.getMessage());
        }
        return flag;
    }


    /**
     * 下载上传的文件
     * @param filename
     * @return
     */
    public static void downloadFile(String filename,String filePath,HttpServletResponse response){
        if (StringUtils.isEmpty(filename) || StringUtils.isEmpty(filePath)){
            log.info("FTP服务器:【"+IP+":"+PORT +"】执行下载操作，未获取到可下载文件，终止执行下载。");
            errrorPageMsg(response,"您访问的文件不存在");
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行下载操作开始...");
        long start = System.currentTimeMillis();
        filePath = filePath.replaceAll(Matcher.quoteReplacement(File.separator),"/");
        filePath = filePath.replaceAll("\\\\","/");
        filePath = filePath.replace(filename,"");
        String remoteFileName = "";
        if (filePath.contains(".")) {
            remoteFileName = filePath.substring(filePath.lastIndexOf("/")+1);
            filePath = filePath.substring(0,filePath.lastIndexOf("/"));
        } else {
            remoteFileName = filename;
        }
        InputStream resultStream = null;
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;
        try {
            initFtpClient();
            //切换FTP目录
            ftpClient.changeWorkingDirectory(filePath);
            log.info("目录切换成功,当前目录为：" + ftpClient.printWorkingDirectory());

            /*【主动模式】 工作的原理： FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
                登录成功后要list列表或者读取数据时，客户端随机开放一个端口（1024以上），发送 PORT命令到FTP服务器，
                告诉服务器客户端采用主动模式并开放端口；FTP服务器收到PORT主动模式命令和端口号后，
                通过服务器的20端口和客户端开放的端口连接，发送数据*/
            // ftpClient.enterLocalActiveMode();


            /*【被动模式】工作原理：FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
            登录成功后要list列表或者读取数据时，发送PASV命令到FTP服务器， 服务器在本地随机开放一个端口（1024以上），
            然后把开放的端口告诉客户端， 客户端再连接到服务器开放的端口进行数据传输*/
            ftpClient.enterLocalPassiveMode();
            // 由于apache不支持中文语言环境，通过定制类解析中文日期类型
            ftpClient.configure(new FTPClientConfig("com.zznode.tnms.ra.c11n.nj.resource.ftp.UnixFTPEntryParser"));
            FTPFile[] ftpFiles = ftpClient.listFiles();
            if(ftpFiles.length == 0) {
                log.info("FTP服务器:【"+IP+":"+PORT +"】地址：{"+filePath+"}下未获取到文件！");
            } else {
                log.info("FTP服务器:【"+IP+":"+PORT +"】目录"+ftpClient.printWorkingDirectory()+"下文件数"+ftpFiles.length);
//                for(FTPFile fs:ftpFiles){
//                    log.info("FTP服务器:【"+IP+":"+PORT +"】目录"+ftpClient.printWorkingDirectory()+"下文件名"+fs.getName());
//                }
                // 设置输出流为二进制以处理在Unix环境上文件内容乱码
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // 根据指定名称获取指定文件
                inputStream = ftpClient.retrieveFileStream(remoteFileName);
                if (null == inputStream) {
                    log.info("FTP服务器:【"+IP+":"+PORT +"】地址：{"+filePath+"}下未获取到指定文件：{"+remoteFileName+"}！");
                } else {
                    // 复制流对象以完成ftp同步的文件流close操作
                    baos = cloneInputStream(inputStream);
                    resultStream = new ByteArrayInputStream(baos.toByteArray());
                    baos.close();
                    inputStream.close(); // 使用完成后必须立即关闭此文件流
                    log.info("FTP服务器:【"+IP+":"+PORT +"】下载文件流完成!");
                    ftpClient.completePendingCommand();
                }
                ftpClient.logout();
                log.info("FTP服务器:【"+IP+":"+PORT +"】退出成功！");
            }
        } catch (Exception e) {
            log.error("下载文件失败,失败原因："+e.getMessage());
        } finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                    log.info("FTP服务器:【"+IP+":"+PORT +"】断开连接成功！");
                }catch(IOException e){
                    log.error("错误原因："+e.getMessage());
                }
            }
            if (null != baos) {
                try {
                    baos.close();
                } catch (IOException e) {
                    log.error("关闭流失败，失败原因："+e.getMessage());
                }
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭流失败，失败原因："+e.getMessage());
                }
            }
        }

        if (resultStream == null) {
            errrorPageMsg(response,"您访问的文件不存在");
            try {
                response.getWriter().close();
            } catch (IOException e) {
                log.error("出现错误：" + e.getMessage());
            }
            return;
        }
        response.reset();
        response.setContentType("application/x-msdownload;charset=utf-8");
        try {
            response.setHeader("Content-Disposition", "attachment;filename="+ new String(filename.getBytes("gbk"), "iso-8859-1"));//下载文件的名称
        } catch (UnsupportedEncodingException e) {
            log.error("出现错误：" + e.getMessage());
            log.error(e.getMessage());
        }
        ServletOutputStream sout = null;
        try {
            sout = response.getOutputStream();
        } catch (IOException e) {
            log.error("出现错误：" + e.getMessage());
        }
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(resultStream);
            bos = new BufferedOutputStream(sout);
            byte[] buff = new byte[2048];
            int bytesRead;
            while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                bos.write(buff, 0, bytesRead);
            }
            bos.flush();
            bos.close();
            bis.close();
            sout.close();
            resultStream.close();
        } catch (final IOException e) {
            log.error("出现错误：" + e.getMessage());
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                log.error("出现错误：" + e.getMessage());
            }
            try {
                if (bos != null){
                    bos.close();
                }
            } catch (IOException e) {
                log.error("出现错误：" + e.getMessage());
            }
            try {
                if (sout != null){
                    sout.close();
                }
            } catch (IOException e) {
                log.error("出现错误：" + e.getMessage());
            }
            try {
                resultStream.close();
            } catch (IOException e) {
                log.error("出现错误：" + e.getMessage());
            }
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行下载操作结束，耗时:" + (System.currentTimeMillis() - start) + "毫秒");
    }


    /**
     * 下载上传的文件
     * @param filename
     * @return
     */
    public static InputStream downloadFile(String filename,String filePath){
        if (StringUtils.isEmpty(filename) || StringUtils.isEmpty(filePath)){
            log.info("FTP服务器:【"+IP+":"+PORT +"】执行下载操作，未获取到可下载文件，终止执行下载。");
            return null;
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行下载操作开始...");
        long start = System.currentTimeMillis();
        filePath = filePath.replaceAll(Matcher.quoteReplacement(File.separator),"/");
        filePath = filePath.replaceAll("\\\\","/");
        filePath = filePath.replace(filename,"");
        String remoteFileName = "";
        if (filePath.contains(".")) {
            remoteFileName = filePath.substring(filePath.lastIndexOf("/")+1);
            filePath = filePath.substring(0,filePath.lastIndexOf("/"));
        } else {
            remoteFileName = filename;
        }

        InputStream resultStream = null;
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;
//        FTPClient ftpClient = getFtpClient();
        try {
            initFtpClient();
            //切换FTP目录
            ftpClient.changeWorkingDirectory(filePath.replaceAll(Matcher.quoteReplacement(File.separator),"/"));
            log.info("目录切换成功,当前目录为：" + ftpClient.printWorkingDirectory());
            /*【主动模式】 工作的原理： FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
                登录成功后要list列表或者读取数据时，客户端随机开放一个端口（1024以上），发送 PORT命令到FTP服务器，
                告诉服务器客户端采用主动模式并开放端口；FTP服务器收到PORT主动模式命令和端口号后，
                通过服务器的20端口和客户端开放的端口连接，发送数据*/
            // ftpClient.enterLocalActiveMode();


            /*【被动模式】工作原理：FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
            登录成功后要list列表或者读取数据时，发送PASV命令到FTP服务器， 服务器在本地随机开放一个端口（1024以上），
            然后把开放的端口告诉客户端， 客户端再连接到服务器开放的端口进行数据传输*/
            ftpClient.enterLocalPassiveMode();
            // 由于apache不支持中文语言环境，通过定制类解析中文日期类型
            ftpClient.configure(new FTPClientConfig("com.zznode.tnms.ra.c11n.nj.resource.ftp.UnixFTPEntryParser"));
            FTPFile[] ftpFiles = ftpClient.listFiles();
            if(ftpFiles.length == 0) {
                log.info("FTP服务器:【"+IP+":"+PORT +"】地址：{"+filePath+"}下未获取到文件！");
            } else {
                log.info("FTP服务器:【"+IP+":"+PORT +"】目录"+ftpClient.printWorkingDirectory()+"下文件数"+ftpFiles.length+1);
//                for(FTPFile fs:ftpFiles){
//                    log.info("FTP服务器:【"+IP+":"+PORT +"】目录"+ftpClient.printWorkingDirectory()+"下文件名"+fs.getName());
//                }
                // 设置输出流为二进制以处理在Unix环境上文件内容乱码
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // 根据指定名称获取指定文件
                inputStream = ftpClient.retrieveFileStream(remoteFileName);
                if (null == inputStream) {
                    log.info("FTP服务器:【"+IP+":"+PORT +"】地址：{"+filePath+"}下未获取到指定文件：{"+filename+"}！");
                } else {
                    // 复制流对象以完成ftp同步的文件流close操作
                    baos = cloneInputStream(inputStream);
                    resultStream = new ByteArrayInputStream(baos.toByteArray());
                    baos.close();
                    inputStream.close(); // 使用完成后必须立即关闭此文件流
                    log.info("FTP服务器:【"+IP+":"+PORT +"】下载文件流完成!");
                    ftpClient.completePendingCommand();
                }
                ftpClient.logout();
                log.info("FTP服务器:【"+IP+":"+PORT +"】退出成功！");
            }
        } catch (Exception e) {
            log.error("下载文件失败,失败原因："+e.getMessage());
        } finally{
//            try{
//                ftpClientPool.returnObject(ftpClient);
//            } catch (Exception e) {
//                log.error("ftp客户端实例还原线程池出现错误：" + e.getMessage());
//            }
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                    log.info("FTP服务器:【"+IP+":"+PORT +"】断开连接成功！");
                }catch(IOException e){
                    log.error("错误原因："+e.getMessage());
                }
            }
            if (null != baos) {
                try {
                    baos.close();
                } catch (IOException e) {
                    log.error("关闭流失败，失败原因："+e.getMessage());
                }
            }
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭流失败，失败原因："+e.getMessage());
                }
            }
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行下载操作结束，耗时:" + (System.currentTimeMillis() - start) + "毫秒");
        return resultStream;
    }


    public static void deleteFile(String remotePath){
        if (StringUtils.isEmpty(remotePath)){
            log.info("FTP服务器:【"+IP+":"+PORT +"】执行删除操作，未获取到可删除文件，终止执行删除。");
            return;
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行删除操作开始...");
        long start = System.currentTimeMillis();
        String filepath = remotePath.replaceAll(Matcher.quoteReplacement(File.separator),"/");
        filepath = filepath.replaceAll("\\\\","/");
        String remoteFileName = filepath.substring(filepath.lastIndexOf("/")+1);
        String remoteFilePath = filepath.substring(0,filepath.lastIndexOf("/"));
        try{
            initFtpClient();
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                log.info("FTP服务器:【"+IP+":"+PORT +"】被异常断开连接！");
            }
            // 设置文件操作目录
            ftpClient.changeWorkingDirectory(remoteFilePath);
            /*【主动模式】 工作的原理： FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
            登录成功后要list列表或者读取数据时，客户端随机开放一个端口（1024以上），发送 PORT命令到FTP服务器，
            告诉服务器客户端采用主动模式并开放端口；FTP服务器收到PORT主动模式命令和端口号后，
            通过服务器的20端口和客户端开放的端口连接，发送数据*/
            // ftpClient.enterLocalActiveMode();


            /*【被动模式】工作原理：FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
            登录成功后要list列表或者读取数据时，发送PASV命令到FTP服务器， 服务器在本地随机开放一个端口（1024以上），
            然后把开放的端口告诉客户端， 客户端再连接到服务器开放的端口进行数据传输*/
            ftpClient.enterLocalPassiveMode();
            // 获取文件操作目录下所有文件名称
            // 由于apache不支持中文语言环境，通过定制类解析中文日期类型
            ftpClient.configure(new FTPClientConfig("com.zznode.tnms.ra.c11n.nj.resource.ftp.UnixFTPEntryParser"));
//                FTPFile[] remoteNames = ftpClient.listFiles();
            // 循环比对文件名称，判断是否含有当前要删除的文件名（CK标记，待验证为何需要遍历）
//                for (FTPFile ftpFile : remoteNames) {
//                    if (fileName.equals(ftpFile.getName())) {
//                        ftpClient.deleteFile(fileName);
//                    }
//                }
            // 直接删除文件（CK改造）
            ftpClient.deleteFile(remoteFileName);
            log.info("FTP服务器:【"+IP+":"+PORT +"】删除文件完成!");
            ftpClient.logout();
            log.info("FTP服务器:【"+IP+":"+PORT +"】退出成功！");
        }catch(IOException e){
            log.error("失败原因："+e.getMessage());
        }finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                    log.info("FTP服务器:【"+IP+":"+PORT +"】断开连接成功！");
                }catch(IOException e){
                    log.error("错误原因："+e.getMessage());
                }
            }
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行删除操作结束，耗时:" + (System.currentTimeMillis() - start) + "毫秒");
    }


    /**
     * 移除服务器上的文件
     * @param filePaths
     * @throws IOException
     */
    public static void deleteFile(List<Map<String,Object>> filePaths){
        if (null == filePaths || filePaths.size() == 0){
            log.info("FTP服务器:【"+IP+":"+PORT +"】执行删除操作，未获取到可删除文件，终止执行删除。");
            return;
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行删除操作开始...");
        long start = System.currentTimeMillis();
        try{
            initFtpClient();
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                log.info("FTP服务器:【"+IP+":"+PORT +"】被异常断开连接！");
            }
            for(Map<String,Object> map:filePaths){
                String remotePath = String.valueOf(map.get("remotePath"));
                String fileName = String.valueOf(map.get("fileName"));

                // 设置文件操作目录
                ftpClient.changeWorkingDirectory(remotePath.replaceAll(Matcher.quoteReplacement(File.separator),"/"));
                /*【主动模式】 工作的原理： FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
                登录成功后要list列表或者读取数据时，客户端随机开放一个端口（1024以上），发送 PORT命令到FTP服务器，
                告诉服务器客户端采用主动模式并开放端口；FTP服务器收到PORT主动模式命令和端口号后，
                通过服务器的20端口和客户端开放的端口连接，发送数据*/
                // ftpClient.enterLocalActiveMode();


                /*【被动模式】工作原理：FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
                登录成功后要list列表或者读取数据时，发送PASV命令到FTP服务器， 服务器在本地随机开放一个端口（1024以上），
                然后把开放的端口告诉客户端， 客户端再连接到服务器开放的端口进行数据传输*/
                ftpClient.enterLocalPassiveMode();
                // 获取文件操作目录下所有文件名称
                // 由于apache不支持中文语言环境，通过定制类解析中文日期类型
                ftpClient.configure(new FTPClientConfig("com.zznode.tnms.ra.c11n.nj.resource.ftp.UnixFTPEntryParser"));
//                FTPFile[] remoteNames = ftpClient.listFiles();
                // 循环比对文件名称，判断是否含有当前要删除的文件名（CK标记，待验证为何需要遍历）
//                for (FTPFile ftpFile : remoteNames) {
//                    if (fileName.equals(ftpFile.getName())) {
//                        ftpClient.deleteFile(fileName);
//                    }
//                }
                // 直接删除文件（CK改造）
                ftpClient.deleteFile(fileName);
            }
            log.info("FTP服务器:【"+IP+":"+PORT +"】删除文件完成!");
            ftpClient.logout();
            log.info("FTP服务器:【"+IP+":"+PORT +"】退出成功！");
        }catch(IOException e){
            log.error("失败原因："+e.getMessage());
        }finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                    log.info("FTP服务器:【"+IP+":"+PORT +"】断开连接成功！");
                }catch(IOException e){
                    log.error("错误原因："+e.getMessage());
                }
            }
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行删除操作结束，耗时:" + (System.currentTimeMillis() - start) + "毫秒");
    }




    /**
     * @Title: FtpUtils
     * @Description: 流对象复制
     * @Param: [input]
     * @return: java.io.ByteArrayOutputStream
     * @throws:
     * @Author: chenkang
     * @Date: 2020-09-19 19:04
     * @version: v1.0
     * Modification History:
     *      @Author       @date        @version   @Description
     *      chenkang    2020-09-19 19:04    v1.0     流对象复制
     * -------------------------------------------------------------
     */
    private static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            log.error("流对象复制失败,失败信息:"+e.getMessage());
            return null;
        }
    }

    /**
     * 文件内容输入到浏览器页面，预览时可用
     * @param filename
     * @param filePath
     * @param response
     * @return
     */
    public static int downloadFileToBrowser(String filename, String filePath, HttpServletResponse response) {
        if (StringUtils.isEmpty(filename) || StringUtils.isEmpty(filePath)){
            log.info("FTP服务器:【"+IP+":"+PORT +"】执行下载操作，未获取到可下载文件，终止执行下载。");
            return 0;
        }
        log.info("FTP服务器:【"+IP+":"+PORT +"】执行下载操作开始...");
        filePath = filePath.replaceAll(Matcher.quoteReplacement(File.separator),"/");
        filePath = filePath.replaceAll("\\\\","/");
        filePath = filePath.replace(filename,"");
        String remoteFileName = "";
        if (filePath.contains(".")) {
            remoteFileName = filePath.substring(filePath.lastIndexOf("/")+1);
            filePath = filePath.substring(0,filePath.lastIndexOf("/"));
        } else {
            remoteFileName = filename;
        }
        try {
            initFtpClient();
            //切换FTP目录
            ftpClient.changeWorkingDirectory(filePath.replaceAll(Matcher.quoteReplacement(File.separator),"/"));
            /*【主动模式】 工作的原理： FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
                登录成功后要list列表或者读取数据时，客户端随机开放一个端口（1024以上），发送 PORT命令到FTP服务器，
                告诉服务器客户端采用主动模式并开放端口；FTP服务器收到PORT主动模式命令和端口号后，
                通过服务器的20端口和客户端开放的端口连接，发送数据*/
            // ftpClient.enterLocalActiveMode();


            /*【被动模式】工作原理：FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
            登录成功后要list列表或者读取数据时，发送PASV命令到FTP服务器， 服务器在本地随机开放一个端口（1024以上），
            然后把开放的端口告诉客户端， 客户端再连接到服务器开放的端口进行数据传输*/
            ftpClient.enterLocalPassiveMode();
            // 由于apache不支持中文语言环境，通过定制类解析中文日期类型
            ftpClient.configure(new FTPClientConfig("com.zznode.tnms.ra.c11n.nj.resource.ftp.UnixFTPEntryParser"));
            ftpClient.retrieveFile(remoteFileName,response.getOutputStream());
            return 0;
        } catch (Exception e) {
            log.error("下载文件失败,失败原因："+e.getMessage());
        } finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                    log.info("FTP服务器:【"+IP+":"+PORT +"】断开连接成功！");
                }catch(IOException e){
                    log.error("错误原因："+e.getMessage());
                }
            }
        }
        return 0;
    }


    /**
    * @Title: FtpUtils
    * @Description: 通过file_path判断文件在服务器上面是否存在
    * @Param:
    * @return:
    * @throws:
    * @Author: liuxiao
    * @Date: 2021/6/21 11:38
    * @version: v1.0
    * Modification History:
    *      @Author       @date        @version   @Description
    *      liuxiao    2021/6/21 11:38    v1.0
    * -------------------------------------------------------------
    */
    public static InputStream checkFileIsExist(String filename,String filePath) {
        InputStream inputStream = null;
//        FTPClient ftpClient = getFtpClient();
        try {
            initFtpClient();
            //切换FTP目录
            ftpClient.changeWorkingDirectory(filePath.replaceAll(Matcher.quoteReplacement(File.separator),"/"));
            /*【主动模式】 工作的原理： FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
                登录成功后要list列表或者读取数据时，客户端随机开放一个端口（1024以上），发送 PORT命令到FTP服务器，
                告诉服务器客户端采用主动模式并开放端口；FTP服务器收到PORT主动模式命令和端口号后，
                通过服务器的20端口和客户端开放的端口连接，发送数据*/
            // ftpClient.enterLocalActiveMode();

            /*【被动模式】工作原理：FTP客户端连接到FTP服务器的21端口，发送用户名和密码登录，
            登录成功后要list列表或者读取数据时，发送PASV命令到FTP服务器， 服务器在本地随机开放一个端口（1024以上），
            然后把开放的端口告诉客户端， 客户端再连接到服务器开放的端口进行数据传输*/
            ftpClient.enterLocalPassiveMode();
            // 由于apache不支持中文语言环境，通过定制类解析中文日期类型
            ftpClient.configure(new FTPClientConfig("com.zznode.tnms.ra.c11n.nj.resource.ftp.UnixFTPEntryParser"));
            // 设置输出流为二进制以处理在Unix环境上文件内容乱码
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 根据指定名称获取指定文件
            inputStream = ftpClient.retrieveFileStream(filename);
        } catch (Exception e) {
            log.error("下载文件失败,失败原因："+e.getMessage());
        } finally{
            if(ftpClient.isConnected()){
                try{
                    ftpClient.disconnect();
                    log.info("FTP服务器:【"+IP+":"+PORT +"】断开连接成功！");
                }catch(IOException e){
                    log.error("错误原因："+e.getMessage());
                }
            }
        }
        return inputStream;
    }





    public static void errrorPageMsg(HttpServletResponse response,String errorMsg){
        response.setContentType("text/html; charset=UTF-8");
        try {
            response.getWriter().print("<!DOCTYPE html>\n" +
                    "<html lang=\"zh-CN\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                    "\n" +
                    "    <title>错误页面</title>\n" +
                    "\n" +
                    "    <style>\n" +
                    "        * { margin: 0; padding: 0; }\n" +
                    "        body { background-color: #f8f8f8; -webkit-font-smoothing: antialiased; }\n" +
                    "        .error { position: absolute; left: 50%; top: 50%; width: 483px; margin: -300px 0 0 -242px; padding-top: 199px; font-size: 18px; color: #666; text-align: center; background: #f8f8f8 url(../images/404.jpg) 0 0 no-repeat; }\n" +
                    "        .error .remind { margin: 30px 0; }\n" +
                    "        .error .button { display: inline-block; padding: 0 20px; line-height: 40px; font-size: 14px; color: #fff; background-color: #f8912d; text-decoration: none; }\n" +
                    "        .error .button:hover { opacity: .9; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "\n" +
                    "<body>\n" +
                    "\n" +
                    "<div class=\"error\">\n" +
                    "    <p class=\"remind\">"+errorMsg+"，请返回上一页！</p>\n" +
                    "    <p><a class=\"button\" href=\"javascript:history.back(-1)\">返回</a></p>\n" +
                    "</div>\n" +
                    "\n" +
                    "\n" +
                    "</body></html>");
        } catch (IOException e) {
            log.error("出现错误：" + e.getMessage());
        }
        try {
            response.getWriter().close();
        } catch (IOException e) {
            log.error("出现错误：" + e.getMessage());
        }
    }

    public static String recursiveFileName(List<String> filenames, String filename) {
        if (filenames.contains(filename)) {
            String shortfilename = filename.substring(0,filename.lastIndexOf("."));
            String suffix = filename.substring(filename.lastIndexOf("."),filename.length());
            if (filename.lastIndexOf("(") > 0 && filename.lastIndexOf(")") > 0
                    && filename.lastIndexOf(")") > filename.lastIndexOf("(")) {
                String numStr = filename.substring(filename.lastIndexOf("(")+1,filename.lastIndexOf(")"));
                try {
                    int num = Integer.valueOf(numStr);
                    shortfilename = shortfilename.substring(0,shortfilename.lastIndexOf("("+numStr+")"));
                    filename = shortfilename+"("+(num+1)+")"+suffix;
                } catch (Exception e) {
                    // 如果转换失败，证明括号内不是数字
                    filename = shortfilename+"(1)"+suffix;
                }
            } else {
                filename = shortfilename+"(1)"+suffix;
            }
            filename = recursiveFileName(filenames,filename);
        }
        return filename;
    }


    /*
     * 从FTP服务器下载文件 到本地
     *
     * @param ftpPath FTP服务器中文件所在路径 格式： ftptest/aa
     * @param localPath 下载到本地的位置 格式：H:/download
     * @param fileName 文件名称
     */
    public static void downloadFtpFile(String fileName,String ftpPath, String localPath
                                       ) {

        try {
            initFtpClient();
            ftpClient.setControlEncoding("UTF-8"); // 中文支持
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(ftpPath);

            File localFile = new File(localPath + File.separatorChar + fileName);
            OutputStream os = new FileOutputStream(localFile);
            ftpClient.retrieveFile(fileName, os);
            os.close();
            ftpClient.logout();

        } catch (FileNotFoundException e) {
            System.out.println("没有找到" + ftpPath + "文件");
            e.printStackTrace();
        } catch (SocketException e) {
            System.out.println("连接FTP失败.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件读取错误。");
            e.printStackTrace();
        }

    }



}
