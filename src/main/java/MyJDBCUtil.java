import com.alibaba.druid.pool.DruidConnectionHolder;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

/**
 * Created by LiuKunpeng on 2023-11-29 13:30
 */
@Component
public class MyJDBCUtil {

    static DruidDataSource ds;

    static {
        ds = new DruidDataSource();
        String driver = "com.mysql.cj.jdbc.Driver";
        ds.setDriverClassName(driver);
        String url="jdbc:mysql://localhost:3306/test";
        ds.setUrl(url);
        ds.setUsername("root");
        ds.setPassword("123456");
        ds.setInitialSize(2);
        ds.setMaxActive(20);
        ds.setKeepAlive(true);
        ds.setMaxWait(60000);
    }

    private static  ThreadLocal<Connection> local = new ThreadLocal<>();

    //开启事务
    public static void startTransaction(Map<String,Object> urlMap) throws Exception{
        Connection conn = getConnection(urlMap);
        conn.setAutoCommit(false);
    }

    //提交事务
    public static void commit() throws SQLException {
        Connection conn = local.get();
        if(conn!=null){
            conn.commit();
            conn.close();
            local.set(null);
        }
    }
    //回滚事务
    public static void rollback() throws SQLException {
        Connection conn = local.get();
        if(conn!=null){
            conn.rollback();
            conn.close();
            local.set(null);
        }
    }

    public static Connection getConnection() throws Exception {
        DruidPooledConnection connection = ds.getConnection();
        return connection;
    }

    public static Connection getConnection(Map<String,Object> urlMap) throws Exception {
        Connection connection = null;
//        String user = urlMap.get("user");
//        String password = urlMap.get("password");
//        String url = urlMap.get("url");
//        String driver = "com.mysql.cj.jdbc.Driver";
        //通过反射获取dirver驱动加载
//            Class.forName(driver);
//            connection = DriverManager.getConnection(url, user, password);
        String driver = "com.mysql.cj.jdbc.Driver";
        Map<String,Object> map = new HashMap<>();
        map.put("url",urlMap.get("url"));
        map.put("username",urlMap.get("user"));
        map.put("password",urlMap.get("password"));
        map.put("driverClassName",driver);
        map.put("initialSize",String.valueOf(2));
        map.put("maxActive",String.valueOf(300));
        map.put("maxWait",String.valueOf(60000));
//        DataSource ds = DruidDataSourceFactory.createDataSource(map);
        connection = ds.getConnection();
        return connection;
    }

    public static List<String> getDataSource(String dbflag) throws IOException {
        Properties properties = new Properties();
        InputStream in = JDBCTools.class.getClassLoader().getResourceAsStream("db.properties");
        properties.load(in);
        String ip="",user="",password="";
        if("dev".equals(dbflag)){
            ip=properties.getProperty("devIp");
            user=properties.getProperty("devUser");
            password=properties.getProperty("devPassword");

        }
        if("prd".equals(dbflag)){
            ip=properties.getProperty("proIp");
            user=properties.getProperty("proUser");
            password=properties.getProperty("proPassword");
        }
        String url="jdbc:mysql://"+ip+":3306/test";
        List<String> list = new ArrayList<>();
        list.add(url);
        list.add(user);
        list.add(password);
        return list;
    }

