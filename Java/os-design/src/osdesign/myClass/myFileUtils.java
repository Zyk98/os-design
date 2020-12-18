package osdesign.myClass;

import osdesign.CLibrary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class myFileUtils {
    /**
     * 新建文本文档txt文件
     * @param src 源文件
     */
    public void newFile(String src) {
        try {
            if(CLibrary.INSTANCE2.createTxtFile(src)){
                System.out.println("new a txt successfully");
            }else{
                System.out.println("new a txt fail");
            }
        } catch (Exception e) {
            System.out.println("新建txt文件操作出错");
        }
    }


    /**
     * 剪切单个文件
     * @param src 源文件
     * @param dest 目的文件
     */
    public void moveFile(String src,String dest) {
        try {
            CLibrary.INSTANCE2.moveFile(src,dest);
        } catch (Exception e) {
            System.out.println("剪切文件操作出错");
        }
    }

    /**
     * 复制单个文件
     * @param src 源文件
     * @param dest 目的文件
     */
    public void copyFile(String src,String dest) {
        try {
            CLibrary.INSTANCE2.copyFile(src,dest);
        } catch (Exception e) {
            System.out.println("复制文件操作出错");
        }
    }


    /**
     * 复制整个文件夹内容
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public void copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
            File a=new File(oldPath);
            String[] file=a.list();
            File temp=null;
            for (int i = 0; i < file.length; i++) {
                if(oldPath.endsWith(File.separator)){
                    temp=new File(oldPath+file[i]);
                }
                else{
                    temp=new File(oldPath+File.separator+file[i]);
                }

                if(temp.isFile()){
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/" +
                            (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ( (len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if(temp.isDirectory()){//如果是子文件夹
                    copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]);
                }
            }
        }
        catch (Exception e) {
            System.out.println("复制整个文件夹内容操作出错");
            e.printStackTrace();

        }

    }


    /**
     * 取得文件最后一次修改的时间
     * @param file
     * @return
     */
    public String lastTime(File file) {
        long lastModified = file.lastModified();
        Date date = new Date(lastModified);
        date.setTime(lastModified);
        SimpleDateFormat s = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = s.format(date);
        return time;
    }

    /**
     * 删除单个文件
     * @param filePathAndName
     */
    public void deleteFile(String filePathAndName) {
        try {
            CLibrary.INSTANCE2.deleteFile(filePathAndName);
        } catch (Exception e) {
            System.out.println("删除文件操作出错");
        }
    }


    /**
     * 删除文件夹
     * @param folderPath
     */
    public void deleteFolder(String folderPath) {
        try {
            deleteAllFile(folderPath);  //删除完里面所有内容
            String filePath = folderPath;
            File myFilePath = new File(filePath);
            myFilePath.delete();  //删除空文件夹
        } catch (Exception e) {
            System.out.println("删除文件夹操作出错");
        }
    }


    /**
     * 删除文件夹里面的所有文件
     * @param path
     */
    public void deleteAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                deleteAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
                deleteFolder(path + "/" + tempList[i]);//再删除空文件夹
            }
        }
    }
}
