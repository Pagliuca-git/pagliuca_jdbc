import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiuKunpeng on 2023-11-29 14:03
 */
public class TestMyJdbc {
    public static void main(String[] args) {

        ServiceVo serviceVo = new ServiceVo();
        serviceVo.setServiceName("testUpdate");
        serviceVo.setDbflag("dev");
        serviceVo.setType("post");

//        ServiceVo serviceVo = new ServiceVo();
//        serviceVo.setServiceName("testQuery");
//        serviceVo.setDbflag("dev");
//        serviceVo.setType("get");
//        String queryParam = "{\"id\": 1,\"Name\": \"唐三藏\"}";
//        serviceVo.setJson(queryParam);
//        ResponseEntity queryRes = null;
//        try {
//            queryRes = MyJDBCUtil.getService(serviceVo);
//        } catch (SQLException | IOException e) {
//            e.printStackTrace();
//        }


        List<Dept> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Dept dept = new Dept();
            dept.setId(i);
            dept.setAge(20);
            dept.setName("孙悟空");
            dept.setChangetype("inserted");
            list.add(dept);
        }
        String insertParamList = JacksonUlits.listToJson(list);
        serviceVo.setJson(insertParamList);
        ResponseEntity insertRes = null;
        try {
            insertRes = MyJDBCUtil.getService(serviceVo);
        } catch (Exception e) {
            e.printStackTrace();
        }


//        List<Dept> list = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            Dept dept = new Dept();
//            dept.setId(i);
//            dept.setAge(10);
//            dept.setName("孙悟空1");
//            dept.setChangetype("updated");
//            list.add(dept);
//        }
//        String insertParamList = JacksonUlits.listToJson(list);
//        serviceVo.setJson(insertParamList);
//        ResponseEntity insertRes = null;
//        try {
//            insertRes = MyJDBCUtil.getService(serviceVo);
//        } catch (SQLException | IOException e) {
//            e.printStackTrace();
//        }


//        String deleteParamList = "[{\"id\": 0,\"changetype\": \"deleted\"},{\"id\": 1,\"changetype\": \"deleted\"},{\"id\": 2,\"changetype\": \"deleted\"},{\"id\": 3,\"changetype\": \"deleted\"},{\"id\": 4,\"changetype\": \"deleted\"}]";
//        serviceVo.setJson(deleteParamList);
//        ResponseEntity insertRes = null;
//        try {
//            insertRes = MyJDBCUtil.getService(serviceVo);
//        } catch (SQLException | IOException e) {
//            e.printStackTrace();
//        }

        System.out.println("done");
    }
}
