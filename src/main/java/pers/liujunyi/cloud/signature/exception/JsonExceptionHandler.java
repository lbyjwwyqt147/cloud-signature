package pers.liujunyi.cloud.signature.exception;


import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.server.*;
import pers.liujunyi.cloud.signature.util.DateTimeUtils;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一异常处理
 *
 * @author ljy
 *
 */
@Log4j2
public class JsonExceptionHandler extends DefaultErrorWebExceptionHandler {


    public JsonExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    /**
     * 获取异常属性
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        int code = 500;
        Throwable error = super.getError(request);
        if (error instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            code = 404;
        } else if (error instanceof BindException) {
            code = ErrorCodeEnum.PARAMS.getCode();
        } else if (error instanceof MethodArgumentNotValidException) {
            code = ErrorCodeEnum.PARAMS.getCode();
        } else if (error instanceof ValidationException) {
            code = HttpStatus.BAD_REQUEST.value();
        } else if (error instanceof HttpMessageNotReadableException) {
            code = HttpStatus.BAD_REQUEST.value();
        }
        return response(code, this.buildMessage(request, error));
    }

    /**
     * 指定响应处理方法为JSON处理的方法
     *
     * @param errorAttributes
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }


    /**
     * 构建异常信息
     *
     * @param request
     * @param ex
     * @return
     */
    private String buildMessage(ServerRequest request, Throwable ex) {
        StringBuilder message = new StringBuilder("Failed to handle request [");
        message.append(request.methodName());
        message.append(" ");
        message.append(request.uri());
        message.append("]");
        if (ex != null) {
            message.append(": ");
            message.append(ex.getMessage());
        }
        String msg = message.toString();
        log.info("异常描述：" + message.toString());
        return msg;
    }

    /**
     * 构建返回的JSON数据格式
     *
     * @param status       状态码
     * @param errorMessage 异常信息
     * @return
     */
    public static Map<String, Object> response(int status, String errorMessage) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        map.put("message", errorMessage);
        map.put("data", null);
        map.put("timestamp", DateTimeUtils.getCurrentDateAsString());
        map.put("success", false);
        map.put("total", null);
        return map;
    }
}
