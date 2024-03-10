

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Map;
import java.util.Properties;

/**
 * Created by LiuKunpeng on 2023-11-29 12:52
 */
public class JDBCTools {

    private static  ThreadLocal<Connection> local = new ThreadLocal<>();

    //开启事务
    public static void startTransaction(Map<String,Object> urlMap) throws SQLException{
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

    public static Connection getConnection(Map<String,Object> urlMap){
        //1. 读配置文件参数
//        Properties properties = new Properties();
        Connection connection = null;
        try{
//            InputStream in = JDBCTools.class.getClassLoader().getResourceAsStream("db.properties");
//            properties.load(in);
//            String user = properties.getProperty("user");
//            String password = properties.getProperty("password");
//            String url = properties.getProperty("url");
//            String driver = properties.getProperty("driver");
            String user = String.valueOf(urlMap.get("user"));
            String password = String.valueOf(urlMap.get("password"));
            String url = String.valueOf(urlMap.get("url"));
            String driver = "com.mysql.cj.jdbc.Driver";
            //通过反射获取dirver驱动加载
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        }
        catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
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
}
