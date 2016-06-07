/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import lib.common.DBPoolManager;

/**
 *
 * @author nack_ki
 */
public class ServiceCharge {

    public enum SRVC_CHRG {

        PER_MESSAGE(0),
        PER_DAY(1),
        PER_WEEK(2),
        PER_MONTH(3);
        private final int id;

        SRVC_CHRG(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static SRVC_CHRG fromId(final int id) {
            for (SRVC_CHRG e : SRVC_CHRG.values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            String type = "";

            try {
                DBPoolManager cp = new DBPoolManager();

                // insert new member record
                String sql = "SELECT chrg_desc "
                        + " FROM srvc_chrg_type"
                        + " WHERE srvc_chrg_type_id=?";

                try {
                    cp.prepareStatement(sql);
                    cp.getPreparedStatement().setInt(1, this.id);
                    ResultSet rs = cp.execQueryPrepareStatement();
                    if (rs.next()) {
                        type = rs.getString(1);
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

            return type;
        }
    }

    /**
     * Get charge day interval by service main ID
     * @param srvc_main_id
     * @return value of day
     */
    public static int getChargeInterval(int srvc_main_id) {
        int day = 0;// get connection pool

        try {
            int charge_type = 0;
            int charge_amount = 0;

            DBPoolManager cp = new DBPoolManager();

            // insert new member record
            String sql = "SELECT srvc_chrg_type_id, srvc_chrg_amnt "
                    + " FROM srvc_main"
                    + " WHERE srvc_main_id=?";

            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, srvc_main_id);
                ResultSet rs = cp.execQueryPrepareStatement();
                if (rs.next()) {
                    charge_type = rs.getInt("srvc_chrg_type_id");
                    charge_amount = rs.getInt("srvc_chrg_amnt");
                }
                rs.close();

                switch (charge_type) {
                    case 1:
                        day = charge_amount;
                        break;
                    case 2:
                        day = (charge_amount * 7);
                        break;
                    case 3:
                        day = (charge_amount * 30);
                        break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            return day;
        }

        return day;
    }
}
