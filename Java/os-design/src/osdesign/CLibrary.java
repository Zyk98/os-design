package osdesign;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * DLL动态库调用接口
 * 调用编写的demo.dll
 */
public interface CLibrary extends Library {
    //CLibrary INSTANCE = (CLibrary) Native.loadLibrary((Platform.isWindows() ? "KERNEL32" : "c"),CLibrary.class);

    CLibrary INSTANCE2 = (CLibrary)Native.loadLibrary("demo", CLibrary.class);   //java的反射机制

    //下面声明要调用的DLL中的方法

    //弹出关于消息框
    void box();

    //复制文件
    void copyFile(String src,String dest);

    //剪切文件
    void moveFile(String src,String dest);

    //删除文件
    void deleteFile(String fileName);

    //创建一个txt文件
    boolean createTxtFile(String fileName);

    //保存文件对话框
    void save();
}
