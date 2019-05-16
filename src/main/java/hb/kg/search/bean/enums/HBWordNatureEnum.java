package hb.kg.search.bean.enums;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Getter;

/**
 * 词性枚举
 */
@Getter
public enum HBWordNatureEnum {
    N(1, "n", "n", "名词"),
    NR(2, "nr", "n", "人名"),
    NR1(3, "nr1", "n", "汉语姓氏"),
    NR2(4, "nr2", "n", "汉语名字"),
    NRJ(5, "nrj", "n", "日语人名"),
    NRF(6, "nrf", "n", "音译人名"),
    NS(7, "ns", "n", "地名"),
    NSF(8, "nsf", "n", "音译地名"),
    NT(9, "nt", "n", "机构团体名"),
    NZ(10, "nz", "n", "其它专名"),
    NL(11, "nl", "n", "名词性惯用语"),
    NG(12, "ng", "n", "名词性语素"),
    NW(13, "nw", "n", "新词"),
    T(14, "t", "t", "时间词"),
    TG(15, "tg", "t", "时间词性语素"),
    S(16, "s", "s", "处所词"),
    F(17, "f", "f", "方位词"),
    V(18, "v", "v", "动词"),
    VD(19, "vd", "v", "副动词"),
    VN(20, "vn", "v", "名动词"),
    VSHI(21, "vshi", "v", "是"),
    VYOU(22, "vyou", "v", "有"),
    VF(23, "vf", "v", "趋向动词"),
    VX(24, "vx", "v", "形式动词"),
    VI(25, "vi", "v", "不及物动词"),
    VL(26, "vl", "v", "动词性惯用语"),
    VG(27, "vg", "v", "动词性语素"),
    A(28, "a", "a", "形容词"),
    AD(29, "ad", "a", "副形词"),
    AN(30, "an", "a", "名形词"),
    AG(31, "ag", "a", "形容词性语素"),
    AL(32, "al", "a", "形容词性惯用语"),
    B(33, "b", "b", "区别词"),
    BL(34, "bl", "b", "区别词性惯用语"),
    Z(35, "z", "z", "状态词"),
    R(36, "r", "r", "代词"),
    RR(37, "rr", "r", "人称代词"),
    RZ(38, "rz", "r", "指示代词"),
    RZT(39, "rzt", "r", "时间指示代词"),
    RZS(40, "rzs", "r", "处所指示代词"),
    RZV(41, "rzv", "r", "谓词性指示代词"),
    RY(42, "ry", "r", "疑问代词"),
    RYT(43, "ryt", "r", "时间疑问代词"),
    RYS(44, "rys", "r", "处所疑问代词"),
    RYV(45, "ryv", "r", "谓词性疑问代词"),
    RG(46, "rg", "r", "代词性语素"),
    M(47, "m", "m", "数词"),
    MQ(48, "mq", "m", "数量词"),
    Q(49, "q", "q", "量词"),
    QV(50, "qv", "q", "动量词"),
    QT(51, "qt", "q", "时量词"),
    D(52, "d", "d", "副词"),
    P(53, "p", "p", "介词"),
    PBA(54, "pba", "p", "把"),
    PBEI(55, "pbei", "p", "被"),
    C(56, "c", "c", "连词"),
    CC(57, "cc", "c", "并列连词"),
    U(58, "u", "u", "助词"),
    UZHE(59, "uzhe", "u", "着"),
    ULE(60, "ule", "u", "了"),
    UGUO(61, "uguo", "u", "过"),
    UDE1(62, "ude1", "u", "的"),
    UDE2(63, "ude2", "u", "地"),
    UDE3(64, "ude3", "u", "得"),
    USUO(65, "usuo", "u", "所"),
    UDENG(66, "udeng", "u", "等"),
    UYY(67, "uyy", "u", "一样"),
    UDH(68, "udh", "u", "的话"),
    ULS(69, "uls", "u", "来讲"),
    UZHI(70, "uzhi", "u", "之"),
    ULIAN(71, "ulian", "u", "连"),
    E(72, "e", "e", "叹词"),
    Y(73, "y", "y", "语气词"),
    O(74, "o", "o", "拟声词"),
    H(75, "h", "h", "前缀"),
    K(76, "k", "k", "后缀"),
    X(77, "x", "x", "字符串"),
    XX(78, "xx", "x", "非语素字"),
    XU(79, "xu", "x", "URL"),
    W(80, "w", "w", "标点符号"),
    WKZ(81, "wkz", "w", "左括号"),
    WKY(82, "wky", "w", "右括号"),
    WYZ(83, "wyz", "w", "左引号"),
    WYY(84, "wyy", "w", "右引号"),
    WJ(85, "wj", "w", "句号"),
    WW(86, "ww", "w", "问号"),
    WT(87, "wt", "w", "叹号"),
    WD(88, "wd", "w", "逗号"),
    WF(89, "wf", "w", "分号"),
    WN(90, "wn", "w", "顿号"),
    WM(91, "wm", "w", "冒号"),
    WS(92, "ws", "w", "省略号"),
    WP(93, "wp", "w", "破折号"),
    WB(94, "wb", "w", "百分号"),
    WH(95, "wh", "w", "单位符号"),
    NO(96, "no", "no", "无词性");// 为找到相应词性
    private Integer index;
    private String name;
    private String type; // 类型
    private String explain; // 说明

    private HBWordNatureEnum(Integer index,
                             String name,
                             String type,
                             String explain) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.explain = explain;
    }

    /**
     * 为了避免内部操作对元数据的影响，这里返回一个新的集合
     */
    public static Collection<String> getAllByType(String type) {
        Collection<String> result = new ArrayList<>();
        for (HBWordNatureEnum serviceEnum : values()) {
            if (serviceEnum.getType().equals(type)) {
                result.add(serviceEnum.getName());
            }
        }
        return result;
    }

    public static HBWordNatureEnum valueOf(int index) {
        for (HBWordNatureEnum serviceEnum : values()) {
            if (serviceEnum.getIndex() == index) {
                return serviceEnum;
            }
        }
        return null;
    }
}
/**
 * 现在库中出现的所有类型有如下这些： "mq", "nrf", "nz", "n", "m", "i", "nr", "v", "nsf", "l",
 * "ns", "vi", "nt", "gb", "nto", "gi", "a", "d", "gm", "gg", "gc", "j", "nis",
 * "vl", "vn", "z", "ntu", "nmc", "nts", "ntc", "nnt", "nhd", "al", "nf", "q",
 * "t", "gp", "b", "nit", "nnd", "nrt", "vd", "r", "p", "f", "c", "ntch", "uls",
 * "nba", "nth", "s", "nbc", "bl", "an", "nhm", "nrfg", "o", "rr", "nrj", "u",
 * "dl", "ntcf", "ncs", "ad", "ntcb", "y", "rzt", "rz", "vf", "h", "ryv", "rzs",
 * "qt", "vx", "udeng", "rzv", "ry", "uyy", "cc", "qv", "rys", "ryt", "e", "vq",
 * "œa", "na", "nr2", "nr1", "udh", "jn"
 */