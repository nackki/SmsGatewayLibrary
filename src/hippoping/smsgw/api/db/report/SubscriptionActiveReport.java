package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.SubscriberGroup;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lib.common.DBPoolManager;

public class SubscriptionActiveReport implements Serializable {

    public List<SubscriptionActive> getSubscriptionActiveReport() {
        List subActiveList = new ArrayList();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "  SELECT m.srvc_main_id"
                        + "     , " + OperConfig.CARRIER.DTAC.getId() + " AS OPER_ID"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0))"
                        + "     , COUNT(*)"
                        + "  FROM mmbr_dtac m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.DTAC.getId()
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                        + " GROUP BY m.srvc_main_id"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId()
                        + " AND extd_ctr>0))"
                        + " UNION "
                        + "SELECT m.srvc_main_id"
                        + "     , " + OperConfig.CARRIER.DTAC_SDP.getId() + " AS OPER_ID"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0))"
                        + "     , COUNT(*)"
                        + "  FROM mmbr_dtac m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.DTAC_SDP.getId()
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                        + " GROUP BY m.srvc_main_id"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId()
                        + " AND extd_ctr>0))"
                        + " UNION "
                        + "SELECT m.srvc_main_id"
                        + "     , " + OperConfig.CARRIER.TRUE.getId() + " AS OPER_ID"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0))"
                        + "     , COUNT(*)"
                        + "  FROM mmbr_true m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.TRUE.getId()
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                        + " GROUP BY m.srvc_main_id"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0))"
                        + " UNION "
                        + "SELECT m.srvc_main_id"
                        + "     , " + OperConfig.CARRIER.TRUEH.getId() + " AS OPER_ID"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0))"
                        + "     , COUNT(*)" + "  FROM mmbr_true m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.TRUEH.getId()
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                        + " GROUP BY m.srvc_main_id"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0))"
                        + " UNION "
                        + "SELECT m.srvc_main_id"
                        + "     , " + OperConfig.CARRIER.AIS.getId() + " AS OPER_ID"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0))" + "     , COUNT(*)"
                        + "  FROM mmbr_ais m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.AIS.getId()
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                        + " GROUP BY m.srvc_main_id"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0))"
                        + " UNION "
                        + "SELECT m.srvc_main_id"
                        + "     , " + OperConfig.CARRIER.AIS_LEGACY.getId() + " AS OPER_ID"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0))"
                        + "     , COUNT(*)"
                        + "  FROM mmbr_ais m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.AIS_LEGACY.getId()
                        + "   AND ss.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + "   AND ss.srvc_type & " + ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId()
                        + " GROUP BY m.srvc_main_id"
                        + "     , (state=" + SubscriberGroup.SUB_STATUS.REGISTER.getId()
                        + "       OR (state=" + SubscriberGroup.SUB_STATUS.PREPARE2REGISTER.getId() + " AND extd_ctr>0))";

                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    try {
                        ServiceElement se = new ServiceElement(rs.getInt(1), rs.getInt(2), ServiceElement.SERVICE_TYPE.SUBSCRIPTION.getId(), ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId());

                        SubscriptionActive subActive = new SubscriptionActive(se);
                        switch (SubscriptionActive.STATUS.fromId(rs.getInt(3))) {
                            case INACTIVE:
                                subActive.setAmountInactive(OperConfig.CARRIER.fromId(rs.getInt(2)), rs.getInt(4));
                                break;
                            case ACTIVE:
                            default:
                                subActive.setAmountActive(OperConfig.CARRIER.fromId(rs.getInt(2)), rs.getInt(4));
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
            e.printStackTrace();
        }
        return subActiveList;
    }
}