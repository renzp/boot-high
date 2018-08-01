package com.lixy.boothigh.utils;
/**
 * @Author: MR LIS
 * @Description:
 * @Date: Create in 14:51 2018/8/1
 * @Modified By:
 */

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * ftp工具类
 *
 * @Author: MR LIS
 * @Date: 15:38 2018/8/1
 */
public class FtpUtil {

    private static Logger logger = Logger.getLogger(FtpUtil.class);

    private static FTPClient ftpClient;

    private static String charset = "GBK";

    /**
     * 关闭ftp连接
     */
    public static void closeFtp() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * ftp上传文件到，FtpAtt对象定义的path目录下
     *
     * @param f
     * @throws Exception
     */
    public static void upload(File f) throws Exception {
        if (f.isDirectory()) {
            ftpClient.makeDirectory(f.getName());
            ftpClient.changeWorkingDirectory(f.getName());
            String[] files = f.list();
            for (String fstr : files) {
                File file1 = new File(f.getPath() + File.separator + fstr);
                if (file1.isDirectory()) {
                    upload(file1);
                    ftpClient.changeToParentDirectory();
                } else {
                    File file2 = new File(f.getPath() + File.separator + fstr);
                    FileInputStream input = new FileInputStream(file2);
                    ftpClient.storeFile(file2.getName(), input);
                    input.close();
                }
            }
        } else {
            File file2 = new File(f.getPath());
            FileInputStream input = new FileInputStream(file2);
            ftpClient.storeFile(file2.getName(), input);
            input.close();
        }
    }

    /**
     * 获取ftp连接
     *
     * @param f
     * @return
     * @throws Exception
     */
    public static boolean connectFtp(FtpAtt f) throws Exception {
        ftpClient = new FTPClient();
        boolean flag = false;
        int reply;
        if (f.getPort() == null) {
            ftpClient.connect(f.getIpAddr(), 21);
        } else {
            ftpClient.connect(f.getIpAddr(), f.getPort());
        }
        ftpClient.login(f.getUserName(), f.getPwd());
        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            return flag;
        }
        ftpClient.changeWorkingDirectory(f.getPath());
        flag = true;
        return flag;
    }

    /**
     * 根据流上传文件到ftp,名称为remoteFileName
     *
     * @param input          流
     * @param remoteFileName 保存在ftp的文件名
     * @throws Exception
     * @Author: MR LIS
     * @Date: 15:43 2018/8/1
     */
    public static void upload(FtpAtt ftpAtt,InputStream input, String remoteFileName) {

        try {
            if(connectFtp(ftpAtt)) {
                ftpClient.storeFile(remoteFileName, input);
            }else{
                logger.error("链接失败！");
            }
        } catch (IOException e) {
            logger.error("文件" + remoteFileName + "上传到ftp失败", e);
        } catch (Exception e) {
            logger.error("文件" + remoteFileName + "上传到ftp失败", e);
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //关闭连接
            closeFtp();
        }

    }
    /**
     * 下载链接配置
     *
     * @param f
     * @param localBaseDir  本地目录
     * @param remoteBaseDir 远程目录
     * @throws Exception
     */
    public static void downLoad(FtpAtt f, String localBaseDir, String remoteBaseDir) throws Exception {
        if (connectFtp(f)) {

            try {
                FTPFile[] files = null;
                boolean changedir = ftpClient.changeWorkingDirectory(File.separator + remoteBaseDir);
                if (changedir) {
                    ftpClient.setControlEncoding(charset);
                    files = ftpClient.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        try {
                            downloadFile(files[i], localBaseDir, File.separator + remoteBaseDir);
                        } catch (Exception e) {
                            logger.error(e);
                            logger.error("<" + files[i].getName() + ">下载失败");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.error("下载过程中出现异常");
            }finally {
                closeFtp();
            }
        } else {
            logger.error("链接失败！");
        }

    }

    /**
     * 下载指定名称的文件
     *
     * @param f
     * @param localBaseDir  本地目录
     * @param remoteBaseDir 远程目录
     * @throws Exception
     */
    public static void downLoad(FtpAtt f, String localBaseDir, String remoteBaseDir, String fileName) throws Exception {
        if (connectFtp(f)) {

            try {
                FTPFile[] files = null;
                boolean changedir = ftpClient.changeWorkingDirectory(remoteBaseDir);
                if (changedir) {
                    ftpClient.setControlEncoding(charset);
                    files = ftpClient.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        try {
                            /**
                             * 如果文件名不为空，则说明只下载对应文件名称的文件
                             */
                            if (StringUtils.isNotBlank(fileName) && files[i].getName().equals(fileName)) {
                                downloadFile(files[i], localBaseDir, remoteBaseDir);
                            }
                        } catch (Exception e) {
                            logger.error(e);
                            logger.error("<" + files[i].getName() + ">下载失败");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e);
                logger.error("下载过程中出现异常");
            }finally {
                closeFtp();
            }
        } else {
            logger.error("链接失败！");
        }

    }

    /**
     * 下载FTP文件
     * 当你需要下载FTP文件的时候，调用此方法
     * 根据<b>获取的文件名，本地地址，远程地址</b>进行下载
     *
     * @param ftpFile
     * @param relativeLocalPath  本地目录
     * @param relativeRemotePath ftp目录
     */
    private static void downloadFile(FTPFile ftpFile, String relativeLocalPath, String relativeRemotePath) {
        if (ftpFile.isFile()) {
            if (ftpFile.getName().indexOf("?") == -1) {
                OutputStream outputStream = null;
                try {
                    File locaFile = new File(relativeLocalPath + File.separator + ftpFile.getName());
                    //判断文件是否存在，存在则返回
                    if (locaFile.exists()) {
                        return;
                    } else {
                        outputStream = new FileOutputStream(relativeLocalPath + File.separator + ftpFile.getName());
                        ftpClient.retrieveFile(ftpFile.getName(), outputStream);
                        outputStream.flush();
                        outputStream.close();
                    }
                } catch (Exception e) {
                    logger.error(e);
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        logger.error("输出文件流异常");
                    }
                }
            }
        } else {
            String newlocalRelatePath = relativeLocalPath + ftpFile.getName();
            String newRemote = new String(relativeRemotePath + File.separator + ftpFile.getName().toString());
            File fl = new File(newlocalRelatePath);
            if (!fl.exists()) {
                fl.mkdirs();
            }
            try {
                newlocalRelatePath = newlocalRelatePath + File.separator;
                newRemote = newRemote + File.separator;
                String currentWorkDir = ftpFile.getName().toString();
                boolean changedir = ftpClient.changeWorkingDirectory(currentWorkDir);
                if (changedir) {
                    FTPFile[] files = null;
                    files = ftpClient.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        downloadFile(files[i], newlocalRelatePath, newRemote);
                    }
                }
                if (changedir) {
                    ftpClient.changeToParentDirectory();
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        FtpAtt f = new FtpAtt();
        f.setIpAddr("192.168.19.161");
        f.setUserName("ftpserver");
        f.setPwd("ftp@123456");
        f.setPath("excelDir");
        FtpUtil.connectFtp(f);
/*        File file = new File("D:\\fileDir\\excel\\81_syscategoryinfo.xlsx");
        FtpUtil.upload(new FileInputStream(file),"hhhh2.xlsx");*/
//        FtpUtil.upload(file);//把文件上传在ftp上
//        FtpUtil.downLoad(f, "e:/",  "excelDir");//下载ftp文件测试
        FtpUtil.downLoad(f, "e:/", "excelDir", "hhhh.xlsx");//下载ftp文件测试
        System.out.println("ok");

    }
}