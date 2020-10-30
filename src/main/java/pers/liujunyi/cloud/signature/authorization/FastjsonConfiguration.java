package pers.liujunyi.cloud.signature.authorization;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

/***
 * 文件名称: FastjsonConfiguration
 * 文件描述: 
 * 公 司:
 * 内容摘要:
 * 其他说明:
 * 完成日期:2020/10/30 10:57
 * 修改记录:
 * @version 1.0
 * @author ljy
 */
@Configuration
public class FastjsonConfiguration {

    @Bean
    public HttpMessageConverters fastjsonConverter() {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        //自定义格式化输出
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteNullListAsEmpty, // List类型字段为null时输出[]而非null
                SerializerFeature.WriteMapNullValue, // 显示空字段
                SerializerFeature.WriteNullStringAsEmpty, // 字符串类型字段为null时间输出""而非null
                SerializerFeature.WriteNullBooleanAsFalse, // Boolean类型字段为null时输出false而null
                SerializerFeature.PrettyFormat, // 美化json输出，否则会作为整行输出
                SerializerFeature.WriteNullNumberAsZero, // 数值字段如果为null,输出为0,而非null
                SerializerFeature.WriteNullBooleanAsFalse, // Boolean字段如果为null,输出为false,而非null
                SerializerFeature.WriteDateUseDateFormat, // 时间格式yyyy-MM-dd HH: mm: ss
                SerializerFeature.DisableCircularReferenceDetect);  // 禁用循环引用检测
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        supportedMediaTypes.add(MediaType.APPLICATION_PDF);
        supportedMediaTypes.add(MediaType.APPLICATION_RSS_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XML);
        supportedMediaTypes.add(MediaType.IMAGE_GIF);
        supportedMediaTypes.add(MediaType.IMAGE_JPEG);
        supportedMediaTypes.add(MediaType.IMAGE_PNG);
        supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.TEXT_XML);
        FastJsonHttpMessageConverter fastjson = new FastJsonHttpMessageConverter();
        fastjson.setFastJsonConfig(fastJsonConfig);
        fastjson.setSupportedMediaTypes(supportedMediaTypes);
        return new HttpMessageConverters(fastjson);
    }

}
