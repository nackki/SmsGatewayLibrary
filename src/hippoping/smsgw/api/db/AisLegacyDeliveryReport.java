package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lib.common.DBPoolManager;
import lib.common.DatetimeUtil;

public class AisLegacyDeliveryReport extends DeliveryReport {

    protected int dr_lgcy_id;
    protected String ntype;
    protected String status;
    protected String costcode;
    protected String code;

    public AisLegacyDeliveryReport(int dr_lgcy_id)
            throws Exception {
        this.dr_lgcy_id = dr_lgcy_id;

        String sql = "   SELECT *   FROM trns_dr_lgcy  WHERE dr_lgcy_id=?";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);

                cp.getPreparedStatement().setInt(1, dr_lgcy_id);

                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    this.ntype = rs.getString("ntype");
                    this.msisdn = rs.getString("msisdn");
                    this.txid = rs.getString("txid");
                    this.status = rs.getString("status");
                    this.status_desc = rs.getString("status_desc");
                    this.costcode = rs.getString("costcode");
                    this.code = rs.getString("code");
                } else {
                    throw new Exception("id not found!!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<AisLegacyDeliveryReport> get(TxQueue txq) throws Exception {
        List list = null;
        List drList = null;

        String sql = "   SELECT *   FROM trns_dr_lgcy  WHERE txid=?    AND msisdn=?";
        try {
            drList = DeliveryReport.get(txq, txq.msisdn);

            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);

                for (int i = 0; i < drList.size(); i++) {
                    AisLegacyDeliveryReport aisdr = (AisLegacyDeliveryReport) drList.get(i);

                    cp.getPreparedStatement().setString(1, txq.txid);
                    cp.getPreparedStatement().setString(2, txq.msisdn);

                    ResultSet rs = cp.execQueryPrepareStatement();
                    if (rs.next()) {
                        aisdr.dr_lgcy_id = rs.getInt("dr_lgcy_id");
                        aisdr.ntype = rs.getString("ntype");
                        aisdr.status = rs.getString("status");
                        aisdr.status_desc = rs.getString("status_desc");
                        aisdr.costcode = rs.getString("costcode");
                        aisdr.code = rs.getString("code");
                    }

                    list.add(aisdr);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static int add(String ntype, String msisdn, String txid, String status, String status_desc, String costcode, String code) {
        int id = 0;

        DeliveryReport.add(txid, msisdn, status.equals("OK") ? 0 : 1, status + (!status_desc.isEmpty() ? "|" + status_desc : ""), DatetimeUtil.getDateTime(), OperConfig.CARRIER.AIS_LEGACY.getId());

        String sql = " INSERT INTO         trns_dr_lgcy (ntype, msisdn, txid, status, status_desc, costcode, code) VALUES (?,?,?,?,?,?,?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql, 1);

                cp.getPreparedStatement().setString(1, ntype);
                cp.getPreparedStatement().setString(2, msisdn);
                cp.getPreparedStatement().setString(3, txid);
                cp.getPreparedStatement().setString(4, status);
                cp.getPreparedStatement().setString(5, status_desc);
                cp.getPreparedStatement().setString(6, costcode);
                cp.getPreparedStatement().setString(7, code);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;
    }

    public String getCode() {
        return this.code;
    }

    public String getCostcode() {
        return this.costcode;
    }

    public int getDr_lgcy_id() {
        return this.dr_lgcy_id;
    }

    public String getNtype() {
        return this.ntype;
    }

    public String getStatus() {
        return this.status;
    }
}