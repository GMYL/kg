//package hb.kg.common.server;
//
//import java.net.InetAddress;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import javax.annotation.PostConstruct;
//
//import org.apache.commons.lang3.StringUtils;
//import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
//import org.elasticsearch.client.Requests;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.transport.client.PreBuiltTransportClient;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import hb.kg.common.service.BaseService;
//import hb.kg.common.util.http.HttpClientUtil;
//import hb.kg.common.util.json.JSONObject;
//import hb.kg.common.util.set.HBCollectionUtil;
//import hb.kg.common.util.set.HBStringUtil;
//import lombok.Getter;
//
///**
// * 在之前的版本中ES的连接用静态的方式实现的客户端单一化，但这样对后期的优化过程很不好
// */
//@Getter
//@Service
//public class EsClientServer extends BaseService {
//    private TransportClient client;
//    private AtomicBoolean isShutdown = new AtomicBoolean(false);
//
//    @PostConstruct
//    public void init() {
//        if (sysConfService.getSwitchOnlineEs()) {
//            Settings settings = Settings.builder()
//                                        .put("client.transport.sniff",
//                                             sysConfService.getEsSniffOpen())
//                                        .put("cluster.name", sysConfService.getEsClusterName())
//                                        .put("processors", 1)
//                                        .build();
//            client = new PreBuiltTransportClient(settings);
//            String[] machines = sysConfService.getEsClusterMachines().trim().split(",");
//            for (String mc : machines) {
//                try {
//                    String[] ip_port = mc.trim().split(":");
//                    if (HBStringUtil.isBlank(machineOne)) {
//                        machineOne = ip_port[0];
//                    }
//                    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip_port[0]),
//                                                                              Integer.parseInt(ip_port[1])));
//                } catch (Exception e) {
//                    logger.error("es machine[" + mc + "] error", e);
//                }
//            }
//            freshHealth();
//            logger.info("根据配置文件，ES 已经成功启动，当前连接的ES服务器有" + machines.length + "个:"
//                    + sysConfService.getEsClusterMachines());
//        } else {
//            logger.info("根据配置文件，ES 没有启动");
//        }
//    }
//
//    public List<String> getMachines() {
//        return HBCollectionUtil.stringToList(sysConfService.getEsClusterMachines());
//    }
//
//    /**
//     * 关闭ES引擎连接，当前采用的策略是只允许关闭一次，且关闭后不能再次打开
//     */
//    public void shutdown() {
//        if (!isShutdown.getAndSet(true)) {
//            logger.info("开始关闭es引擎连接");
//            client.close();
//        }
//    }
//
//    public void dropIndex(String index) {
//        DeleteIndexRequest deleting = Requests.deleteIndexRequest(index);
//        client.admin().indices().delete(deleting);
//    }
//
//    private String machineOne;
//    private JSONObject health;
//
//    /**
//     * 每5分钟更新一下ES引擎的健康情况
//     */
//    @Scheduled(cron = "0 */5 * * * *")
//    public void freshHealth() {
//        if (sysConfService.getSwitchOnlineEs()) {
//            String healthState = HttpClientUtil.httkgetRequest("http://" + machineOne
//                    + ":9200/_cluster/health?level=shards");
//            if (StringUtils.isNotEmpty(healthState)) {
//                health = JSONObject.parseObject(healthState);
//            }
//        }
//    }
//
//    public JSONObject getHealth() {
//        return health;
//    }
//}
