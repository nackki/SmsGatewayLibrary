/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.operator.cat_cpg;

import hippoping.smsgw.api.db.OperConfig;
import hippoping.smsgw.api.db.Subscriber;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

/**
 *
 * @author nacks_mcair
 */
public class CatSubscriber extends Subscriber {

    private static final Logger log = Logger.getLogger(CatSubscriber.class.getName());
    private String extr_id;

    public String getExtr_id() {
        return extr_id;
    }

    public void setExtr_id(String extr_id) {
        this.extr_id = extr_id;
    }

    public CatSubscriber(String msisdn, int srvc_main_id, OperConfig.CARRIER oper) throws Exception {
        super(msisdn, srvc_main_id, oper);

        DBPoolManager cp = new DBPoolManager();

        String sql
                = "SELECT extr_id"
                + "  FROM mmbr_" + oper.toString().toLowerCase()
                + " WHERE srvc_main_id=?"
                + "   AND msisdn=?";
        try {
            cp.prepareStatement(sql);
            cp.getPreparedStatement().setInt(1, srvc_main_id);
            cp.getPreparedStatement().setString(2, msisdn);
            ResultSet rs = cp.execQueryPrepareStatement();
            try {
                if (rs.next()) {
                    this.extr_id = rs.getString(1);
                } else {
                    throw new Exception("subscriber not found");
                }
            } finally {
                rs.close();
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            throw e;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Subscriber [{0},{1}, srvc_main_id:{2}] not found!!", new Object[]{msisdn, oper.name(), srvc_main_id});

            throw e;
        } finally {
            cp.release();
        }
    }

    public int syncExtrId()
            throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE mmbr_" + OperConfig.CARRIER.fromId(this.oper_id).toString().toLowerCase()
                        + "    SET extr_id=?"
                        + "  WHERE msisdn="
                        + this.msisdn
                        + "    AND srvc_main_id=" + this.srvc_main_id;

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, this.extr_id);
                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                log.severe(e.getMessage());
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

}
