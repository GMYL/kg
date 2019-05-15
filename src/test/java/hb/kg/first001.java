package hb.kg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class first001 {
    public static void main(String[] arges) {
        // 输入总金额
        Scanner scTotal = new Scanner(System.in);
        // 空格分割的单价
        Scanner scArr = new Scanner(System.in);
        Integer total = scTotal.nextInt();;
        String arr = scArr.nextLine();
        // 商品的单价list
        List<Integer> priceList = new ArrayList<>();
        String strArr[] = arr.split("\\s");
        for (String str : strArr) {
            // 转成Integer
            Integer price = Integer.parseInt(str);
            priceList.add(price);
        }
        // 将输入的价格进行排序默认升序
        Collections.sort(priceList);
        // 降序
        // Collections.sort(priceList, new Comparator<Integer>() {
        // @Override
        // public int compare(Integer o1,
        // Integer o2) {
        // return o2.compareTo(o1);
        // }
        // });
        // 最后
        Integer allPrice = 0;
        for (Integer price : priceList) {
            if (total >= price) {
                total = total - price;
                allPrice += price;
            } else {
                break;
            }
        }
        System.out.println(allPrice);
    }
}
