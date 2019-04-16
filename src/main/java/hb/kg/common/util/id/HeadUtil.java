package hb.kg.common.util.id;

import java.util.ArrayList;
import java.util.List;

public class HeadUtil {
    public static List<String> headImgPath = new ArrayList<>();
    static {
        headImgPath.add("http://scvlive.oss-cn-beijing.aliyuncs.com/boy1.png");
        headImgPath.add("http://scvlive.oss-cn-beijing.aliyuncs.com/boy2.png");
        headImgPath.add("http://scvlive.oss-cn-beijing.aliyuncs.com/boy3.png");
        headImgPath.add("http://scvlive.oss-cn-beijing.aliyuncs.com/girl1.png");
        headImgPath.add("http://scvlive.oss-cn-beijing.aliyuncs.com/girl2.png");
        headImgPath.add("http://scvlive.oss-cn-beijing.aliyuncs.com/girl3.png");
    }
    public static String initHead(Integer sex) {
        if (sex == null) { // 性别为空
            int value = (int) (Math.random() * 6);
            return headImgPath.get(value);
        } else {
            if (sex == 1) { // 女
                int value = (int) (Math.random() * 3);
                return headImgPath.get(value + 3);
            } else if (sex == 0) {// 男
                int value = (int) (Math.random() * 3);
                return headImgPath.get(value);
            }
        }
        return headImgPath.get(0);
    }

    public static String initHead(String sex) {
        if (sex == null) { // 性别为空
            int value = (int) (Math.random() * 6);
            return headImgPath.get(value);
        } else {
            if (sex == "女") { // 女
                int value = (int) (Math.random() * 3);
                return headImgPath.get(value + 3);
            } else if (sex == "男") {// 男
                int value = (int) (Math.random() * 3);
                return headImgPath.get(value);
            }
        }
        return headImgPath.get(0);
    }
}
