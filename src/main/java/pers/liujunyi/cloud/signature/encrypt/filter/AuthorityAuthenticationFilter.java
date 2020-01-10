package pers.liujunyi.cloud.signature.encrypt.filter;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import pers.liujunyi.cloud.signature.exception.ErrorCodeEnum;
import pers.liujunyi.cloud.signature.restful.ResultInfo;
import pers.liujunyi.cloud.signature.util.DateTimeUtils;
import pers.liujunyi.cloud.signature.util.JsonUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * API权限认证过滤器
 * @author ljy
 */
@Log4j2
@Component
public class AuthorityAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${data.security.antMatchers}")
    private String excludeAntMatchers;
    @Value("${data.security.user-authorization-uri}")
    private String authorizationUrl;
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
            requestUrl = requestUrl.substring(requestUrl.indexOf("/", 2));
            HttpHeaders headers = httpServletRequest.getHeaders();
            String accessToken = httpServletRequest.getQueryParams().getFirst("access_token");
            String headerToken = headers.getFirst(HEADER_AUTHORIZATION);
            if (StringUtils.isNotBlank(headerToken) && StringUtils.isBlank(accessToken)) {
                accessToken = headerToken.indexOf("bearer") != -1 ? headerToken.split(" ")[1] : "";
            }
            AtomicBoolean examine = new AtomicBoolean(true);
            // 不需要进行权限校验的url
            String[] antMatchers = excludeAntMatchers.trim().split(",");
            for (String matchers : antMatchers) {
               boolean through = requestMatcher.match(matchers.trim(), requestUrl);
                if (through) {
                    examine.set(false);
                    break;
                }
            }

            if (examine.get()) {
                // 验证是否拥有API访问权限
                String getUrl = authorizationUrl + "api/v1/ignore/authority/authentication?token={token}&requestUrl={requestUrl}";
                Map<String, String> params = new ConcurrentHashMap<>();
                params.put("token", accessToken);
                params.put("requestUrl", requestUrl);
                ResultInfo resultInfo = this.restTemplate.getForObject(getUrl, ResultInfo.class, params);
                boolean isAuthenticated = resultInfo.getSuccess();
                if (!isAuthenticated) {
                    log.info(">> 请求:" + requestUrl + " 当前用户【" + accessToken + "】 无访问权限.");
                    ResultInfo result = new ResultInfo();
                    result.setTimestamp(DateTimeUtils.getCurrentDateTimeAsString());
                    result.setSuccess(false);
                    result.setStatus(ErrorCodeEnum.AUTHORITY.getCode());
                    result.setMessage(ErrorCodeEnum.AUTHORITY.getMessage());
                    byte[] datas = JsonUtils.toJson(result).getBytes(StandardCharsets.UTF_8);
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
