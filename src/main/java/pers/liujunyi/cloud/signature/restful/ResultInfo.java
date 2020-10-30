package pers.liujunyi.cloud.signature.restful;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pers.liujunyi.cloud.signature.exception.ErrorCodeEnum;

import java.io.Serializable;

/***
 * 返回信息
 * @author ljy
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ResultInfo implements Serializable {

    private static final long serialVersionUID = 3885241278599823865L;
    /** 状态码 */
    private Integer status;
    /** 消息 */
    private String message;
    /** 数据项 */
    private Object data;
    /**  扩展数据 */
    private Object extend;
    /** 时间 */
    private String timestamp;
    /**  是否处理成功 */
    private Boolean success = true;
    /** 总记录条数 */
    private Long total;
    private String description;

    public ResultInfo(){

    }

    public ResultInfo(ErrorCodeEnum  errorCodeEnum){
        this.status = errorCodeEnum.getCode();
        this.message = errorCodeEnum.getMessage();
    }

    public ResultInfo(ErrorCodeEnum  errorCodeEnum, Object data, Object extend){
        this.status = errorCodeEnum.getCode();
        this.message = errorCodeEnum.getMessage();
        this.data = data;
        this.extend = extend;
    }

    public ResultInfo(ErrorCodeEnum  errorCodeEnum, Object data){
        this.status = errorCodeEnum.getCode();
        this.message = errorCodeEnum.getMessage();
        this.data = data;
    }
}
