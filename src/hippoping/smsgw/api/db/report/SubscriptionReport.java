/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.ServiceElement.SERVICE_STATUS;
import hippoping.smsgw.api.db.ServiceElement.SERVICE_TYPE;
import hippoping.smsgw.api.db.Subscriber;
import hippoping.smsgw.api.db.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lib.common.DBPoolManager;

/**
 *
 * @author developer
 */
public class SubscriptionReport {

    public SubscriptionReport() {
    }

    public static List<Subscriber> getSubscriptionList(int srvc_id, int oper_id, String fdate, String tdate, String orderby, int slimit, int rows) {

        List<Subscriber> subList = new ArrayList<Subscriber>();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String limit = "";
                if (slimit >= 0) {
                    limit = " LIMIT " + slimit + ((rows > 0) ? " ," + rows : "");
                }

                String wherestatus = SERVICE_STATUS.where(SERVICE_STATUS.ON.getId() | SERVICE_STATUS.TEST.getId(), "ss.status");
                String wheretype = SERVICE_TYPE.where(SERVICE_TYPE.SUBSCRIPTION.getId(), "ss.status");

                String sql = "SELECT m.msisdn, m.srvc_main_id, "
                        + CARRIER.DTAC.getId() + " AS OPER_ID FROM mmbr_dtac m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.DTAC.getId()
                        + wherestatus
                        + wheretype
                        + " WHERE 1"
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')"
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )"
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "")
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "")
                        + " UNION "
                        + "SELECT m.msisdn, m.srvc_main_id, "
                        + CARRIER.DTAC_SDP.getId() + " AS OPER_ID FROM mmbr_dtac m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.DTAC_SDP.getId()
                        + wherestatus
                        + wheretype
                        + " WHERE 1"
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')"
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )"
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "")
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "")
                        + " UNION "
                        + "SELECT m.msisdn, m.srvc_main_id, "
                        + OperConfig.CARRIER.TRUE.getId() + " AS OPER_ID FROM mmbr_true m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.TRUE.getId()
                        + wherestatus
                        + wheretype
                        + " WHERE 1"
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')"
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )"
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "")
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "")
                        + " UNION "
                        + "SELECT m.msisdn, m.srvc_main_id, "
                        + OperConfig.CARRIER.TRUEH.getId() + " AS OPER_ID"
                        + "  FROM mmbr_trueh m" + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.TRUEH.getId()
                        + wherestatus
                        + wheretype
                        + " WHERE 1"
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')"
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )"
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "")
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "")
                        + " UNION "
                        + "SELECT m.msisdn, m.srvc_main_id, "
                        + OperConfig.CARRIER.AIS.getId() + " AS OPER_ID FROM mmbr_ais m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.AIS.getId()
                        + wherestatus
                        + wheretype
                        + " WHERE 1"
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')"
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )"
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "")
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "")
                        + " UNION "
                        + "SELECT m.msisdn, m.srvc_main_id, "
                        + OperConfig.CARRIER.AIS_LEGACY.getId() + " AS OPER_ID FROM mmbr_ais m"
                        + " INNER JOIN srvc_sub ss"
                        + "    ON ss.srvc_main_id = m.srvc_main_id"
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.AIS_LEGACY.getId()
                        + wherestatus
                        + wheretype
                        + " WHERE 1"
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')"
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )"
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "")
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "")
                        + orderby
                        + limit;

                //System.out.println(sql); //nack_debug
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    try {
                        Subscriber sub = new Subscriber(rs.getString("msisdn"), rs.getInt("srvc_main_id"), CARRIER.fromId(rs.getInt(3)));
                        if (sub != null) {
                            subList.add(sub);
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
        return subList;
    }

    public static List<Subscriber> getSubscriptionList(int srvc_id, int oper_id, String fdate, String tdate, String orderby, int slimit, int rows, User user) {

        List<Subscriber> subList = new ArrayList<Subscriber>();
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String whereuid = "";
                if (user != null) {
                    whereuid = " AND ( 0";
                    for (int i = 0; i < user.getChildUid().length; i++) {
                        if (!user.getChildUid()[i].isEmpty()) {
                            whereuid += " OR ss.uid=" + user.getChildUid()[i];
                        }
                    }
                    whereuid += " )";
                }

                String limit = "";
                if (slimit >= 0) {
                    limit = " LIMIT " + slimit + ((rows > 0) ? " ," + rows : "");
                }

                String wherestatus = SERVICE_STATUS.where(SERVICE_STATUS.ON.getId() | SERVICE_STATUS.TEST.getId(), "ss.status");
                String wheretype = SERVICE_TYPE.where(SERVICE_TYPE.SUBSCRIPTION.getId(), "ss.status");

                String sql = "SELECT m.msisdn, m.srvc_main_id, " 
                        + OperConfig.CARRIER.DTAC.getId() + " AS OPER_ID" 
                        + "  FROM mmbr_dtac m" 
                        + " INNER JOIN srvc_sub ss" 
                        + "    ON ss.srvc_main_id = m.srvc_main_id" 
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.DTAC.getId() 
                        + wherestatus 
                        + wheretype 
                        + whereuid 
                        + " WHERE 1" 
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')" 
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )" 
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "") 
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "") 
                        + " UNION "
                        + "SELECT m.msisdn, m.srvc_main_id, " 
                        + OperConfig.CARRIER.DTAC_SDP.getId() + " AS OPER_ID" 
                        + "  FROM mmbr_dtac m" 
                        + " INNER JOIN srvc_sub ss" 
                        + "    ON ss.srvc_main_id = m.srvc_main_id" 
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.DTAC_SDP.getId() 
                        + wherestatus 
                        + wheretype 
                        + whereuid 
                        + " WHERE 1" 
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')" 
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )" 
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "") 
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "") 
                        + " UNION "
                        + "SELECT m.msisdn, m.srvc_main_id, " 
                        + OperConfig.CARRIER.TRUE.getId() + " AS OPER_ID" 
                        + "  FROM mmbr_true m" 
                        + " INNER JOIN srvc_sub ss" 
                        + "    ON ss.srvc_main_id = m.srvc_main_id" 
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.TRUE.getId() 
                        + wherestatus 
                        + wheretype 
                        + whereuid 
                        + " WHERE 1" + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')" 
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )" 
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "") 
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "") 
                        + " UNION " 
                        + "SELECT m.msisdn, m.srvc_main_id, " 
                        + OperConfig.CARRIER.TRUEH.getId() + " AS OPER_ID" 
                        + "  FROM mmbr_trueh m" 
                        + " INNER JOIN srvc_sub ss" 
                        + "    ON ss.srvc_main_id = m.srvc_main_id" 
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.TRUEH.getId() 
                        + wherestatus 
                        + wheretype 
                        + whereuid 
                        + " WHERE 1" 
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')" 
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )" 
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "") 
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "") 
                        + " UNION " 
                        + "SELECT m.msisdn, m.srvc_main_id, " 
                        + OperConfig.CARRIER.AIS.getId() + " AS OPER_ID" 
                        + "  FROM mmbr_ais m" + " INNER JOIN srvc_sub ss" 
                        + "    ON ss.srvc_main_id = m.srvc_main_id" 
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.AIS.getId() 
                        + wherestatus 
                        + wheretype 
                        + whereuid 
                        + " WHERE 1" 
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')" 
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )" 
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "") 
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "") 
                        + " UNION " 
                        + "SELECT m.msisdn, m.srvc_main_id, " 
                        + OperConfig.CARRIER.AIS_LEGACY.getId() + " AS OPER_ID" 
                        + "  FROM mmbr_ais m" + " INNER JOIN srvc_sub ss" 
                        + "    ON ss.srvc_main_id = m.srvc_main_id" 
                        + "   AND ss.oper_id = " + OperConfig.CARRIER.AIS_LEGACY.getId() 
                        + wherestatus 
                        + wheretype 
                        + whereuid 
                        + " WHERE 1" 
                        + "   AND ( register_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "')" 
                        + "      OR unregister_date BETWEEN DATE('" + fdate + "') AND DATE('" + tdate + "') )" 
                        + (srvc_id != 0 ? " AND m.srvc_main_id=" + srvc_id : "") 
                        + (oper_id != 0 ? " HAVING OPER_ID=" + oper_id : "") 
                        + orderby 
                        + limit;

                //System.out.println(sql); //nack_debug
                ResultSet rs = cp.execQuery(sql);
                while (rs.next()) {
                    try {
                        Subscriber sub = new Subscriber(rs.getString("msisdn"), rs.getInt("srvc_main_id"), CARRIER.fromId(rs.getInt(3)));
                        if (sub != null) {
                            subList.add(sub);
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
        return subList;
    }
}
