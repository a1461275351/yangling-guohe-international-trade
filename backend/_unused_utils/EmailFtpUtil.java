package cn.nodesoft.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.*;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.processor.RowListProcessor;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParser;
import org.junit.jupiter.params.shadow.com.univocity.parsers.csv.CsvParserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Matcher;

@Component
@Slf4j
public class EmailFtpUtil {
    @Autowired
    private static ThreadPoolExecutor executor;
	
    /** 日志对象 */
    private static final Logger logger = LoggerFactory.getLogger(EmailFtpUtil.class);
    // ftp客户端
    private static FTPClient ftpClient = new FTPClient();

    //ftp连接参数
    private static String ip = GlobalUtil.getEnvironmentProperty("email.ftp.ip");
    private static String port = GlobalUtil.getEnvironmentProperty("email.ftp.port");
    private static String userName = GlobalUtil.getEnvironmentProperty("email.ftp.user");
    private static String passWord = GlobalUtil.getEnvironmentProperty("email.ftp.password");


    //本地测试ftp连接参数
//    private static String ip = GlobalUtil.getEnvironmentProperty("ftp.ip");
//    private static String port = GlobalUtil.getEnvironmentProperty("ftp.port");
//    private static String userName = GlobalUtil.getEnvironmentProperty("ftp.user");
//    private static String passWord = GlobalUtil.getEnvironmentProperty("ftp.password");

    //本地字符编码
    static String LOCAL_CHARSET = "UTF-8";
    // FTP协议里面，规定文件名编码为iso-8859-1
    static String SERVER_CHARSET = "ISO-8859-1";

