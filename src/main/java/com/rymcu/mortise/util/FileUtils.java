package com.rymcu.mortise.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件操作工具类
 *
 * @author 张代浩
 */
@Slf4j
public class FileUtils {

    /**
     * 获取文件扩展名,带.
     *
     * @param filename 文件名
     * @return 文件扩展名
     */
    public static String getExtend(String filename) {
        return getExtend(filename, "");
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @param defExt   默认文件后缀名， 带.
     * @return 文件扩展名
     */
    public static String getExtend(String filename, String defExt) {
        if (StringUtils.isEmpty(filename)) {
            return defExt.toLowerCase();
        }

        int i = filename.lastIndexOf('.');

        if ((i > 0) && (i < (filename.length() - 1))) {
            String result = filename.substring(i).toLowerCase();
            if (result.contains("?")) {
                return result.split("\\?")[0];
            }
            return result;
        }

        return defExt.toLowerCase();
    }

    /**
     * 获取文件名称[不含后缀名]
     *
     * @param fileName 文件名
     * @return String
     */
    public static String getFilePrefix(String fileName) {
        int splitIndex = fileName.lastIndexOf(".");
        return fileName.substring(0, splitIndex).replaceAll("\\s*", "");
    }

    /**
     * 获取文件名称[不含后缀名]
     * 不去掉文件目录的空格
     *
     * @param fileName 文件名
     * @return String
     */
    public static String getFilePrefix2(String fileName) {
        int splitIndex = fileName.lastIndexOf(".");
        return fileName.substring(0, splitIndex);
    }

    /**
     * 文件复制
     * 方法摘要：这里一句话描述方法的用途
     *
     * @param inputFile 输入文件
     * @param outputFile 输出文件
     */
    public static void copyFile(String inputFile, String outputFile) throws FileNotFoundException {
        File sFile = new File(inputFile);
        File tFile = new File(outputFile);
        int temp = 0;
        byte[] buf = new byte[10240];
        try (FileInputStream fis = new FileInputStream(sFile); FileOutputStream fos = new FileOutputStream(tFile)) {
            try {
                while ((temp = fis.read(buf)) != -1) {
                    fos.write(buf, 0, temp);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 判断文件是否为图片<br>
     * <br>
     *
     * @param filename 文件名<br>
     *                 判断具体文件类型<br>
     * @return 检查后的结果<br>
     */
    public static boolean isPicture(String filename) {
        // 文件名称为空的场合
        if (StringUtils.isBlank(filename)) {
            // 返回不和合法
            return false;
        }
        // 获得文件后缀名
        //String tmpName = getExtend(filename);
        // 声明图片后缀名数组
        String[] imageArray = {"bmp", "dib", "gif", "jfif", "jpe",
                "jpeg", "jpg", "png", "tif", "tiff", "ico"};
        // 遍历名称数组
        for (String s : imageArray) {
            if (s.equals(filename)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文件是否为DWG<br>
     * <br>
     *
     * @param filename 文件名<br>
     *                 判断具体文件类型<br>
     * @return 检查后的结果<br>
     */
    public static boolean isDwg(String filename) {
        // 文件名称为空的场合
        if (StringUtils.isEmpty(filename)) {
            // 返回不和合法
            return false;
        }
        // 获得文件后缀名
        String tmpName = getExtend(filename);
        // 声明图片后缀名数组
        return "dwg".equals(tmpName);
    }

    /**
     * 删除指定的文件
     *
     * @param strFileName 指定绝对路径的文件名
     * @return 如果删除成功true否则false
     */
    public static boolean delete(String strFileName) {
        File fileDelete = new File(strFileName);

        if (!fileDelete.exists() || !fileDelete.isFile()) {
            log.error("错误: {} 不存在!", strFileName);
            return false;
        }

        //LogUtil.info("--------成功删除文件---------"+strFileName);
        return fileDelete.delete();
    }

    /**
     * @param fileName 文件名
     * @return 设定文件
     */
    public static String encodingFileName(String fileName) {
        String returnFileName = "";
        try {
            returnFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            returnFileName = returnFileName.replace("+", "%20");
            if (returnFileName.length() > 150) {
                returnFileName = new String(fileName.getBytes("GB2312"), "ISO8859-1");
                returnFileName = returnFileName.replace(" ", "%20");
            }
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            //LogUtil.info("Don't support this encoding ...");
        }
        return returnFileName;
    }

    /**
     * 根据现有路径获取SWF文件名称
     *
     * @author taoYan
     * @since 2018年7月26日
     */
    public static String getSwfPath(String path) {
        String leftSlash = "/";
        if (!File.separator.equals(leftSlash)) {
            path = path.replace(File.separator, leftSlash);
        }
        String fileDir = path.substring(0, path.lastIndexOf(leftSlash) + 1);//文件目录带/
        int pointPosition = path.lastIndexOf(".");
        String fileName = path.substring(path.lastIndexOf(leftSlash) + 1, pointPosition);//文件名不带后缀
        String swfName = "";//PinyinUtil.getPinYinHeadChar(fileName);// 取文件名首字母作为SWF文件名
        return fileDir + swfName + ".swf";
    }

    /**
     * 上传txt文件，防止乱码
     *
     * @author taoYan
     * @since 2018年7月26日
     */
    public static void uploadTxtFile(MultipartFile mf, String savePath) throws IOException {
        //利用utf-8字符集的固定首行隐藏编码原理
        //Unicode:FF FE   UTF-8:EF BB
        byte[] allbytes = mf.getBytes();
        try {
            String head1 = toHexString(allbytes[0]);
            //System.out.println(head1);
            String head2 = toHexString(allbytes[1]);
            //System.out.println(head2);
            if ("ef".equals(head1) && "bb".equals(head2)) {
                //UTF-8
                String contents = new String(mf.getBytes(), StandardCharsets.UTF_8);
                if (StringUtils.isNotBlank(contents)) {
                    OutputStream out = new FileOutputStream(savePath);
                    out.write(contents.getBytes());
                    out.close();
                }
            } else {

                //GBK
                String contents = new String(mf.getBytes(), "GBK");
                OutputStream out = new FileOutputStream(savePath);
                out.write(contents.getBytes());
                out.close();

            }
        } catch (Exception e) {
            String contents = new String(mf.getBytes(), StandardCharsets.UTF_8);
            if (StringUtils.isNotBlank(contents)) {
                OutputStream out = new FileOutputStream(savePath);
                out.write(contents.getBytes());
                out.close();
            }
        }
    }

    public static String toHexString(int index) {
        String hexString = Integer.toHexString(index);
        // 1个byte变成16进制的，只需要2位就可以表示了，取后面两位，去掉前面的符号填充
        hexString = hexString.substring(hexString.length() - 2);
        return hexString;
    }
}
