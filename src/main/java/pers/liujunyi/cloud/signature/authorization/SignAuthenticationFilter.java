package pers.liujunyi.cloud.signature.authorization;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import pers.liujunyi.cloud.signature.autoconfigure.SignatureSecurityConfig;
import pers.liujunyi.cloud.signature.encrypt.AesEncryptUtils;
import pers.liujunyi.cloud.signature.exception.ErrorCodeEnum;
import pers.liujunyi.cloud.signature.restful.ResultInfo;
import pers.liujunyi.cloud.signature.util.DateTimeUtils;
import pers.liujunyi.cloud.signature.util.JsonUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 请求签名验证过滤器
 * 
 * 请求头中获取sign进行校验，判断合法性和是否过期<br>
 * 
 * sign=加密({参数：值, 参数2：值2, signTime:签名时间戳})
 * @author ljy
 *
 *
 */
@Log4j2
@Component
public class SignAuthenticationFilter implements GlobalFilter, Ordered {

	/** sign 过期时间 */
	private Integer signExpireTime = 60000;

	/** 需要校验签名信息资源 */
	@Autowired
	private SignatureSecurityConfig signatureSecurityConfig;

	@Autowired
	private SignInfo signObj;

	@Override
	public int getOrder() {
		return -999;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest httpServletRequest = exchange.getRequest();
		ServerHttpResponse httpServletResponse = exchange.getResponse();
		// 如果是OPTIONS则结束请求
		if (HttpMethod.OPTIONS.toString().equals(httpServletRequest.getMethod())) {
			exchange.getResponse().setStatusCode(HttpStatus.NO_CONTENT);
			return chain.filter(exchange);
		}
        HttpHeaders headers = httpServletRequest.getHeaders();
		String requestUrl = httpServletRequest.getURI().getRawPath();
		log.info(">> 数字签名校验开始...............");
		log.info(">> HttpMethod:{}, Url:{}", httpServletRequest.getMethod(), requestUrl);
        // 不需要进行签名校验的url
		AtomicBoolean through = new AtomicBoolean(false);
		PathMatcher requestMatcher = new AntPathMatcher();
		List<String> antMatchers = signatureSecurityConfig.getAntMatchers();
		for (String matchers : antMatchers) {
			through.set(requestMatcher.matchStart(matchers.trim(), requestUrl));
            if (through.get()) {
                break;
            }
        }
		if (!through.get()) {
			log.info(">> 请求:" + requestUrl + " 不进行签名校验....");
			return chain.filter(exchange);
		}
		AtomicBoolean pass = new AtomicBoolean(true);
		ResultInfo result = new ResultInfo();
		result.setTimestamp(DateTimeUtils.getCurrentDateTimeAsString());
		result.setSuccess(false);
		result.setStatus(ErrorCodeEnum.SIGN_INVALID.getCode());
		result.setMessage("非法请求：数字签名错误.");
		String sign = headers.getFirst("sign");
		if (!StringUtils.hasText(sign)) {
			log.info(" >> 非法请求: " + requestUrl + " 缺少签名信息");
			pass.set(false);
		}
		if (pass.get()) {
			try {
				String decryptBody = AesEncryptUtils.aesDecrypt(sign, signObj.getSecretKey().trim());
				Map<String, Object> signInfo = JsonUtils.getMapper().readValue(decryptBody, Map.class);
				Long curSignTime = (Long) signInfo.get("signTime");
				String curSecret = (String) signInfo.get("secret");
				boolean validateParameter = (Boolean) signInfo.get("parameter");
				if (!curSecret.equals(signObj.getSecretKey().trim())) {
					log.info(" >> 非法请求: " + requestUrl + " 签名信息不正确");
					pass.set(false);
				}
				if (pass.get()) {
					// 签名时间和服务器时间相差10分钟以上则认为是过期请求，此时间可以配置
					if ((System.currentTimeMillis() - curSignTime) > signObj.getSignExpireTime() * this.signExpireTime) {
						log.info(" >> 非法请求:" + requestUrl + " 请求已过期");
						result.setStatus(ErrorCodeEnum.SIGN_TIME_OUT.getCode());
						result.setMessage("非法请求：请求已过期.");
						pass.set(false);
					}
				}
				if (pass.get()) {
					// POST请求只处理时间
					// GET请求处理参数和时间(参数信息需要在签名信息中才行)
					if(validateParameter && httpServletRequest.getMethod().equals(HttpMethod.GET.name())) {
						Set<String> paramsSet = signInfo.keySet();
						for (String key : paramsSet) {
							if (!"signTime".equals(key)) {
								String signValue = signInfo.get(key).toString();
								String reqValue = httpServletRequest.getQueryParams().getFirst(key);
								//签名信息中的参数和请求参数进行比较 看是否一至
								if (!signValue.equals(reqValue)) {
									log.info(" >> 非法请求:" + requestUrl + " 参数被篡改");
									result.setStatus(ErrorCodeEnum.SIGN_INVALID.getCode());
									result.setMessage("非法请求：参数被篡改.");
									pass.set(false);
								}
							}
						}
					}
				}
				log.info(" >> 请求: " + requestUrl +" 签名校验通过.  ");
			} catch (Exception e) {
				log.info(" >> 非法请求:" + requestUrl + " 签名校验错误.");
				e.printStackTrace();
			}
		}
		if (!pass.get()) {
			byte[] datas = JsonUtils.toJson(result).getBytes(StandardCharsets.UTF_8);
			DataBuffer buffer = httpServletResponse.bufferFactory().wrap(datas);
			httpServletResponse.setStatusCode(HttpStatus.OK);
			httpServletResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
			return httpServletResponse.writeWith(Mono.just(buffer));
		}
		return chain.filter(exchange);
	}

}
