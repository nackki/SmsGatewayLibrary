package hippoping.smsgw.api.operator.ais;

public class TypeLMessageHeaderType {

    public static enum SSS_MSG_TYPE {

        BROADCAST(0),
        WARNING(7),
        RECURRING(8),
        REGISTER(10),
        UNREGISTER(11);
        private final int id;

        private SSS_MSG_TYPE(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static SSS_MSG_TYPE fromId(int id) {
            for (SSS_MSG_TYPE e : values()) {
                if (e.id == id) {
                    return e;
                }
            }
            return null;
        }
    }
}