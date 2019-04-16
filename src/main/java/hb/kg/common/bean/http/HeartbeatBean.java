package hb.kg.common.bean.http;

import java.io.Serializable;

import lombok.Data;

@Data
public class HeartbeatBean implements Serializable {
    private static final long serialVersionUID = -1631034206651818011L;
    private long lastHeartBeatTime;
}
