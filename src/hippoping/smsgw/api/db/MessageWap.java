/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hippoping.smsgw.api.db;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.StringConvert;

/**
 *
 * @author nack_ki
 */
public class MessageWap extends Message implements MessageInterface {

    // Smart Message port declaration
    protected final int PORT_ORG_WAP_PUSH = 0x23F0;
    protected final int PORT_DST_WAP_PUSH = 0x0B84;
    public String url;
    public String title;

    public MessageWap() {
    }

    public MessageWap(int wap_push_id)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT *"
                        + "  FROM ctnt_sms_wap"
                        + " WHERE wap_push_id = ? ";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, wap_push_id);

                ResultSet rs = cp.execQueryPrepareStatement();
                try {
                    if (rs.next()) {
                        this.content_id = wap_push_id;
                        this.sms_info = null;
                        setType(Message.SMS_TYPE.WAP);
                        this.characterType = Message.CHARACTER_TYPE.HEXADECIMAL;
                        this.disposable = rs.getInt("disposable");
                        setAutoSplitLongMessage(true);

                        URL u = new URL(rs.getString("url_jar"));

                        String protocol = u.getProtocol();
                        String host = u.getHost();
                        int port = u.getPort();
                        String file = u.getFile();
                        String ref = u.getRef();

                        String new_url = host + (port != -1 ? ":" + port : "") + (file != null ? file : "") + (ref != null ? ref : "");

                        setContent(new_url, rs.getString("title"));
                        this.url = (protocol + "://" + new_url);
                        this.title = rs.getString("title");
                    } else {
                        throw new Exception("wap content[" + wap_push_id + "] not found!!");
                    }
                } finally {
                    rs.close();
                }
            } catch (MalformedURLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "error on URL parse", e);
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void setContent(String url, String title) {
        try {
            String url_encoded = URLEncoder.encode(url, "UTF-8");
            String url_hex = StringConvert.UTF2HexString(url_encoded);
            String title_hex = StringConvert.UTF2HexString(title);
            String end_wrapper = new String(String.format("000103%s000101", new Object[]{title_hex}));

            int total_len = 14 + "DC0601AE02056A0045C60C03".length() + url_hex.length() + end_wrapper.length();

            if (total_len <= 280) {
                String wsp = String.format("060504%04X%04X", new Object[]{Integer.valueOf(2948), Integer.valueOf(9200)});

                this.userheader = new String[1];
                this.content_sub = new String[1];

                this.userheader[0] = wsp;
                this.content_sub[0] = ("DC0601AE02056A0045C60C03" + url_hex + end_wrapper);
                this.message_num = 1;
            } else {
                int data_len = url_hex.length() + end_wrapper.length();
                if (data_len % 4 > 0) {
                    data_len += 4 - data_len % 4;
                }

                String userdata = url_hex + end_wrapper;

                int frst_ud_len = 256 - "DC0601AE02056A0045C60C03".length();

                int ud_len = 256;
                this.message_num = ((data_len - frst_ud_len) / ud_len + ((data_len - frst_ud_len) % ud_len > 0 ? 1 : 0) + 1);

                this.userheader = new String[this.message_num];

                this.content_sub = new String[this.message_num];

                int spos = 0;
                int epos = 0;
                int index = 0;

                String wsp = String.format("0B0504%04X%04X00030A%02X%02X", new Object[]{Integer.valueOf(2948), Integer.valueOf(9200), Integer.valueOf(this.message_num), Integer.valueOf(index + 1)});

                epos = spos + frst_ud_len;

                this.userheader[index] = wsp;
                this.content_sub[index] = ("DC0601AE02056A0045C60C03" + new String(userdata.substring(spos, epos)));

                index++;

                spos = epos;

                while (spos < userdata.length()) {
                    epos = spos + ud_len;
                    if (epos >= userdata.length()) {
                        epos = userdata.length();
                    }

                    wsp = new String(String.format("0B0504%04X%04X00030A%02X%02X", new Object[]{Integer.valueOf(2948), Integer.valueOf(9200), Integer.valueOf(this.message_num), Integer.valueOf(index + 1)}));

                    this.userheader[index] = wsp;
                    this.content_sub[index] = new String(userdata.substring(spos, epos));

                    index++;

                    spos = epos;
                }
            }
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", e);
        }
    }

    public int add(String content, Message.SMS_TYPE type, int disposable, File file, int uid) throws Exception {
        return -1;
    }

    public int add(String title, String jar, String jad, int disposable, int uid) {
        int id = -1;

        if (jar != null) {
            jar = StringConvert.replace(jar, "&amp;", "&", true);
        }

        if (jad != null) {
            jad = StringConvert.replace(jad, "&amp;", "&", true);
        }

        if (title != null) {
            title = StringConvert.replace(title, "&amp;", "&", true);
        }

        String sql_ctnt_sms_mesg = "INSERT INTO ctnt_sms_wap (wap_push_id, disposable, url_jar, url_jad, title, owner) VALUES(null, ?, ?, ?, ?, ?)";

        this.sms_type = Message.SMS_TYPE.WAP;
        this.encodingType = Message.ENCODING_TYPE.PLAINTEXT;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql_ctnt_sms_mesg, 1);
                cp.getPreparedStatement().setInt(1, disposable);
                cp.getPreparedStatement().setString(2, jar);
                cp.getPreparedStatement().setString(3, jad);
                cp.getPreparedStatement().setString(4, title);
                cp.getPreparedStatement().setInt(5, uid);

                int row = cp.execUpdatePrepareStatement();
                if (row == 1) {
                    ResultSet rs = cp.getPreparedStatement().getGeneratedKeys();
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }

                    rs.close();
                }
            } catch (SQLException e) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return id;
    }

    public int remove() throws Exception {
        int rows = 0;

        switch (getType()) {
            case TEXT:
            case WAP:
                break;
        }

        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM ctnt_sms_wap WHERE wap_push_id=" + getContent_id();

                rows = cp.execUpdate(sql);
            } catch (SQLException e) {
                throw e;
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }

    public int sync() throws Exception {
        int rows = 0;
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE ctnt_sms_wap    SET disposable=?      , url_jar=?      , title=?  WHERE wap_push_id=" + getContent_id();

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, getDisposable());
                cp.getPreparedStatement().setString(2, this.url);
                cp.getPreparedStatement().setString(3, this.title);
                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        switch (getType()) {
            case TEXT:
            case WAP:
                break;
        }

        return rows;
    }

    public boolean isContentBlank(int id) {
        boolean blank = false;

        String sql = "SELECT LENGTH(url_jar) as len FROM ctnt_sms_wap  WHERE wap_push_id=?";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, id);

                ResultSet rs = cp.execQueryPrepareStatement();
                if ((rs.next())
                        && (rs.getInt("len") == 0)) {
                    blank = true;
                }

                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return blank;
    }
}
