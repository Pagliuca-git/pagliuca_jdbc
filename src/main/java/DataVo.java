import lombok.Data;

/**
 * Created by LiuKunpeng on 2023-12-01 14:13
 */
@Data
public class DataVo {
    private String dataName;
    private String dataSource;
    private String querySql;
    private String table;
    private String pk;
}
