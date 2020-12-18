package osdesign.myClass;

import java.io.File;

//文件结点
public class nodeData
{
    public File f;
    public String Name;

    public nodeData(File f, String Name) {
        this.f = f;
        this.Name = Name;
    }

    public nodeData(File file) {
        this.f = file;
    }

    public String toString() {
        return Name;
    }

    public void changeString(String s) {
        Name = s;
    }

}