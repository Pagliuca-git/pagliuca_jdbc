

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * @desc Json处理工具类(jackson)
 */

public class JacksonUlits {

    private static final Logger logger = LoggerFactory.getLogger(JacksonUlits.class);

    //private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");

    //对象转json字符串
    public static String objectToJson(Object data) {
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.setDateFormat(dateFormat);
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("objectToJson error:" + e.toString());
        }
        return null;
    }

    //对象转json字符串，不序列化null
    public static String objectToJsonIgnoreNull(Object data) {
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.setDateFormat(dateFormat);
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            //MAPPER.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
            return MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("objectToJsonIgnoreNull error:" + e.toString());
        }
        return null;
    }

    //map转换json
    public static String mapToJson(Map<String, Object> paramsMap) {
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            return MAPPER.writeValueAsString(paramsMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("mapToJson error:" + e.toString());
        }
        return null;
    }

    //stringMap转换json
    public static String stringMapToJson(Map<String, String> paramsMap) {
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            return MAPPER.writeValueAsString(paramsMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("stringMapToJson error:" + e.toString());
        }
        return null;
    }

    //list集合转json字符串
    public static <T> String listToJson(List<T> objectList) {
        if (objectList == null || objectList.size() == 0) {
            return "[]";
        }
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.setDateFormat(dateFormat);
            return MAPPER.writeValueAsString(objectList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("listToJson error:" + e.toString());
        }
        return null;
    }

    //list集合转json字符串
    public static <T> String listToJsonIgnoreNull(List<T> objectList) {
        if (objectList == null || objectList.size() == 0) {
            return "[]";
        }
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.setDateFormat(dateFormat);
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            //MAPPER.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
            return MAPPER.writeValueAsString(objectList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("listToJsonIgnoreNull error:" + e.toString());
        }
        return null;
    }

    //字符串转pojo实体
    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.setDateFormat(dateFormat);
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //忽略大小写
            MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            return MAPPER.readValue(jsonData, beanType);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("jsonToPojo error:" + e.toString());
        }
        return null;
    }

    //object、hashmap、linkedHashMap 转pojo
    public static <T> T objectToPojo(Object singleObject, Class<T> beanType) {
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.setDateFormat(dateFormat);
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            return MAPPER.convertValue(singleObject, beanType);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("objectToPojo error:" + e.toString());
        }
        return null;
    }

    //object、hashmap、linkedHashMap 转pojo集合
    public static <T> List<T> objectsToPojoList(Object singleObject, Class<?> collectionClass, Class<?>... elementClasses) {
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.setDateFormat(dateFormat);
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(collectionClass, elementClasses);
            return MAPPER.convertValue(singleObject, javaType);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("objectsToPojoList error:" + e.toString());
        }
        return null;
    }

    //json转map
    public static Map<String, Object> jsonToMap(String jsonData) {
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
            return MAPPER.readValue(jsonData, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("jsonToMap error:" + e.toString());
        }
        return null;
    }

    //json转list集合
    //如果不用这种方式也可以直接写：ObjectMapper mapper = new ObjectMapper();
    //List<SysModalColCfg> modalColsList = mapper.readValue(configCols, new TypeReference<ArrayList<SysModalColCfg>>() { });
    public static <T> T jsonToList(String jsonString, Class<?>... elementClasses) {
        if (StringTools.isNullOrEmpty(jsonString)) {
            return null;
        }
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            MAPPER.setDateFormat(dateFormat);
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, elementClasses);
            return MAPPER.readValue(jsonString, javaType);
        } catch (IOException e) {
            logger.error("jsonToList error:" + e.toString());
            return null;
        }
    }

    //json转为ListMap集合
    public static List<Map<String, Object>> jsonToMapList(String jsonString) {
        if (StringTools.isNullOrEmpty(jsonString)) {
            return null;
        }
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            return MAPPER.readValue(jsonString, List.class);
        } catch (IOException e) {
            logger.error("jsonToMapList error:" + e.toString());
            return null;
        }
    }

    public static <T> T servoJsonToListEntity(String jsonString, Class<?>... elementClasses) {
        if (StringTools.isNullOrEmpty(jsonString)) {
            return null;
        }
        try {
            ObjectMapper MAPPER = new ObjectMapper();
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            MAPPER.setDateFormat(dateFormat1);
            JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, elementClasses);
            return MAPPER.readValue(jsonString, javaType);
        } catch (IOException e) {
            logger.error("ServoJsonToList error:" + e.toString());
            return null;
        }
    }

    /**
     * 通用返回结果转换实体
     *
     * @param list
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> getPojo(List<Map<String, Object>> list, Class<T> clazz) {
        List<T> o = jsonToList(objectToJson(list), clazz);
        return o;

    }
}
