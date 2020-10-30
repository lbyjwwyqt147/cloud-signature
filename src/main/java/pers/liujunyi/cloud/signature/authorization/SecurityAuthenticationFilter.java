package pers.liujunyi.cloud.signature.authorization;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import pers.liujunyi.cloud.signature.autoconfigure.IgnoreSecurityConfig;
import pers.liujunyi.cloud.signature.restful.ResultInfo;
import pers.liujunyi.cloud.signature.util.DateTimeUtils;
import pers.liujunyi.cloud.signature.util.JsonUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * API权限认证过滤器  顺序 先进行接口签名校验，然后再对接口权限校验
 * @author ljy
 */
@Log4j2
@Component
public class SecurityAuthenticationFilter implements GlobalFilter, Ordered {

    /** 不需要权限认证的资源 */
    @Autowired
    private IgnoreSecurityConfig ignoreSecurityConfig;
    @Value("${data.security.user-authorization-uri}")
    private String authorizationUrl;
    @Value("${security.oauth2.authorization.check-token-access}")
    private String checkTokenUrl;
    @Value("${data.security.authorizationRequestUrl.enabled}")
    private Boolean enabledAuthorizationRequestUrl;
    private static final String HEADER_AUTHORIZATION = "Authorization";
    @Autowired
    @Lazy
    private RestTemplate restTemplate;

    @Override
    public int getOrder() {
        return -800;
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
        String requestUrl = httpServletRequest.getURI().getRawPath();
        PathMatcher requestMatcher = new AntPathMatcher();
        AtomicBoolean checked = new AtomicBoolean(requestUrl.startsWith("/centre"));
        if (!checked.get()) {
            requestUrl = requestUrl.substring(requestUrl.indexOf("/api"));
            AtomicBoolean examine = new AtomicBoolean(true);
            // 不需要进行权限校验的url
            List<String> ignoreAntMatchers = ignoreSecurityConfig.getAntMatchers();
            for (String matchers : ignoreAntMatchers) {
                boolean through = requestMatcher.match(matchers.trim(), requestUrl);
                if (through) {
                    examine.set(false);
                    break;
                }
            }
           // requestUrl = requestUrl.substring(requestUrl.indexOf("/", 2));
            HttpHeaders headers = httpServletRequest.getHeaders();
            String accessToken = httpServletRequest.getQueryParams().getFirst("access_token");
            String headerToken = headers.getFirst(HEADER_AUTHORIZATION);
            if (StringUtils.isNotBlank(headerToken) && StringUtils.isBlank(accessToken)) {
                accessToken = headerToken.indexOf("bearer") != -1 ? headerToken.split(" ")[1] : "";
            }
            if (examine.get()) {
                ResultInfo resultInfo = new ResultInfo();
                resultInfo.setSuccess(true);
                HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
                // 校验token 有效性
                String checkUrl = checkTokenUrl + "?token={token}";
                Map<String, String> tokenParams = new ConcurrentHashMap<>();
                tokenParams.put("token", accessToken);
                ResponseEntity<JSONObject> checkTokenEntity = restTemplate.exchange(checkUrl, HttpMethod.GET, requestEntity, JSONObject.class, tokenParams);
                String checkEntity = JSON.toJSONString(checkTokenEntity.getBody());
                log.info(">> 请求:" + requestUrl + " 当前token【" + accessToken + "】 token合法性校验结果：" + checkEntity);
                JSONObject jsonObject = checkTokenEntity.getBody();
                boolean checkStatus = jsonObject.getBoolean("success") != null ? jsonObject.getBoolean("success") : true;
                if (checkStatus && enabledAuthorizationRequestUrl) {
                    // 验证是否拥有API访问权限； 如果不进行详细API访问权限认证，只对token有效性进行校验，则不需要下面业务代码
                    String getUrl = authorizationUrl + "api/v1/authority/authentication?token={token}&requestUrl={requestUrl}";
                    Map<String, String> params = new ConcurrentHashMap<>();
                    params.put("token", accessToken);
                    params.put("requestUrl", requestUrl);
                    ResponseEntity<ResultInfo> resEntity = restTemplate.exchange(getUrl, HttpMethod.GET, requestEntity, ResultInfo.class, params);
                    resultInfo = resEntity.getBody();
                    boolean isAuthenticated = resultInfo.getSuccess();
                    if (!isAuthenticated) {
                        log.info(">> 请求:" + requestUrl + " 当前用户【" + accessToken + "】 ." + resultInfo.getMessage());
                        resultInfo.setSuccess(false);
                    }
                } else {
                    resultInfo.setSuccess(false);
                    resultInfo.setStatus(HttpStatus.UNAUTHORIZED.value());
                    resultInfo.setMessage("无效的token.");
                }
                if (!resultInfo.getSuccess()) {
                    resultInfo.setTimestamp(DateTimeUtils.getCurrentDateTimeAsString());
                    byte[] datas = JsonUtils.toJson(resultInfo).getBytes(StandardCharsets.UTF_8);
                    DataBuffer buffer = httpServletResponse.bufferFactory().wrap(datas);
                    httpServletResponse.setStatusCode(HttpStatus.OK);
                    httpServletResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
                    return httpServletResponse.writeWith(Mono.just(buffer));
                }
            }
        }
        return chain.filter(exchange);
    }
}
