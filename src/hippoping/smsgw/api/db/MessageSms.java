package hippoping.smsgw.api.db;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.IllegalFormatCodePointException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;
import lib.common.StringConvert;

public class MessageSms extends Message
        implements MessageInterface {

    private static final Logger log = Logger.getLogger(MessageSms.class.getName());

    private static final String ROOT_DIR = "ext/sms";
    public static final String PICTURE_DIR = "ext/sms/picture";
    public static final String RINGTONE_DIR = "ext/sms/ringtone";
    protected final String RINGTONE_DATA_WRAPPING = "";
    protected final String PICTURE_DATA_WRAPPING = "25F08100480E01";
    protected final String CLIICON_DATA_WRAPPING = "00480E01";
    private final int PORT_ORG_RING_TONE = 0;
    private final int PORT_DST_RING_TONE = 5505;
    private final int PORT_ORG_OPER_LOGO = 0;
    private final int PORT_DST_OPER_LOGO = 5506;
    private final int PORT_ORG_CLI_ICON = 0;
    private final int PORT_DST_CLI_ICON = 5507;
    private final int PORT_ORG_PICT_MESG = 0;
    private final int PORT_DST_PICT_MESG = 5514;
    protected final int PORT_ORG_WAP_PUSH = 9200;
    protected final int PORT_DST_WAP_PUSH = 2948;
    protected String filename;

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public MessageSms() {
    }

    public MessageSms(int sms_mesg_id)
            throws Exception {
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "SELECT *  FROM ctnt_sms_mesg WHERE sms_mesg_id = ? ";

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setInt(1, sms_mesg_id);

                ResultSet rs = cp.execQueryPrepareStatement();
                try {
                    if (rs.next()) {
                        this.content_id = sms_mesg_id;
                        this.sms_info = new MessageSmsInfo(rs.getInt("sms_info_id"));
                        this.disposable = rs.getInt("disposable");
                        setAutoSplitLongMessage(true);
                        setType(Message.SMS_TYPE.values()[rs.getInt("type")]);
                        setCharacterType(Message.CHARACTER_TYPE.fromId(rs.getInt("encoding")));
                        if (this.sms_type == Message.SMS_TYPE.TEXT) {
                            setContent(rs.getString("content"));
                        } else {
                            setContent(rs.getString("content"), this.sms_type);
                        }

                        switch (getType()) {
                            case TEXT:
                            case WAP:
                                break;
                            default:
                                this.filename = rs.getString("content");
                                break;
                        }

                    } else {
                        throw new Exception("sms content[" + sms_mesg_id + "] not found!!");
                    }
                } finally {
                    rs.close();
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

    public void setContent(String content, Message.CHARACTER_TYPE charType) {
        setCharacterType(charType);
        setContent(content);
    }

    public void setContent(String text) {
        text = text.trim();
        int ascii_len = text.length();

        setCharacterType(StringConvert.isEnglishText(text) ? Message.CHARACTER_TYPE.ENGLISH : Message.CHARACTER_TYPE.UNICODE);
        setType(Message.SMS_TYPE.TEXT);

        if (isAutoSplitLongMessage()) {
            int len = 0;
            switch (this.characterType) {
                case ENGLISH:
                case UNICODE:
                    int maxsize;
                    if (this.characterType == Message.CHARACTER_TYPE.ENGLISH) {
                        maxsize = 160;
                    } else {
                        maxsize = 70;
                    }

                    len = text.length();

                    this.message_num = 1;

                    this.content_sub = new String[this.message_num];

                    if (this.characterType == Message.CHARACTER_TYPE.ENGLISH) {
                        this.content_sub[0] = text;
                    } else {
                        String buff = new String();

                        buff = "";
                        for (int i = 0; i < text.length(); i++) {
                            byte hi = (byte) (text.charAt(i) >>> '\b');
                            byte lo = (byte) (text.charAt(i) & 0xFF);
                            int c = hi << 8 | lo;
                            try {
                                buff = String.format("%s%c", new Object[]{buff, Integer.valueOf(c)});
                            } catch (IllegalFormatCodePointException e) {
                                buff = String.format("%s%c", new Object[]{buff, Character.valueOf(text.charAt(i))});
                            }
                        }
                        this.content_sub[0] = buff;
                    }

                    break;
                case HEXADECIMAL:
                default:
                    int ud_len = 268;

                    ud_len = ud_len / 4 * 4;

                    if (ascii_len <= 70) {
                        this.message_num = 1;
                        this.userheader = new String[this.message_num];

                        this.content_sub = new String[this.message_num];

                        this.userheader[0] = new String(String.format("0500030A%02X%02X", new Object[]{Integer.valueOf(this.message_num), Integer.valueOf(1)}));
                        this.content_sub[0] = new String(text);
                    } else {
                        this.message_num = (text.length() / ud_len + (text.length() % ud_len > 0 ? 1 : 0));

                        this.userheader = new String[this.message_num];

                        this.content_sub = new String[this.message_num];

                        int spos = 0;
                        int epos = 0;
                        int index = 0;
                        while (spos < text.length()) {
                            epos = spos + ud_len;
                            if (epos >= text.length()) {
                                epos = text.length();
                            }

                            this.userheader[index] = new String(String.format("0500030A%02X%02X", new Object[]{Integer.valueOf(this.message_num), Integer.valueOf(index + 1)}));
                            this.content_sub[index] = new String(text.substring(spos, epos));

                            index++;

                            spos = epos;
                        }
                    }
                    break;
            }

        } else {
            this.content_sub[0] = new String(text);
            this.message_num = 1;
        }
    }

    public void setContent(String filename, Message.SMS_TYPE type) {
        StringWriter sw = new StringWriter();
        try {
            FileInputStream fstream = new FileInputStream(filename);

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                sw.write(strLine);
            }

            in.close();

            String pdu = new String();

            switch (type) {
                case RINGTONE:
                    //pdu = new rtxconvert().convert(sw.toString());
                    break;
                case PICTURE:
                    //pdu = new bmpconvert().convert(sw.toString());
                    break;
                case OPERLOGO:
                case CLIICON:
                default:
                    throw new Exception("SMS type doesn't supported yet!");
            }

            sw.close();
        } catch (FileNotFoundException e) {
            log.log(Level.SEVERE, "exception caught", e);
        } catch (IOException e) {
            log.log(Level.SEVERE, "exception caught", e);
        } catch (Exception e) {
            log.log(Level.SEVERE, "exception caught", e);
        }
    }

    public int add(String content, int disposable)
            throws Exception {
        return add(content, Message.SMS_TYPE.TEXT, disposable, null, 1);
    }

    public int add(String content, int disposable, int uid)
            throws Exception {
        return add(content, Message.SMS_TYPE.TEXT, disposable, null, uid);
    }

    public int add(String content, Message.SMS_TYPE type, int disposable, File file, int uid)
            throws Exception {
        int id = 0;

        if ((content == null) || (content.trim().isEmpty())) {
            log.warning("blank content isn't allowed");
            return id;
        }

        content = StringConvert.replace(content, "&amp;", "&", true);

        this.sms_type = type;
        MessageSmsInfo info = null;
        switch (type) {
            case TEXT:
                this.encodingType = Message.ENCODING_TYPE.PLAINTEXT;
                if (StringConvert.isEnglishText(content)) {
                    this.characterType = Message.CHARACTER_TYPE.ENGLISH;
                } else {
                    this.characterType = Message.CHARACTER_TYPE.UNICODE;
                }
                break;
            case WAP:
                this.encodingType = Message.ENCODING_TYPE.SMART_MESSAGE;
                this.characterType = Message.CHARACTER_TYPE.HEXADECIMAL;
                break;
            default:
                this.encodingType = Message.ENCODING_TYPE.UNICODE;
                this.characterType = Message.CHARACTER_TYPE.HEXADECIMAL;

                info = new MessageSmsInfo();
                String extension = file.getName().substring(file.getName().indexOf('.') + 1);
                String keyword = file.getName().substring(0, file.getName().indexOf('.') - 1);
                info.setCategory((type == Message.SMS_TYPE.PICTURE ? "image/" : "audio/") + extension);
                info.setTitle(file.getName());
                info.setKeywords(keyword);
                info.setIssue_date(new Date());

                MessageSmsInfo.add(info);
        }

        String sql_ctnt_sms_mesg = "INSERT INTO ctnt_sms_mesg (sms_info_id, TYPE, content,disposable, encoding, owner) VALUES(" + (info == null ? "null" : Integer.valueOf(info.getSms_info_id())) + ", ?, ?, ?, ?, ?)";
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                cp.prepareStatement(sql_ctnt_sms_mesg, 1);
                cp.getPreparedStatement().setInt(1, type.getId());
                cp.getPreparedStatement().setString(2, content);
                cp.getPreparedStatement().setInt(3, disposable);
                cp.getPreparedStatement().setInt(4, this.characterType.getId());
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
                log.log(Level.SEVERE, "SQL error!!", e);
            } finally {
                cp.release();
            }
        } catch (Exception e) {
        }
        return id;
    }

    public boolean isContentBlank(int id) {
        boolean blank = false;

        String sql = "SELECT LENGTH(content) as len FROM ctnt_sms_mesg  WHERE sms_mesg_id=?";
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

    public int remove() throws Exception {
        int rows = 0;

        switch (getType()) {
            case TEXT:
            case WAP:
                break;
            default:
                File file = new File(getFilename());
                if ((file != null) && (file.exists())
                        && (file.delete())) {
                    log.log(Level.SEVERE, "file deleted");
                }

                MessageSmsInfo info = getMessageInfo();
                if (info != null) {
                    log.log(Level.INFO, "Remove sms info " + info.remove() + " row(s)");
                }
                break;
        }
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "DELETE FROM ctnt_sms_mesg WHERE sms_mesg_id=" + getContent_id();

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

        switch (getType()) {
            case TEXT:
            case WAP:
                break;
        }

        String content = "";
        switch (this.sms_type) {
            case TEXT:
                content = getContents();
                break;
            default:
                content = this.filename;
        }
        try {
            DBPoolManager cp = new DBPoolManager();
            try {
                String sql = "   UPDATE ctnt_sms_mesg    SET content=?      , type=?      , disposable=?      , encoding=?  WHERE sms_mesg_id=" + getContent_id();

                cp.prepareStatement(sql);
                cp.getPreparedStatement().setString(1, content);
                cp.getPreparedStatement().setInt(2, getSmsType().getId());
                cp.getPreparedStatement().setInt(3, getDisposable());
                cp.getPreparedStatement().setInt(4, getEncodingType().getId());
                rows = cp.execUpdatePrepareStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                cp.release();
            }
        } catch (Exception e) {
            throw e;
        }

        return rows;
    }
}
