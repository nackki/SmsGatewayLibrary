package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.comparator.MessageDetailReport.MessageDetailSortByDate;
import hippoping.smsgw.api.content.manage.MessageDetail;
import hippoping.smsgw.api.db.RxMoQueue;
import hippoping.smsgw.api.db.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessageDetailReport {

    public static List<MessageDetail> getMessageHistory(String msisdn, int srvc_main_id, int oper_id, int from, int records, String fdate, String tdate) {
        return getMessageHistory(msisdn, srvc_main_id, oper_id, from, records, fdate, tdate, null);
    }

    public static List<MessageDetail> getMessageHistory(String msisdn, int srvc_main_id, int oper_id, int from, int records, String fdate, String tdate, User user) {
        List txqList = TxQueueReport.getMessageHistory(msisdn, srvc_main_id, oper_id, null, from, records, fdate, tdate, user);
        List rxqList = RxMoQueue.getRxQueueList(msisdn, srvc_main_id, oper_id, null, from, records, fdate, tdate, user);

        List msgDetailList = new ArrayList();
        msgDetailList.addAll(txqList);
        msgDetailList.addAll(rxqList);

        Comparator comparator = new MessageDetailSortByDate();
        Collections.sort(msgDetailList, comparator);

        return msgDetailList;
    }
}