package hb.kg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

public class MySimHash {
    private String tokens; // 字符串
    private BigInteger strSimHash;// 字符产的hash值
    private int hashbits = 64; // 分词后的hash数;

    public MySimHash(String tokens) {
        this.tokens = tokens;
        this.strSimHash = this.simHash();
    }

    private MySimHash(String tokens,
                      int hashbits) {
        this.tokens = tokens;
        this.hashbits = hashbits;
        this.strSimHash = this.simHash();
    }

    /**
     * 这个是对整个字符串进行hash计算
     * @return
     */
    private BigInteger simHash() {
        int[] v = new int[this.hashbits];
        List<Term> termList = StandardTokenizer.segment(this.tokens); // 对字符串进行分词
        // 对分词的一些特殊处理 : 比如: 根据词性添加权重 , 过滤掉标点符号 , 过滤超频词汇等;
        Map<String, Integer> weightOfNature = new HashMap<String, Integer>(); // 词性的权重
        weightOfNature.put("n", 2); // 给名词的权重是2;
        Map<String, String> stopNatures = new HashMap<String, String>();// 停用的词性 如一些标点符号之类的;
        stopNatures.put("w", ""); //
        int overCount = 5; // 设定超频词汇的界限 ;
        Map<String, Integer> wordCount = new HashMap<String, Integer>();
        for (Term term : termList) {
            String word = term.word; // 分词字符串
            String nature = term.nature.toString(); // 分词属性;
            // 过滤超频词
            if (wordCount.containsKey(word)) {
                int count = wordCount.get(word);
                if (count > overCount) {
                    continue;
                }
                wordCount.put(word, count + 1);
            } else {
                wordCount.put(word, 1);
            }
            // 过滤停用词性
            if (stopNatures.containsKey(nature)) {
                continue;
            }
            // 2、将每一个分词hash为一组固定长度的数列.比如 64bit 的一个整数.
            BigInteger t = this.hash(word);
            for (int i = 0; i < this.hashbits; i++) {
                BigInteger bitmask = new BigInteger("1").shiftLeft(i);
                // 3、建立一个长度为64的整数数组(假设要生成64位的数字指纹,也可以是其它数字),
                // 对每一个分词hash后的数列进行判断,如果是1000...1,那么数组的第一位和末尾一位加1,
                // 中间的62位减一,也就是说,逢1加1,逢0减1.一直到把所有的分词hash数列全部判断完毕.
                int weight = 1; // 添加权重
                if (weightOfNature.containsKey(nature)) {
                    weight = weightOfNature.get(nature);
                }
                if (t.and(bitmask).signum() != 0) {
                    // 这里是计算整个文档的所有特征的向量和
                    v[i] += weight;
                } else {
                    v[i] -= weight;
                }
            }
        }
        BigInteger fingerprint = new BigInteger("0");
        for (int i = 0; i < this.hashbits; i++) {
            if (v[i] >= 0) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
            }
        }
        return fingerprint;
    }

    /**
     * 对单个的分词进行hash计算;
     * @param source
     * @return
     */
    private BigInteger hash(String source) {
        if (source == null || source.length() == 0) {
            return new BigInteger("0");
        } else {
            /**
             * 当sourece 的长度过短，会导致hash算法失效，因此需要对过短的词补偿
             */
            while (source.length() < 3) {
                source = source + source.charAt(0);
            }
            char[] sourceArray = source.toCharArray();
            BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
            BigInteger m = new BigInteger("1000003");
            BigInteger mask = new BigInteger("2").pow(this.hashbits).subtract(new BigInteger("1"));
            for (char item : sourceArray) {
                BigInteger temp = BigInteger.valueOf((long) item);
                x = x.multiply(m).xor(temp).and(mask);
            }
            x = x.xor(new BigInteger(String.valueOf(source.length())));
            if (x.equals(new BigInteger("-1"))) {
                x = new BigInteger("-2");
            }
            return x;
        }
    }

    /**
     * 计算海明距离,海明距离越小说明越相似;
     * @param other
     * @return
     */
    private int hammingDistance(MySimHash other) {
        BigInteger m = new BigInteger("1").shiftLeft(this.hashbits).subtract(new BigInteger("1"));
        BigInteger x = this.strSimHash.xor(other.strSimHash).and(m);
        int tot = 0;
        while (x.signum() != 0) {
            tot += 1;
            x = x.and(x.subtract(new BigInteger("1")));
        }
        return tot;
    }

    public double getSemblance(MySimHash s2) {
        double i = (double) this.hammingDistance(s2);
        return 1 - i / this.hashbits;
    }

    /**
     * 读取txt文件的内容
     * @param file 想要读取的文件对象
     * @return 返回文件内容
     */
    public static String txt2String(File file) {
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));// 构造一个BufferedReader类来读取文件
            String s = null;
            while ((s = br.readLine()) != null) {// 使用readLine方法，一次读一行
                result.append(System.lineSeparator() + s);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static void main(String[] args) {
        File file = new File("E:/test/g.txt");
        File file2 = new File("E:/test/y.txt");
        String s1 = txt2String(file);
        String s2 = txt2String(file2);
        MySimHash hash1 = new MySimHash(s1, 64);
        MySimHash hash2 = new MySimHash(s2, 64);
        System.out.println("======================================");
        System.out.println("距离为:" + hash1.hammingDistance(hash2));
        System.out.println("相似度为:" + hash1.getSemblance(hash2));
        System.out.println("======================================");
    }
}
