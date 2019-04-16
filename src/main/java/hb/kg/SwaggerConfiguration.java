package hb.kg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import hb.kg.common.service.SysConfService;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ConditionalOnProperty(prefix = "swagger", value = { "enable" }, havingValue = "true")
@EnableSwagger2
@DependsOn("sysConfService")
public class SwaggerConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SwaggerConfiguration.class);
    @Autowired
    private SysConfService sysConfService;

    /**
     * 配置swagger名称和扫描包路径
     */
    @Bean
    public Docket createRestApi() {
        String swaggerLocation = "http://" + sysConfService.getServerHost() + ":"
                + sysConfService.getServerPort() + "/" + sysConfService.getApiPrefixName() + "/"
                + sysConfService.getApiVersion() + "/";
        // String swaggerLocation = "http://localhost:18090/kg/v1/";
        logger.info("初始化swagger服务，监听在：" + swaggerLocation);
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        ApiInfoBuilder builder = new ApiInfoBuilder();
        return docket.apiInfo(builder.title("花瓣儿网平高后台测试swagger")
                                     .description("提供测试接口使用")
                                     .termsOfServiceUrl(swaggerLocation)
                                     .version("1.0")
                                     .build())
                     .select()
                     .apis(RequestHandlerSelectors.basePackage("hb.kg"))
                     .paths(PathSelectors.any())
                     .build();
    }
}
