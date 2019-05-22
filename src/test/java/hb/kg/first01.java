package hb.kg;

import java.text.DecimalFormat;
import java.util.Scanner;

public class first01 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        DecimalFormat df = new DecimalFormat("0");// 保留几位小数
        System.out.print(a + "平方根是：" + df.format(SQR(a)));
    }

    public static double SQR(int a) {
        double x1 = 1, x2;
        x2 = x1 / 2.0 + a / (2 * x1);// 牛顿迭代公式
        while (ABS(x2 - x1) > 1e-4) {
            x1 = x2;
            x2 = x1 / 2.0 + a / (2 * x1);
        }
        return x2;
    }

    public static double ABS(double a) {
        return (a <= 0.0D) ? 0.0D - a : a;
    }
}
