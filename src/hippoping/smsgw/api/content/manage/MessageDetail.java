package hippoping.smsgw.api.content.manage;

import java.util.Date;

public abstract interface MessageDetail {

    public abstract Date getTimestamp();

    public abstract String getContent_Type();

    public abstract long getId();
}