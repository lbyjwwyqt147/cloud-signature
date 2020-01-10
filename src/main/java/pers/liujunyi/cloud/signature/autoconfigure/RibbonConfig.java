package pers.liujunyi.cloud.signature.autoconfigure;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author ljy
 */
@Configuration
public class RibbonConfig {

    /**
     * 添加 @LoadBlanced 注解，使得 RestTemplate 接入 Ribbon，使得利用restTemplate能否实现负载均衡
     * @return
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