    public static void close(PreparedStatement ps, ResultSet rs, Connection connection){
        try{
            if(ps !=null){
                ps.close();
            }
            if(rs !=null){
                rs.close();
            }
            if(connection !=null){
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeStatement(Statement statement, ResultSet rs, Connection connection){
        try{
            if(statement !=null){
                statement.close();
            }
            if(rs !=null){
                rs.close();
            }
            if(connection !=null){
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResponseEntity getService(ServiceVo serviceVo) throws Exception{
        ResponseEntity response = new ResponseEntity();
        //todo 服务名、dbflag判空,用validation
        String sql = "select * from sys_service where serviceName=:serviceName";
        String queryParam = "{\"serviceName\": \"" + serviceVo.getServiceName() +"\"}";
        Map<String,Object> urlMap = new HashMap<>();
        String dbflag = serviceVo.getDbflag();
        List<String> source = getDataSource(dbflag);
        urlMap.put("url",source.get(0));
//        urlMap.put("user",source.get(1));
        urlMap.put("userName",source.get(1));
        urlMap.put("password",source.get(2));
        ResponseEntity serviceRes = executeQueryObj(urlMap, sql, queryParam);
        ResponseEntity serviceResData = checkResponseEntity(response, serviceRes);
        String dataName = Optional.ofNullable(String.valueOf(serviceResData.getQueryMsg().get(0).get("dataName"))).orElse("");
        if(StringUtils.isEmpty(dataName)){
            response.setError(serviceVo.getServiceName()+" 服务对应的dataName为空");
            return response;
        }
        ResponseEntity single = getDBdata(dataName,dbflag);
        ResponseEntity singleData = checkResponseEntity(response, single);
        DataVo dataVo = JacksonUlits.objectToPojo(singleData.getQueryMsg().get(0), DataVo.class);
        if (dataVo != null) {
            String dataSource = Optional.ofNullable(dataVo.getDataSource()).orElse("");
            if(dataSource.contains(";")){
                String[] split = dataSource.split(";");
                if(split.length!=3){
                    response.setError("dataSource格式错误");
                    return response;
                }
                String urlData = split[0];
                String user = split[1];
                String password = split[2];
                Map<String,Object> mapData = new HashMap<>();
                mapData.put("url",urlData);
                mapData.put("user",user);
                mapData.put("password",password);
                if(Constants.GET.equals(serviceVo.getType())){
                    return executeQueryObj(mapData,dataVo.getQuerySql(), serviceVo.getJson());
                }
                if(Constants.POST.equals(serviceVo.getType())){
                    List<Map<String, Object>> maps = JacksonUlits.jsonToMapList(serviceVo.getJson());
                    for (Map<String, Object> map : maps) {
                        if(ObjectUtils.isEmpty(map.get("changetype"))){
                            response.setError("缺少changetype关键字");
                            return response;
                        }
                        String changetype = String.valueOf(map.get("changetype"));
                        if(changetype.equals(Constants.INSERTED)){
                            return executeInsertBatch(mapData,dataVo.getTable(),serviceVo.getJson());
                        }
                        if(changetype.equals(Constants.UPDATED)){
                            return executeUpdateBatch(mapData,dataVo.getTable(),dataVo.getPk(),serviceVo.getJson());
                        }if(changetype.equals(Constants.DELETED)){
                            return executeDeleteBatch(mapData,dataVo.getTable(),dataVo.getPk(),serviceVo.getJson());
                        }
                    }
                }
            }
        }
        return response;
    }

    private static ResponseEntity checkResponseEntity(ResponseEntity response, ResponseEntity serviceRes) {
        if(!serviceRes.getFlag()){
            response.setError(serviceRes.getError());
            return response;
        }
        if(CollectionUtils.isEmpty(serviceRes.getQueryMsg())){
            response.setFlag(true);
            response.setMsg(new ArrayList<>());
            return response;
        }
        return serviceRes;
    }

    public static ResponseEntity getDBdata(String dataName,String dbflag) throws Exception{
        String sql = "select * from sys_data where dataName=:dataName";
        String queryParam = "{\"dataName\": \"" + dataName +"\"}";
        Map<String,Object> urlMap = new HashMap<>();
        List<String> dataSource = getDataSource(dbflag);
        urlMap.put("url",dataSource.get(0));
//        urlMap.put("user",dataSource.get(1));
        urlMap.put("userName",dataSource.get(1));
        urlMap.put("password",dataSource.get(2));
        return executeQueryObj(urlMap,sql, queryParam);
    }

    public static ResponseEntity executeUpdateBatch(Map<String,Object> urlMap,String table,String pk,String params) throws Exception{
        ResponseEntity response = checkUpateParams(params);
        List<Map<String, Object>> list = JacksonUlits.jsonToMapList(params);
        //获取连接
        Connection connection = getConnection(urlMap);
        Statement statement = null;
        statement = connection.createStatement();
        int sqlSize = 0;
        int batchSize = 0;
        for (Map<String, Object> maps : list) {
            StringBuffer sb = new StringBuffer();
            Set<Map.Entry<String, Object>> entrySet = maps.entrySet();
            int paramSize = 0;
            for (Map.Entry<String, Object> entry : entrySet) {
                if(!entry.getKey().equals("changetype")){
                    sb.append(entry.getKey()+"="+getObjectType(entry.getValue()));
                    paramSize++;
                    if(paramSize < entrySet.size()-1){
                        sb.append(",");
                    }
                }
            }
            String sql = "update " + table + " set " + sb + " where " + pk + "=" + getObjectType(maps.get(pk)) + ";";
            sqlSize++;
            statement.addBatch(sql);
            if(sqlSize%2 == 0 && list.size() > sqlSize){
                statement.executeBatch();
                statement.clearBatch();
                batchSize+=2;
            }
        }
        try {
            int[] ints = statement.executeBatch();
            int length = ints.length;
            response.setFlag(true);
            response.setUpdateCount(length+batchSize);
        }
        //最后统一关闭
        finally {
            closeStatement(statement, null,connection);
        }
        return response;
    }

    public static ResponseEntity executeDeleteBatch(Map<String,Object> urlMap,String table,String pk,String params) throws Exception {
        ResponseEntity response = checkUpateParams(params);
        List<Map<String, Object>> list = JacksonUlits.jsonToMapList(params);
        //获取连接
        Connection connection = getConnection(urlMap);
        Statement statement = null;
        statement = connection.createStatement();
        int sqlSize = 0;
        int batchSize = 0;
        for (Map<String, Object> maps : list) {
            StringBuffer value = new StringBuffer();
            Set<Map.Entry<String, Object>> entrySet = maps.entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                if(!entry.getKey().equals("changetype")){
                    value.append(getObjectType(entry.getValue()));
                }
            }
            String sql = "delete from " + table + " where " + pk + "="+value+";";
            statement.addBatch(sql);
            sqlSize++;
            statement.addBatch(sql);
            if(sqlSize%2 == 0 && list.size() > sqlSize){
                statement.executeBatch();
                statement.clearBatch();
                batchSize+=2;
            }
        }
        try {
            int[] ints = statement.executeBatch();
            int length = ints.length;
            response.setFlag(true);
            response.setUpdateCount(length+batchSize);
        }
        //最后统一关闭
        finally {
            closeStatement(statement, null,connection);
        }
        return response;
    }

    public static ResponseEntity checkUpateParams(String params){
        ResponseEntity response = new ResponseEntity();
        List<Map<String, Object>> list = Optional.ofNullable(JacksonUlits.jsonToMapList(params)).orElse(new ArrayList<>());
        if(list.isEmpty()){
            response.setError("参数传入为空");
        }
        return response;
    }
    public static ResponseEntity executeInsertBatch(Map<String,Object> urlMap,String table,String params) throws Exception{
        ResponseEntity response = checkUpateParams(params);
        List<Map<String, Object>> list = JacksonUlits.jsonToMapList(params);
        //获取连接
        Connection connection = getConnection(urlMap);
        Statement statement = null;
        statement = connection.createStatement();
        int sqlSize = 0;
        int batchSize = 0;
        for (Map<String, Object> maps : list) {
            StringBuffer field = new StringBuffer();
            StringBuffer value = new StringBuffer();
            Set<Map.Entry<String, Object>> entrySet = maps.entrySet();
            int paramSize = 0;
            for (Map.Entry<String, Object> entry : entrySet) {
                if(!entry.getKey().equals("changetype")){
                    field.append(entry.getKey());
                    value.append(getObjectType(entry.getValue()));
                    paramSize++;
                    if(paramSize < entrySet.size()-1){
                        field.append(",");
                        value.append(",");
                    }
                }
            }
            String sql = "insert into " + table + " (" + field + ") values (" + value + ");";
            sqlSize++;
            statement.addBatch(sql);
            if(sqlSize%2 == 0 && list.size() > sqlSize){
                statement.executeBatch();
                statement.clearBatch();
                batchSize+=2;
            }
        }
        try {
            int[] ints = statement.executeBatch();
            int length = ints.length;
            response.setFlag(true);
            response.setUpdateCount(length+batchSize);
        }
        //最后统一关闭
        finally {
            closeStatement(statement, null,connection);
        }
        return response;
    }

    //判断传入类型再拼串
    public static String getObjectType(Object o){
        String value = "";
        if(o == null){
            value = null;
        }
        if(o instanceof String){
            value = "'"+o+"'";
        }
        if(o instanceof Integer || o instanceof Long || o instanceof BigInteger || o instanceof Float || o instanceof Double || o instanceof BigDecimal){
            value = String.valueOf(o);
        }
        return value;
    }
    public static ResponseEntity executeQueryObj(Map<String,Object> urlMap,String sql,String params) throws Exception{
        ResponseEntity response = new ResponseEntity();
        Map<String, Object> maps = JacksonUlits.jsonToMap(params);
        if(maps == null || maps.isEmpty()){
            response.setError("参数传入为空");
            return response;
        }
        //获取连接
//        Connection connection = getConnection(urlMap);
        Connection connection = null;
        if(urlMap.get("url").equals("jdbc:mysql://localhost:3306/test")){
            connection = getConnection();
        }else{
            DruidDataSource newDs = new DruidDataSource();
            String driver = "com.mysql.cj.jdbc.Driver";
            newDs.setDriverClassName(driver);
            newDs.setUrl(String.valueOf(urlMap.get("url")));
            newDs.setUsername("root");
            newDs.setPassword("123456");
            newDs.setInitialSize(2);
            newDs.setMaxActive(20);
            newDs.setKeepAlive(true);
            newDs.setMaxWait(60000);
            connection = newDs.getConnection();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String,Object>> list = new ArrayList<>();
        try {
            // 设置参数
            if(sql.contains(":")){
                String sqlReplace = "";
                Set<Map.Entry<String, Object>> entrySet = maps.entrySet();
                for (Map.Entry<String, Object> entry : entrySet) {
                    if(!sql.contains((":"+entry.getKey()))){
                        response.setError("参数传入不完整");
                        return response;
                    }
                    sqlReplace = sql.replace(":"+entry.getKey(),getObjectType(entry.getValue()));
                    sql = sqlReplace;
                }
                ps = connection.prepareStatement(sqlReplace);
            }else {
                ps = connection.prepareStatement(sql);
            }
            if (ps != null) {
                rs = ps.executeQuery();
            }
            // 处理查询结果集
            ResultSetMetaData metaData = null;
            if (rs != null) {
                metaData = rs.getMetaData();
            }
            int columnCount = metaData.getColumnCount();
            while (rs.next()){
                Map<String,Object> map = new LinkedHashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    String columnLabel = metaData.getColumnLabel(i + 1);
                    Object value = rs.getObject(columnLabel);
                    map.put(columnLabel,value);
                }
                list.add(map);
            }
            response.setFlag(true);
            response.setMsg(list);
        }
        //关闭连接，并不是真正关闭，而是归还到jdbc连接池
        finally {
            closeStatement(ps, rs, connection);
        }
        return response;
    }
}
