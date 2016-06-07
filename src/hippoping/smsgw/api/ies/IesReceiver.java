package hippoping.smsgw.api.ies;

import com.nation.ies.request.Newslist;
import hippoping.smsgw.api.db.DroConfigure;
import hippoping.smsgw.api.db.MessageSms;
import hippoping.smsgw.api.db.MessageWap;
import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.ServiceContentAction;
import hippoping.smsgw.api.db.ServiceElement;
import hippoping.smsgw.api.db.TxQueue;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.StringConvert;

public class IesReceiver {

    private static final Logger log = Logger.getLogger(IesReceiver.class.getClass().getName());

    public int insertRecord(Newslist.News news) throws Exception {
        int row = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "INSERT INTO trns_ctnt_mngr  VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, ((Newslist.News.NewsId) news.getNewsId().get(0)).getValue());
                cp.getPreparedStatement().setString(2, news.getTopic());
                cp.getPreparedStatement().setString(3, news.getValidDate());
                cp.getPreparedStatement().setString(4, news.getUpdateDate());
                cp.getPreparedStatement().setString(5, news.getExpireDate());
                cp.getPreparedStatement().setString(6, news.getPublisher());
                cp.getPreparedStatement().setString(7, news.getCategory());
                cp.getPreparedStatement().setString(8, news.getType());
                cp.getPreparedStatement().setString(9, news.getOrg());
                cp.getPreparedStatement().setString(10, news.getEdition());
                cp.getPreparedStatement().setString(11, news.getSection());
                cp.getPreparedStatement().setString(12, news.getColsection());
                cp.getPreparedStatement().setString(13, news.getSubcolumn());
                cp.getPreparedStatement().setString(14, news.getRanking());
                cp.getPreparedStatement().setString(15, news.getPriority());
                cp.getPreparedStatement().setInt(16, Integer.valueOf(news.getPage()).intValue());
                cp.getPreparedStatement().setString(17, news.getPageCode());
                cp.getPreparedStatement().setString(18, news.getDbCode());
                cp.getPreparedStatement().setString(19, news.getSttsCode());
                cp.getPreparedStatement().setString(20, news.getLanguage());
                cp.getPreparedStatement().setString(21, news.getAuthor());
                cp.getPreparedStatement().setString(22, news.getContent());
                cp.getPreparedStatement().setString(23, news.getSummary());
                cp.getPreparedStatement().setString(24, news.getContentSummary());
                cp.getPreparedStatement().setString(25, news.getUrl());
                cp.getPreparedStatement().setString(26, news.getNote());
                cp.getPreparedStatement().setString(27, news.getPicturelist());

                row = cp.execUpdatePrepareStatement();
                log.log(Level.INFO, "{0} row(s) inserted.", Integer.valueOf(row));
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return row;
    }

    private int insertContent(String text, int language)
            throws Exception {
        int ctnt_id = new MessageSms().add(text, 1, 1);

        return ctnt_id;
    }

    private int insertContent(String url, String url_jad, String title)
            throws Exception {
        int ctnt_id = new MessageWap().add(title, url, url_jad, 1, 1);

        return ctnt_id;
    }

    public void serviceMap(Newslist.News news)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "  SELECT cm.srvc_main_id"
                        + "     , cm.oper_id"
                        + "  FROM ctnt_mngr_map cm"
                        + " INNER JOIN srvc_sub s"
                        + "    ON cm.srvc_main_id = s.srvc_main_id"
                        + "   AND s.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + "   AND cm.oper_id = s.oper_id"
                        + " WHERE cm.db_code = ?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, news.getDbCode());
                ResultSet rs = cp.execQueryPrepareStatement();

                List serviceList = new ArrayList();
                try {
                    log.log(Level.INFO, "Provisioning list:");
                    while (rs.next()) {
                        serviceList.add(new ServiceElement(rs.getInt("srvc_main_id"),
                                rs.getInt("oper_id"),
                                ServiceElement.SERVICE_TYPE.ALL.getId(),
                                ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId()));
                        log.log(Level.INFO, "{0} <-> srvc_main_id[{1}]", new Object[]{OperConfig.CARRIER.fromId(rs.getInt("oper_id")).name(), Integer.valueOf(rs.getInt("srvc_main_id"))});
                    }
                } finally {
                    rs.close();
                }

