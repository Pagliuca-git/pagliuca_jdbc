import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: GAOYX
 * @date: 2020/3/14
 * @description: 字符校验
 */
@Component
public class StringTools {
    public static boolean isNullOrEmpty(String val) {
        boolean flag = false;
        if (StringUtils.isEmpty(val) || StringUtils.isBlank(val)) {
            flag = true;
        }
        return flag;
    }

    /**
     * 判断对象是否为空或者null
     *
     * @param obj 对象
     * @return
     * @author James
     * @time 2018/11/12
     */
    public static boolean isNullOrEmpty(Object obj) {
        boolean isEmpty = false;
        if (obj == null) {
            isEmpty = true;
        } else if (obj instanceof String) {
            isEmpty = ((String) obj).trim().isEmpty();
        } else if (obj instanceof Collection) {
            isEmpty = (((Collection) obj).size() == 0);
        } else if (obj instanceof Map) {
            isEmpty = ((Map) obj).size() == 0;
        } else if (obj.getClass().isArray()) {
            isEmpty = Array.getLength(obj) == 0;
        }
        return isEmpty;
    }

    //判断是否是数字(包含小数)
    public static boolean isNumeric(String str) {
        String reg = "^-?[0-9]+(.[0-9]+)?$";
        return str.matches(reg);
    }

    public static boolean isNumber(String str) {
        if (str != null && str.length() < 11 && !"".equals(str.trim())) {
            Pattern pattern = Pattern.compile("[0-9]*");
            Long number = 0l;
            if (pattern.matcher(str).matches()) {
                number = Long.parseLong(str);
            } else {
                return false;
            }
            if (number > 2147483647) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public static String subLimitStr(String oldstr) {
        int objLength = oldstr.length();
        int num = 20;
        if (objLength > num) {
            oldstr = oldstr.substring(0, num);
        }
        return oldstr;
    }

    public static boolean isNullOrWhiteSpace(String a) {
        return a == null || (a.length() > 0 && a.trim().length() <= 0);
    }

    public static String GetJsonDataString(String para, String value) throws Exception {
        String jsonParas = "";
        try {
            Map<String, Object> dic = new HashMap<>();
            String[] paras = null;
            String[] values = null;
            if (para.contains("&") && value.contains("&")) {
                paras = para.split("&");
                values = value.split("&");
                if (paras.length != values.length) {
                    throw new Exception("参数名【" + para + "】与参数值【" + value + "】个数不匹配");
                }
                for (int i = 0; i < paras.length; i++) {
                    if (dic.containsKey(paras[i])) {
                        throw new Exception("参数名【" + paras[i] + "】重复");
                    } else {
                        dic.put(paras[i], values[i]);
                    }
                }
            } else {
                if (!para.contains("&") && !value.contains("&")) {
                    dic.put(para, value);
                } else {
                    throw new Exception("参数名【" + para + "】与参数值【" + value + "】个数不匹配");
                }
            }
            if (dic.size() > 0) {
                jsonParas = JacksonUlits.mapToJson(dic);
            } else {
                jsonParas = "{}";
            }
        } catch (Exception ex) {
            throw new Exception("【参数:" + para + ",参数值:" + value + "】转换成JSON数据格式失败:" + ex.getMessage());
        }
        return jsonParas;
    }

    public static String GetServiceName(String errMsg)
    {
        String name="";
        if (errMsg.contains(":"))
        {
            name = errMsg.substring(0, errMsg.indexOf(":"));
        }
        return name;
    }

    /**
     * 构造list类型json
     * @param val 原字符串
     * @return
     */
    public static String GetListJsonData(String val)
    {
        if (val.substring(0, 1) == "{") {
            return "[" + val + "]";
        } else {
            return val;
        }
    }

    //字符串中文校验
    public static boolean isChineseStr(String str){
        if (isNullOrEmpty(str)) {
            return true;
        }
        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
        char c[] = str.toCharArray();
        for(int i=0;i<c.length;i++){
            Matcher matcher = pattern.matcher(String.valueOf(c[i]));
            if(!matcher.matches()){
                return false;
            }
        }
        return true;
    }

    public static boolean checkStraHasStrb(String a, String b){
        return checkStraHasStrb(a, b, ",");
    }

    public static boolean checkStraHasStrb(String a, String b, String character){
        if(null == a || null == b){
            return false;
        }
        String[] ss = a.split(character);
        for (String s : ss) {
            if(b.equals(s)){
                return true;
            }
        }
        return false;
    }

    public static String spellStraNoHasStrb(String a, String b){
        return spellStraNoHasStrb(a, b, ",");
    }

    public static String spellStraNoHasStrb(String a, String b, String character){
        if(StringUtils.isEmpty(a)){
            return b;
        }
        if(StringUtils.isEmpty(b)){
            return a;
        }
        List<String> res = new ArrayList<>();
        List<String> as = Arrays.asList(a.split(character));
        List<String> bs = Arrays.asList(b.split(character));
        res.addAll(as);
        for (String s : bs) {
            if(!res.contains(s)){
                res.add(s);
            }
        }
        return String.join(character.replaceAll("\\\\", ""), res);
    }

    public static boolean replaceObjAllFields(Object obj) {
        if (null == obj) {
            return false;
        }
        try {
            if (obj instanceof Collection) {
                List<Object> list = (List) obj;
                for (Object row : list) {
                    field(row);
                }
            } else {
                field(obj);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void field(Object obj) throws Exception {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (isNullOrEmpty(field.get(obj))) {
                continue;
            }
            if (field.get(obj) instanceof String) {
                if (field.get(obj).toString().contains("'")) {
                    field.set(obj, field.get(obj).toString().replace("'", "''"));
                }
            }
        }
    }

}
