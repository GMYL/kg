package hb.kg;

//第二题
public class second {
    public static void main(String[] args) {
        int p = superDigit("9875", 1);
        System.out.println(p);
    }

    static int superDigit(String n,
                          int k) {
        String str = "";
        for (int i = 1; i <= k; i++) {
            str = str + n;// 字符串拼接
        }
        Long sum = Long.parseLong(str);//转成long
        while (sum >= 10) {
            sum = fn(sum);//递归求和
        }
        return sum.intValue();
    }

    public static Long fn(Long n) {// 递归
        if (n == 0)
            return (long) 0;
        else
            return n % 10 + fn(n / 10);
    }
}