                ServiceContentAction.ACTION_TYPE type;
                switch (news.getType().toUpperCase().trim()) {
                    case "WAP":
                        type = ServiceContentAction.ACTION_TYPE.WAP;
                        break;
                    case "MMS":
                        type = ServiceContentAction.ACTION_TYPE.MMS;
                        break;
                    default:
                        type = ServiceContentAction.ACTION_TYPE.SMS;
                        break;
                }

                int ctnt_id;
                switch (type) {
                    case WAP:
                        ctnt_id = insertContent(news.getUrl(), "", news.getNote());
                        break;
                    case MMS:
                        throw new Exception("MMS doesn't supported yet!!");
                    case SMS:
                    default:
                        ctnt_id = insertContent(news.getContent(), StringConvert.isEnglishText(news.getContent()) ? 0 : 1);
                }

                if (ctnt_id == 0) {
                    throw new Exception("insert new content failed!!");
                }

                ServiceContentAction action = new ServiceContentAction();
                action.action_type = type;
                action.contentId = ctnt_id;

                for (int i = 0; i < serviceList.size(); i++) {
                    DroConfigure dro = new DroConfigure((ServiceElement) serviceList.get(i));

                    long txq_id = new TxQueue().add(action,
                            ((ServiceElement) serviceList.get(i)).srvc_main_id,
                            ((ServiceElement) serviceList.get(i)).oper_id,
                            null,
                            ((ServiceElement) serviceList.get(i)).chrg_flg,
                            null,
                            news.getValidDate(),
                            dro.getBroadcast(),
                            ((ServiceElement) serviceList.get(i)).priority,
                            0);

                    if (txq_id <= 0) {
                        throw new Exception("insert new tx_queue failed!!");
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void serviceMap(String dbCode, ServiceContentAction sca, String deliver)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql =
                        "  SELECT cm.srvc_main_id"
                        + "     , cm.oper_id"
                        + "  FROM ctnt_mngr_map cm"
                        + " INNER JOIN srvc_sub s"
                        + "    ON cm.srvc_main_id = s.srvc_main_id"
                        + "   AND s.status != " + ServiceElement.SERVICE_STATUS.OFF.getDbId()
                        + "   AND cm.oper_id = s.oper_id"
                        + " WHERE cm.db_code = ?";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, dbCode);
                ResultSet rs = cp.execQueryPrepareStatement();

                List serviceList = new ArrayList();
                try {
                    log.log(Level.INFO, "Provisioning list:");
                    while (rs.next()) {
                        serviceList.add(new ServiceElement(rs.getInt("srvc_main_id"), rs.getInt("oper_id"),
                                ServiceElement.SERVICE_TYPE.ALL.getId(),
                                ServiceElement.SERVICE_STATUS.ON.getId() | ServiceElement.SERVICE_STATUS.TEST.getId()));
                        log.log(Level.INFO, "{0} <-> srvc_main_id[{1}]",
                                new Object[]{OperConfig.CARRIER.fromId(rs.getInt("oper_id")).name(), Integer.valueOf(rs.getInt("srvc_main_id"))});
                    }
                } finally {
                    rs.close();
                }

                for (int i = 0; i < serviceList.size(); i++) {
                    // use service priority instead
//                    TxQueue.TX_TYPE piority = TxQueue.TX_TYPE.CONTENT2;
//                    if (((ServiceElement) serviceList.get(i)).chrg_flg != null) {
//                        piority = ((ServiceElement) serviceList.get(i)).chrg_flg.equals("MT") ? TxQueue.TX_TYPE.CHARGE_CONTENT2 : TxQueue.TX_TYPE.CONTENT2;
//                    }
                    ServiceElement se = (ServiceElement) serviceList.get(i);
                    DroConfigure dro = new DroConfigure((ServiceElement) serviceList.get(i));

                    long txq_id = new TxQueue().add(sca,
                            se.srvc_main_id,
                            se.oper_id,
                            null,
                            se.chrg_flg,
                            null,
                            deliver,
                            dro.getBroadcast(),
                            //piority.getId(),
                            se.priority,
                            0);

                    if (txq_id <= 0) {
                        throw new Exception("insert new tx_queue failed!!");
                    }
                }
            } catch (SQLException e) {
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }
}