    /**
     *	获取FTPClient对象
     *
     * @return
     */
    public static FTPClient getFTPClient() {
        logger.info("准备连接ftp服务器...");
        logger.error("准备连接ftp服务器...");
        logger.info("读取application-jd.yml配置文件，连接ftp服务器参数，ip：" + ip);
        logger.info("读取application-jd.yml配置文件，连接ftp服务器参数，port：" + port);
        logger.info("读取application-jd.yml配置文件，连接ftp服务器参数，userName：" + userName);
        logger.info("读取application-jd.yml配置文件，连接ftp服务器参数，passWord：" + passWord);
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient = new FTPClient();
            // 连接FTP服务器
            ftpClient.connect(ip, Integer.parseInt(port));
            // 登陆FTP服务器
            ftpClient.login(userName, passWord);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                logger.info("未连接到FTP，用户名或密码错误。");
                logger.error("未连接到FTP，用户名或密码错误。");
                ftpClient.disconnect();
            } else {
                logger.info("FTP连接成功。");
            }
        } catch (SocketException e) {
            logger.error("FTP的IP地址可能错误，请正确配置。");
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("FTP的端口错误,请正确配置。");
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        logger.info("连接FTP执行完成。");
        return ftpClient;
    }

    /**
     *	Description: 向FTP服务器上传文件
     * @Title: uploadFile
     * @Description: 向FTP服务器上传文件
     * @param @param ftpPath
     * @param @param fileName
     * @param @param input
     * @param @return 参数
     * @return boolean 返回类型
     * @throws
     */
    public static boolean uploadFile(String ftpPath, String fileName, InputStream input) throws Exception {
        boolean success = false;
        FTPClient ftpClient = null;
        try {
            int reply;
            ftpClient = getFTPClient();
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return success;
            }
            // 中文支持
            ftpClient.setControlEncoding("UTF-8");
            //ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //设置linux环境
            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
            ftpClient.configure(conf);
            if (isDirExist(ftpClient, ftpPath)) {
                ftpClient.changeWorkingDirectory(ftpPath);
            } else {
                //ftpClient.makeDirectory(ftpPath);
                //ftpClient.changeWorkingDirectory(ftpPath);
                ftpPath = ftpPath.replace("\\","/");
                String[] flolder = ftpPath.split("/");//示例  DestFolder： 2021/03/03
                for (int i = 0; i < flolder.length; i++) {  
                    if (!ftpClient.changeWorkingDirectory(flolder[i])) {//切换工作目录  返回true切换成功说明有这个文件夹， 反之 
                        ftpClient.makeDirectory(flolder[i]);//  没有这个文件夹的话 创建这个文件夹
                        ftpClient.changeWorkingDirectory(flolder[i]); //创建好后再切换工作目录
                    } 
                }
            }

            //ftpClient.enterLocalPassiveMode();
            ftpClient.storeFile(new String(fileName.getBytes(LOCAL_CHARSET),SERVER_CHARSET), input);

            //input.close();
            ftpClient.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }


    /**
     * 	判断Ftp目录是否存在
     * @Title: isDirExist
     * @Description: TODO(这里用一句话描述这个方法的作用)
     * @param @param ftpClient
     * @param @param dir
     * @param @return
     * @param @throws IOException 参数
     * @return boolean 返回类型
     * @throws
     */
    private static boolean isDirExist(FTPClient ftpClient, String dir) throws IOException {
        boolean b = ftpClient.changeWorkingDirectory(dir);
        return b;
    }

    /**
     *	功能：根据文件名称，下载文件流
     * @Title: download
     * @Description: 功能：根据文件名称，下载文件流
     * @param @param filename
     * @param @param response
     * @param @throws IOException 参数
     * @return void 返回类型
     * @throws
     */

    public static void download(String filename, HttpServletResponse response) throws Exception {
    	if (StringUtils.isBlank(filename)) {
    	    logger.error("filename参数为空，这将导致ftp服务器无法追踪到目标文件，请检查");
        }
    	logger.info("即将开始下载文件，路径：" + filename);
        String[] strs = filename.split("/");
        String downloadFile = strs[strs.length - 1];
        try {
        	// 设置文件ContentType类型，这样设置，会自动判断下载文件类型
            response.setContentType("application/x-msdownload");
            // 设置文件头：最后一个参数是设置下载的文件名并编码为UTF-8
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(downloadFile, "UTF-8"));
            // 建立连接
            connectToServer();
            // 中文支持
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //设置linux环境
            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
            ftpClient.configure(conf);
            // 设置传输二进制文件
            int reply = ftpClient.getReplyCode();
            ftpClient.changeWorkingDirectory("./home/ftp");
            if(!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                throw new IOException("failed to connect to the FTP Server: ip错误");
            }
            // 此句代码适用Linux的FTP服务器
            String newPath = new String(filename.getBytes("UTF-8"),"ISO-8859-1");
            // ftp文件获取文件
            InputStream is = null;
            BufferedInputStream bis = null;
            OutputStream out = null;
            try {
                is = ftpClient.retrieveFileStream(newPath);
                bis = new BufferedInputStream(is);
                out = response.getOutputStream();
                byte[] buf = new byte[1024];
                int len = 0;
                while ((len = bis.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeConnect();
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            out.close();
        } catch (FTPConnectionClosedException e) {
        	logger.error("ftp连接被关闭！", e);
            throw e;
        } catch (Exception e) {
        	logger.error("ERR : upload file "+ filename+ " from ftp : failed!", e);
        }
    }

    /**
     * 从FTP服务器下载多个文件
     *
     * @param fileNames
     *            要下载的文件名列表
     * @param localPath
     *            本地存放路径
     * @return
     */
    public static boolean downFile(List<String> fileNames, String localPath) {
        // TODO Auto-generated method stub
        boolean result = false;
        try {
            int reply;
            ftpClient.setControlEncoding("UTF-8");

            ftpClient.connect(ip, Integer.parseInt(port));
            // 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftpClient.login(userName, passWord);// 登录
            // 设置文件传输类型为二进制
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            // 获取ftp登录应答代码
            reply = ftpClient.getReplyCode();
            // 验证是否登陆成功
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                logger.info("ftp服务器登陆失败");
                return result;
            }
            // 转移到FTP服务器目录至指定的目录下
            ftpClient.changeWorkingDirectory("./home/ftp");
            for(String fileName : fileNames){
                // 获取文件列表
                FTPFile[] fs = ftpClient.listFiles();
                for (FTPFile ff : fs) {
                    if (ff.isFile() && StringUtils.equals(ff.getName(), fileName)) {
                        File localFile = new File(localPath + File.separator + ff.getName());
                        if(ff.getSize() == 0){//如果是空文件，直接在本地创建同名空文件
                            File lf = new File(localPath);
                            if(!lf.exists()){
                                lf.mkdirs();
                            }
                            if(!localFile.createNewFile()){
                                return result;
                            }
                        }else{
                            if(!localFile.getParentFile().exists()){
                                localFile.getParentFile().mkdirs();
                            }
                            /*if(localFile.exists()){
                            	localFile.delete();
                            }*/
                            OutputStream is = new FileOutputStream(localFile);
                            if(!ftpClient.retrieveFile(ff.getName(), is)){
                                return result;
                            }
                            is.close();
                        }
                    }
                }
            }
            result = true;
            ftpClient.logout();
        } catch (SocketException e) {
            logger.error("ftp连接失败，堆栈信息：", e);
        } catch (IOException e) {
            logger.error("发生IO异常，堆栈信息：", e);
        } catch (Exception e) {
            logger.error("发生异常，堆栈信息：", e);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                    logger.error("关闭连接时发生异常，堆栈信息为：", ioe);
                }
            }
        }
        return result;
    }
	
	/**
	 * 	连接到ftp服务器
	 * @Title: connectToServer
	 * @Description: 连接到ftp服务器
	 * @param @throws Exception 参数
	 * @return void 返回类型
	 * @throws
	 */
    private static void connectToServer() throws Exception {
        if (!ftpClient.isConnected()) {
            int reply;
            try {
                logger.info("准备重新连接ftp服务器...");
                logger.info("读取application-jd.yml配置文件，重新连接ftp服务器参数，ip：" + ip);
                logger.info("读取application-jd.yml配置文件，重新连接ftp服务器参数，port：" + port);
                logger.info("读取application-jd.yml配置文件，重新连接ftp服务器参数，userName：" + userName);
                logger.info("读取application-jd.yml配置文件，重新连接ftp服务器参数，passWord：" + passWord);
                ftpClient = new FTPClient();
                ftpClient.connect(ip, Integer.parseInt(port));
                ftpClient.login(userName, passWord);
                reply = ftpClient.getReplyCode();

                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect();
                    logger.info("connectToServer FTP server refused connection.");
                }

            }catch(FTPConnectionClosedException ex){
            	logger.error("服务器:IP："+ ip +"没有连接数！there are too many connected users,please try later", ex);
                throw ex;
            }catch (Exception e) {
            	logger.error("登录ftp服务器【"+ip+"】失败", e);
                throw e;
            }
        }
    }
    
    /**
     *	功能：关闭连接
     * @Title: closeConnect
     * @Description: 功能：关闭连接
     * @return void 返回类型
     * @throws
     */
	public static void closeConnect() {
		try {
			if (ftpClient != null) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
            logger.info("ftp连接已正常关闭！");
		} catch (Exception e) {
			logger.error("ftp连接关闭失败！", e);
		}
	}

    /**
     *	Description: 向FTP服务器上传文件
     * @Title: uploadFile
     * @Description: 向FTP服务器上传文件
     * @param @param ftpPath
     * @param @param fileName
     * @param @param input
     * @param @return 参数
     * @return boolean 返回类型
     * @throws
     */
    public static boolean ftpReadCSV(String ftpDirectory, String fileName) {
        boolean result = false;
        FTPClient ftpClient = null;
        try {
            int reply;
            ftpClient = getFTPClient();
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return result;
            }
            ftpClient.changeWorkingDirectory(ftpDirectory);//找到指定目录
            InputStream inputStream=ftpClient.retrieveFileStream(fileName);//根据指定名称获取指定文件
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream,"GBK"));
            String line=null;
            StringBuilder stringBuilder=new StringBuilder(150);
            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }
            String context=stringBuilder.toString();

            String[] sz = context.replaceAll("\"","").split("\n");
            for (int i = 0; i < sz.length; i++)
            {
                System.out.println(sz[i]);//输出每一行的数据
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return result;
    }
    public static void main(String[] args) {
        //ftp
//        System.out.print(GlobalUtil.getJDValue("ftp.basePath"));
//        String[] dates=DateUtil.getDate().split("-");
//        String filePrefixSum = GlobalUtil.getJDValue("ftp.basePath")+GlobalUtil.getJDValue("ftp.rpaFilePrefix")+"/"+dates[0]+"/"+dates[1]+"/"+dates[2];
//        String fileNameCustomer=filePrefixSum+"/"+GlobalUtil.getJDValue("ftp.rapCustomersuffix");
//        ftpReadCSV(filePrefixSum,GlobalUtil.getJDValue("ftp.rapCustomersuffix"));
        //本地读取csv
//        String filePath="F:\\ftp\\nodesoft\\rpafiles\\2022\\06\\09\\代发_个人客户规模分析.csv";
//        ArrayList<String> strings = readCsvByBufferedReader(new File(filePath));
//        System.out.print(strings.size());
//        CompletableFuture<String> supportComplete = CompletableFuture.supplyAsync(()-> {
//            calc();
//            System.out.println("第一个有返回值的异步任务");
//            return "第一个返回值";
//        }).handle((e,throwable) -> {
//            System.out.println("当前线程名称为:" + Thread.currentThread().getName());
//            System.out.println("异常"+throwable);
//            return "有返回值";
//        });
//        CompletableFuture<String> runAsync = CompletableFuture.supplyAsync(()-> {
//            System.out.println("第2个有返回值的异步任务");
//            return "第2个返回值";
//        });
//            CompletableFuture.allOf(supportComplete, runAsync).get();
        HashSet<String> set = new HashSet();
        set.add("12131");
        set.add("1212");
        set.add("12131");
        System.out.print(set.size());

    }

