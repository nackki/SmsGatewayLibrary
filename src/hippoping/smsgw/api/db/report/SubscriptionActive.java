package hippoping.smsgw.api.db.report;

import hippoping.smsgw.api.db.OperConfig.CARRIER;
import hippoping.smsgw.api.db.ServiceElement;

public class SubscriptionActive {

    protected ServiceElement service;
    protected long[] amount_active;
    protected long[] amount_inactive;
    private static final CARRIER[] ALL_OPER 
            = {CARRIER.DTAC, CARRIER.TRUE, CARRIER.AIS_LEGACY, CARRIER.AIS, CARRIER.TRUEH, CARRIER.DTAC_SDP, CARRIER.CAT};

    public SubscriptionActive(ServiceElement service) {
        this.service = service;

        this.amount_active = new long[ALL_OPER.length];
        this.amount_inactive = new long[ALL_OPER.length];

        for (CARRIER oper : ALL_OPER) {
            this.amount_active[oper.getId()] = 0L;
            this.amount_inactive[oper.getId()] = 0L;
        }
    }

    public ServiceElement getService() {
        return this.service;
    }

    public void setService(ServiceElement service) {
        this.service = service;
    }

    public long getAmountActive(CARRIER oper) {
        return this.amount_active[oper.getId()];
    }

    public void setAmountActive(CARRIER oper, long amount) {
        this.amount_active[oper.getId()] = amount;
    }

    public long getAmountInactive(CARRIER oper) {
        return this.amount_inactive[oper.getId()];
    }

    public void setAmountInactive(CARRIER oper, long amount) {
        this.amount_inactive[oper.getId()] = amount;
    }

    public static enum STATUS {

        INACTIVE(0),
        ACTIVE(1);
        private final int id;

        private STATUS(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static STATUS fromId(int id) {
            for (STATUS e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}