package hb.kg;

//第一题
import java.util.Scanner;

public class first {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Long str = sc.nextLong();// 输入的time
        System.out.println("" + strangeCounter(str));
    }

    static long strangeCounter(long t) {
        long value = 4;
        long m = 3;// 每次从m倒数
        for (long i = 1; i <= t; i++) {
            if (value == 1) {
                m = m * 2;
                value = m + 1;
            }
            value--;
        }
        return value;
    }
}
