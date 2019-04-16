package hb.kg.common.server.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 只处理json格式的数据，传入为string在这里转换成json
 * @FIXME 改成被register的方式
 */
@Service
@ChannelHandler.Sharable
public class HBNettyChannelHandler extends ChannelInboundHandlerAdapter {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${run.on.dev:true}")
    private boolean runOnDev;
    @Value("${netty.test.token:HalfIsWorseThanNoneAtAll}")
    private String nettyTestToken;
//    @Autowired
//    private AgentService agentService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }
//
//    public void channelRead(ChannelHandlerContext ctx,
//                            Object msg)
//            throws Exception {
//        String content = null;
//        Object result = "NA";
//        try {
//            if (msg instanceof HttpContent) {
//                HttpContent msgContent = (HttpContent) msg;
//                ByteBuf buf = msgContent.content();
//                content = buf.toString(CharsetUtil.UTF_8);
//                buf.release();
//            } else if (msg instanceof HttpRequest) {
//                HttpRequest request = (HttpRequest) msg;
//                content = request.decoderResult().toString();
//            }
//            if (StringUtils.isNotBlank(content)) {
//                if (HBStringUtil.checkIsStringAJson(content)) {
//                    JSONObject jobj = JSONObject.parseObject(content);
//                    String platid = jobj.getString("platid");
//                    String token = jobj.getString("token");
//                    String type = jobj.getString("type");
//                    JSONObject data = jobj.getJSONObject("data");
//                    if (StringUtils.isNotBlank(platid) && StringUtils.isNotBlank(token)
//                            && StringUtils.isNotBlank(type) && data != null) {
//                        if (checkToken(platid, data.toJSONString(), token)) {
//                            if (checkPermission(platid, type)) {
////                                result = agentService.solveDataByType(type, data);
//                            } else {
//                                result = "{\"code\": \"" + ApiCode.NO_AUTH.getCode()
//                                        + "\",\"errMsg\": \"没有访问该模块的权限\"}";
//                            }
//                        } else {
//                            result = "{\"code\": \"" + ApiCode.NO_AUTH.getCode()
//                                    + "\",\"errMsg\": \"签名校验错误\"}";
//                        }
//                    } else {
//                        result = "{\"code\": \"" + ApiCode.PARAM_CONTENT_ERROR.getCode()
//                                + "\",\"errMsg\": \"传递的字段不全\"}";
//                    }
//                } else {
//                    return; // 如果不是json，可能是OPTION请求，那么直接返回
//                }
//            }
//        } catch (Exception e) {
//            logger.error("解析netty的msg出错：" + msg, e);
//        }
//        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
//                                                                HttpResponseStatus.OK,
//                                                                Unpooled.wrappedBuffer(result.toString()
//                                                                                             .getBytes()));
//        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
//        response.headers()
//                .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
//        ctx.write(response).addListener(ChannelFutureListener.CLOSE);
//    }

//    /**
//     * 检查对方平台有没有访问该模块的权限
//     */
//    private boolean checkPermission(String platid,
//                                    String type) {
//        return agentService.checkPlatPermission(platid, type);
//    }
//
//    /**
//     * 解析拿到的token
//     */
//    public boolean checkToken(String id,
//                              String content,
//                              String token) {
//        try {
//            if (runOnDev) {
//                HBAgent agent = agentService.findOne(id);
//                if (agent != null && StringUtils.isNoneBlank(agent.getPublicKey())) {
//                    // token = HBSecurityUtil.signRSA(id, agent.getPrivateKey());
//                    return HBSecurityUtil.verifyRSA(id, agent.getPublicKey(), token);
//                } else {
//                    return false;
//                }
//            } else {
//                return nettyTestToken.equals(token);
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return false;
//        }
//    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
