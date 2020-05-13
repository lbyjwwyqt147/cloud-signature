package pers.liujunyi.cloud.signature.authorization;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/***
 * 签名信息
 * @author ljy
 */
@Data
@Component
public class SignInfo {

    @Value("${spring.encrypt.signExpireTime}")
    private Integer signExpireTime;

    @Value("${spring.encrypt.secretKey}")
    private  String secretKey;

    @Value("${spring.encrypt.privateKey}")
    private  String privateKey;

    @Value("${spring.encrypt.publicKey}")
    private  String publicKey;

}
