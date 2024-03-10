import java.util.List;
import java.util.Map;

/**
 * Created by LiuKunpeng on 2023-11-29 13:45
 */
public class ResponseEntity {
    private boolean flag = false;
    private List<Map<String,Object>> queryMsg;
    private String error;
    private int updateCount;

    public ResponseEntity() {
    }

    public ResponseEntity(boolean flag, List<Map<String, Object>> queryMsg, String error) {
        this.flag = flag;
        this.queryMsg = queryMsg;
        this.error = error;
    }

    public boolean getFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public List<Map<String, Object>> getQueryMsg() {
        return queryMsg;
    }

    public void setMsg(List<Map<String, Object>> queryMsg) {
        this.queryMsg = queryMsg;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }
}
