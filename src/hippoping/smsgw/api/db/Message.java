package hippoping.smsgw.api.db;

import hippoping.smsgw.api.db.ServiceContentAction.ACTION_TYPE;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib.common.DBPoolManager;

public class Message implements Serializable {

    protected final int MAX_CONCAT_MESG_SIZE = 3;
    protected final int MAX_ENG_MESG_LEN = 160;
    protected final int MAX_TH_MESG_LEN = 70;
    protected final int MAX_HEX_MESG_LEN = 280;
    protected final int ALL_PLAN_TEXT_UH_LEN = 12;
    protected final int SNGL_SMRT_MESG_UH_LEN = 14;
    protected final int LONG_SMRT_MESG_UH_LEN = 24;
    protected final String ALL_PLAN_TEXT_UH = "0500030A%02X%02X";
    protected final String SNGL_SMRT_MESG_UH = "060504%04X%04X";
    protected final String LONG_SMRT_MESG_UH = "0B0504%04X%04X00030A%02X%02X";
    protected final String DATA_WRAPPING = "DC0601AE02056A0045C60C03";
    protected final String END_WRAPPER = "000103%s000101";
    public boolean longMessage = false;
    protected CHARACTER_TYPE characterType;
    protected ENCODING_TYPE encodingType;
    protected String[] userheader;
    protected String[] content_sub;
    protected int message_num = 0;
    protected boolean autoSplitLongMessage = true;
    protected MessageSmsInfo sms_info;
    protected SMS_TYPE sms_type;
    protected int disposable;
    protected int content_id;
    protected ServiceContentAction.ACTION_TYPE contentType;
    protected Object _instance;

    public int getContent_id() {
        return this.content_id;
    }

    public CHARACTER_TYPE getCharacterType() {
        return this.characterType;
    }

    public ENCODING_TYPE getEncodingType() {
        return this.encodingType;
    }

    public SMS_TYPE getSmsType() {
        return this.sms_type;
    }

    public String[] getUserHeader() {
        return this.userheader;
    }

    public String[] getContentSub() {
        return this.content_sub;
    }

    public void setContentSub(String str, int index) {
        this.content_sub[index] = str;
    }

    public Object getInstance() {
        return this._instance;
    }

    public int getMessage_num() {
        return this.message_num;
    }

    public void setMessage_num(int num) {
        this.message_num = num;
    }

    public Message() {
    }

    public Message(long tx_queue_id)
            throws Exception {
        DBPoolManager cp = new DBPoolManager();
        try {
            String sql = 
                    "SELECT q.ctnt_id"
                    + "     , q.ctnt_type"
                    + "     , q.srvc_main_id"
                    + "     , q.tx_queue_id AS id"
                    + "  FROM trns_tx_queue AS q"
                    + " WHERE q.tx_queue_id = ?";

            cp.prepareStatement(sql);
            cp.getPreparedStatement().setLong(1, tx_queue_id);
            ResultSet rs = cp.execQueryPrepareStatement();
            if (rs.next()) {
                int content_type = rs.getInt("ctnt_type");
                int id = rs.getInt("ctnt_id");

                switch (ACTION_TYPE.fromId(content_type)) {
                    case SMS:
                        _instance = (MessageSms) new MessageSms(id);
                        break;
                    case WAP:
                        _instance = (MessageWap) new MessageWap(id);
                        break;
                    case MMS:
                        _instance = (MessageMms) new MessageMms(id);
                        break;
                    case FORWARD:
                        //Logger.getLogger(getClass().getName()).log(Level.INFO, "inquiry FORWARD[" + content_id + "]<->tx_queue_id[" + tx_queue_id + "]");
                        break;
                }

                setContentType(ServiceContentAction.ACTION_TYPE.fromId(content_type));
                Logger.getLogger(getClass().getName()).log(Level.INFO, "set content type:" + getContentType().toString());
                if (this._instance != null) {
                    ((Message) this._instance).setContentType(ServiceContentAction.ACTION_TYPE.fromId(content_type));

                    this.userheader = ((Message) this._instance).getUserHeader();
                    this.content_sub = ((Message) this._instance).getContentSub();
                    this.message_num = ((Message) this._instance).getMessage_num();
                    this.sms_type = ((Message) this._instance).getSmsType();
                    this.characterType = ((Message) this._instance).getCharacterType();
                    this.encodingType = ((Message) this._instance).getEncodingType();
                }
            } else {
                throw new Exception("tx_queue_id not found!!");
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cp.release();
        }
    }

    public ServiceContentAction.ACTION_TYPE getContentType() {
        return this.contentType;
    }

    public void setContentType(ServiceContentAction.ACTION_TYPE contentType) {
        this.contentType = contentType;
    }

    public int getDisposable() {
        return this.disposable;
    }

    public String[] getContent() {
        return this.content_sub;
    }

    public String getContents() {
        StringWriter sw = new StringWriter();
        for (String token : this.content_sub) {
            sw.append(token);
        }
        return sw.toString();
    }

    @Override
    public String toString() {
        return getContents();
    }

    public SMS_TYPE getType() {
        return this.sms_type;
    }

    public void setType(SMS_TYPE type) {
        this.sms_type = type;

        switch (this.sms_type) {
            case TEXT:
                this.encodingType = ENCODING_TYPE.PLAINTEXT;
                break;

            case RINGTONE:
            case OPERLOGO:
            case CLIICON:
            case PICTURE:
                this.characterType = CHARACTER_TYPE.HEXADECIMAL;
                this.encodingType = ENCODING_TYPE.UNICODE;
                break;

            case WAP:
                this.characterType = CHARACTER_TYPE.HEXADECIMAL;
                this.encodingType = ENCODING_TYPE.SMART_MESSAGE;
        }
    }

    public MessageSmsInfo getMessageInfo() {
        return this.sms_info;
    }

    public boolean isAutoSplitLongMessage() {
        return this.autoSplitLongMessage;
    }

    public void setAutoSplitLongMessage(boolean autoSplitLongMessage) {
        this.autoSplitLongMessage = autoSplitLongMessage;
    }

    public void setCharacterType(CHARACTER_TYPE charType) {
        this.characterType = charType;

        if ((this.characterType == CHARACTER_TYPE.ENGLISH) || (this.characterType == CHARACTER_TYPE.UNICODE)) {
            setEncodingType(ENCODING_TYPE.PLAINTEXT);
        }
    }

    public void setEncodingType(ENCODING_TYPE encodingType) {
        this.encodingType = encodingType;
    }

    public static enum ENCODING_TYPE {

        PLAINTEXT(0),
        UNICODE(1),
        SMART_MESSAGE(2);
        private final int id;

        private ENCODING_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static ENCODING_TYPE fromId(int id) {
            for (ENCODING_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }

    public static enum CHARACTER_TYPE {

        ENGLISH(0),
        UNICODE(1), // Universal encoding e.g. Thai, Chinese
        HEXADECIMAL(2);
        private final int id;

        private CHARACTER_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static CHARACTER_TYPE fromId(int id) {
            for (CHARACTER_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }

    public static enum SMS_TYPE {

        TEXT(0),
        RINGTONE(1),
        OPERLOGO(2),
        CLIICON(3),
        PICTURE(4),
        WAP(5);
        private final int id;

        private SMS_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static SMS_TYPE fromId(int id) {
            for (SMS_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}