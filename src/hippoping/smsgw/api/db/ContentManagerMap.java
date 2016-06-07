package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class ContentManagerMap {

    protected String db_code;
    protected OperConfig.CARRIER oper;
    protected int srvc_main_id;

    public ContentManagerMap() {
    }

    public ContentManagerMap(String db_code)
            throws Exception {
        this.db_code = db_code;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT * FROM ctnt_mngr_map  WHERE db_code=? LIMIT 1";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, db_code);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    this.oper = OperConfig.CARRIER.fromId(rs.getInt("oper_id"));
                    this.srvc_main_id = rs.getInt("srvc_main_id");
                } else {
                    throw new Exception("db_code not found!!");
                }

                rs.close();
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static List<ContentManagerMap> getDbcodes(OperConfig.CARRIER oper, int srvc_main_id) throws Exception {
        List list = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();

            String whereoper = "";
            if (oper != OperConfig.CARRIER.ALL) {
                whereoper = " AND oper_id=" + oper.getId();
            }
            try {
                String sql = "  SELECT db_code FROM ctnt_mngr_map  WHERE 1" + whereoper + "   AND srvc_main_id=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                list.clear();
                while (rs.next()) {
                    try {
                        ContentManagerMap cmm = new ContentManagerMap(rs.getString(1));

                        if (cmm != null) {
                            list.add(cmm);
                        }
                    } catch (Exception e) {
                    }
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return list;
    }

    public int add(String db_code, OperConfig.CARRIER oper, int srvc_main_id) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "  INSERT INTO ctnt_mngr_map (db_code, oper_id, srvc_main_id) VALUES (?,?,?)";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, db_code);
                cp.getPreparedStatement().setInt(2, oper.getId());
                cp.getPreparedStatement().setInt(3, srvc_main_id);

                row = cp.execUpdatePrepareStatement();

                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "create new dbcode " + row + " row(s) inserted");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return row;
    }

    protected int remove() {
        return remove(this.oper, this.srvc_main_id);
    }

    public int remove(OperConfig.CARRIER oper, int srvc_main_id) {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();

            String whereoper = "";
            if (oper != OperConfig.CARRIER.ALL) {
                whereoper = " AND oper_id=" + oper.getId();
            }
            try {
                String sql = "  DELETE FROM ctnt_mngr_map WHERE 1" + whereoper + "   AND srvc_main_id=?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);

                row = cp.execUpdatePrepareStatement();

                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "dbcode " + row + " row(s) removed.");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return row;
    }

    public String getDb_code() {
        return this.db_code;
    }

    public OperConfig.CARRIER getOper() {
        return this.oper;
    }

    public int getSrvc_main_id() {
        return this.srvc_main_id;
    }
}