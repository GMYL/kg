package hb.kg;

import java.util.Scanner;

public class second002 {
    public static void main(String[] args) {
        Scanner sca = new Scanner(System.in);
        Scanner scArr = new Scanner(System.in);
        String date = scArr.nextLine();
        String content = sca.nextLine();
        int month = Integer.parseInt(date.split(" ")[0]);
        int day = Integer.parseInt(date.split(" ")[1]);
        String[] codeTable = { "ABCDEFGHI", "JKLMNOPQR", "STUVWXYZ*" };
        int indexMon = month - 1;
        int indexDay = day - 1;
        char[] charArrOne = codeTable[0].toCharArray();
        char[] charArrTwo = codeTable[1].toCharArray();
        char[] charArrThree = codeTable[2].toCharArray();
        // 因为每一组的里的每个元素都会向左移动(d-1)次，故而先移动组内元素
        charArrOne = toCharArray(charArrOne, indexDay);
        charArrTwo = toCharArray(charArrTwo, indexDay);
        charArrThree = toCharArray(charArrThree, indexDay);
        // 将组内移动完成的元素组成新的数组
        codeTable[0] = new String(charArrOne);
        codeTable[1] = new String(charArrTwo);
        codeTable[2] = new String(charArrThree);
        // 再整体移动每组元素
        codeTable = toStringArray(codeTable, indexMon);
        char[] contentArray = content.toCharArray();
        // 把空格换位*
        for (int i = 0; i < contentArray.length; i++) {
            if (contentArray[i] == ' ') {
                contentArray[i] = '*';
            }
        }
        // 将数组转为一个char数组，循环对比用户输入的内容
        String strs = new String(codeTable[0]) + new String(codeTable[1])
                + new String(codeTable[2]);
        char[] strArr = strs.toCharArray();
        String str = "";
        for (int i = 0; i < contentArray.length; i++) {
            for (int j = 0; j < strArr.length; j++) {
                // 当用户输入的字符等于第一组中元素时，输出1+（j下标0开始到8结束 ，需加1）
                if (contentArray[i] == strArr[j] && j < 9) {
                    str = str + "1" + (j + 1) + " ";
                }
                // 当用户输入的字符等于第二组中元素时，输出2+（j下标9开始到17结束 ，需加1再减9，即j-8）
                if (contentArray[i] == strArr[j] && j < 18 && j >= 9) {
                    str = str + "2" + (j - 8) + " ";
                }
                // 当用户输入的字符等于第三组中元素时，输出3+（j下标19开始到26结束 ，需加1再减18，即j-17）
                if (contentArray[i] == strArr[j] && j < 27 && j >= 18) {
                    str = str + "3" + (j - 17) + " ";
                }
            }
        }
        System.out.println(str);
    }

    public static char[] toCharArray(char[] strArr,
                                     int index) {
        while (index > 0) {
            char temp = strArr[0];
            for (int i = 0; i < strArr.length; i++) {
                if (i < strArr.length - 1) {
                    strArr[i] = strArr[i + 1];
                }
                if (i == strArr.length - 1) {
                    strArr[i] = temp;
                }
            }
            index--;
        }
        return strArr;
    }

    public static String[] toStringArray(String[] strArr,
                                         int index) {
        while (index > 0) {
            String temp = strArr[0];
            for (int i = 0; i < strArr.length; i++) {
                if (i < strArr.length - 1) {
                    strArr[i] = strArr[i + 1];
                }
                if (i == strArr.length - 1) {
                    strArr[i] = temp;
                }
            }
            index--;
        }
        return strArr;
    }
}
