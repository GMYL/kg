package hb.kg;

import java.util.HashMap;
import java.util.Map;
//第三题
import java.util.Scanner;

public class thirdly {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine();// 输入的字符串
        System.out.println("" + altemate(str));
    }

    static int altemate(String s) {
        // 第一步，取出n种元素
        String strs = "";
        for (int i = 0; i < s.length(); i++) {
            if (strs.indexOf(s.charAt(i)) == -1) {
                strs += s.charAt(i);
            }
        }
        // 第二步取出排列组合的strs.length()中的2种结果
        for (int i = 0; i < strs.length() - 1; i++) {
            for (int j = i + 1; j < strs.length(); j++) {
                String st = "" + strs.charAt(i) + strs.charAt(j); // st是n种元素中的两个
                String res = "";
                // 将原字符串种的等于st种任意一个元素的字符拼接到res中
                for (int k = 0; k < s.length(); k++) {
                    if (s.charAt(k) == st.charAt(0) || s.charAt(k) == st.charAt(1)) {
                        res += s.charAt(k);
                    }
                }
                // 此处判断 res是否符合要求，
                boolean flag = true;
                for (int k = 0; k < res.length() - 1; k++) {
                    if (res.charAt(k) == res.charAt(k + 1)) {
                        flag = false;
                        break; // 跳出循环
                    }
                }
                if (flag) {
                    return res.length();
                }
            }
        }
        return 0;
    }

    static boolean judge(String s) {
        int len = s.length();
        if (len <= 1 && len > -1) {
            return true;
        }
        String term = s.substring(0, 2);
        String aa = "";
        if (len % 2 == 1) {// 奇数
            if (!s.substring(0, 1).equals(s.substring(len - 1, len))) {
                return false;// 第一个与最后一个不相等
            }
            return judge(s.substring(0, len - 1));// 递归
        } else {
            for (int i = 2; i <= len - 2; i = i + 2) {
                aa = s.substring(i, i + 2);
                if (!term.equals(aa)) {
                    return false;
                }
            }
            return true;
        }
    }
}
