

public class Request {
    public static final String DOOR_OPEN = "DOOR_OPEN";
    public static final String DOOR_CLOSE = "DOOR_CLOSE";
    public static final String COME_TO_FLOOR = "COME_TO_FLOOR";
    public static final String CALL_BUTTON = "CALL_BUTTON";

    public int floor;
    public String type;
    public int passenger_id;

    public Request(String type, int floor) {
        this.floor = floor;
        this.type = type;
    }

    public Request(String type, int floor, int pass_id) {
        this.floor = floor;
        this.type = type;
        this.passenger_id = pass_id;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Request) {
            return floor == ((Request)o).floor && type.equals(((Request)o).type);
        }
        return false;
    }
}
