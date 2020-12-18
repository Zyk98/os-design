package osdesign.myClass;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 将文件大小由Byte转为MB或者KB
 */
public class byteTo {

    public String fileSizeByteToM(Long size) {

        BigDecimal fileSize = new BigDecimal(size);
        BigDecimal param = new BigDecimal(1024);
        int count = 0;
        while(fileSize.compareTo(param) > 0 && count < 5)
        {
            fileSize = fileSize.divide(param);
            count++;
        }
        DecimalFormat df = new DecimalFormat("#.##");
        String result = df.format(fileSize) + "";
        switch (count) {
            case 0:
                result += "B";
                break;
            case 1:
                result += "KB";
                break;
            case 2:
                result += "MB";
                break;
            case 3:
                result += "GB";
                break;
            case 4:
                result += "TB";
                break;
            case 5:
                result += "PB";
                break;
        }
        return result;
    }
}
