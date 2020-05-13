package pers.liujunyi.cloud.signature.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/***
 * 文件名称: IgnoreSecurityConfig
 * 文件描述: 
 * 公 司:
 * 内容摘要:
 * 其他说明:
 * 完成日期:2020/5/13 9:52
 * 修改记录:
 * @version 1.0
 * @author ljy
 */
@Data
@Component
@ConfigurationProperties(prefix="data.security.ignore")
public class IgnoreSecurityConfig {

    private List<String> antMatchers = new ArrayList<>();
}