//        public static int calc() {
//            return 1 / 0;
//        }

    /**
     * BufferedReader 读取
     * @param file
     * @return
     */
    public static ArrayList<String> readCsvByBufferedReader(File file) {
        file.setReadable(true);
        file.setWritable(true);
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            br = new BufferedReader(isr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String line = "";
        ArrayList<String> records = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                records.add(line);
            }
            System.out.println("csv表格读取行数：" + records.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }
    public static List<String[]> parseCSV(File file){
        //创建一个配置选项，用来提供多种配置选项
        CsvParserSettings parserSettings = new CsvParserSettings();
        //打开解析器的自动检测功能，让它自动检测输入中包含的分隔符
        parserSettings.setLineSeparatorDetectionEnabled(true);

        //创建RowListProcessor对象，用来把每个解析的行存储在列表中
        RowListProcessor rowListProcessor = new RowListProcessor();
        parserSettings.setProcessor(rowListProcessor);  //配置解析器
        //待解析的CSV文件包含标题头，把第一个解析行看作文件中每个列的标题
        parserSettings.setHeaderExtractionEnabled(true);
        parserSettings.setLineSeparatorDetectionEnabled(true);

        //创建CsvParser对象，用于解析文件
        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(file);

        //如果解析中包含标题，用于获取标题
        String[] headers = rowListProcessor.getHeaders();
        //获取行值，并遍历打印
        List<String[]> rows = rowListProcessor.getRows();
        /*for(int i = 0; i < rows.size(); i++){
            System.out.println(Arrays.asList(rows.get(i)));
        }*/
        return rows;
    }
    /** * 删除文件 *
     * @param pathname FTP服务器保存目录 *
     * @param filename 要删除的文件名称 *
     * @return */
    public static boolean deleteFile(String pathName, String filename) {
        boolean flag = false;
//        pathName = pathName.replaceAll(Matcher.quoteReplacement(File.separator),"/");
//        pathName = pathName.replaceAll("\\\\","/");
        try {
            System.out.println("开始删除文件");
            FTPClient ftpClient = null;
            ftpClient = getFTPClient();
//            initFtpClient();
            //切换FTP目录

            // 中文支持
            ftpClient.setControlEncoding("UTF-8");
            //ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            //设置linux环境
            FTPClientConfig conf = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
            ftpClient.configure(conf);
            if (isDirExist(ftpClient, pathName)) {
                ftpClient.changeWorkingDirectory(pathName);
            } else {
//                ftpClient.makeDirectory(pathName);
//                ftpClient.changeWorkingDirectory(pathName);
//                pathName = pathName.replace("\\","/");
//                String[] flolder = pathName.split("/");//示例  DestFolder： 2021/03/03
//                pathName = pathName.replace("\\","/");
                String[] flolder = pathName.split("\\\\");//示例  DestFolder： 2021/03/03
                for (int i = 0; i < flolder.length; i++) {
                    if (!ftpClient.changeWorkingDirectory(flolder[i])) {//切换工作目录  返回true切换成功说明有这个文件夹， 反之
                        ftpClient.makeDirectory(flolder[i]);//  没有这个文件夹的话 创建这个文件夹
                        ftpClient.changeWorkingDirectory(flolder[i]); //创建好后再切换工作目录
                    }
                }
            }


//            ftpClient.changeWorkingDirectory(pathName);
            log.info("目录切换成功,当前目录为：" + ftpClient.printWorkingDirectory());
            boolean dele = ftpClient.deleteFile(new String(filename.getBytes("utf-8"),"iso-8859-1"));
            System.out.println("删除文件==="+dele);
            ftpClient.logout();
            flag = true;
            System.out.println("删除文件成功");
        } catch (Exception e) {
            System.out.println("删除文件失败");
            e.printStackTrace();
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return flag;
    }

}