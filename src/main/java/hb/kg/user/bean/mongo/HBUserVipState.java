package hb.kg.user.bean.mongo;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户的VIP状态，这个类每天凌晨统一做一次计算，通过计算用户发起的VIP业务订单的生效时间和结束时间来给用户设置VIP的权限
 */
@ApiModel(description = "用户的VIP状态")
@Data
@EqualsAndHashCode(callSuper = false)
public class HBUserVipState {
    @ApiModelProperty(value = "VIP等级")
    private String level; // VIP等级
    @ApiModelProperty(value = "开始时间")
    private Date startDay; // 开始时间
    @ApiModelProperty(value = "还有几天过期")
    private Integer expireDays; // 还有几天过期
}
