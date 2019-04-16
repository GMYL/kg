package hb.kg.system.bean.http;

import lombok.Data;

@Data
public class HBBackUpFile {
    private String filename; // 备份文件名
    private long lastModifyTime; // 备份文件最后修改日期
    private String loc; // 备份文件路径
    private String size; // 备份文件大小
}
