
public class CallButton extends Button {
    public Elevator elevator;
    public Channel myChannel;

    public CallButton(int floor, Elevator elevator, Channel myChannel) {
        super(floor);
        this.elevator = elevator;
        this.myChannel = myChannel;
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(2);

                Request req = myChannel.receive();
                Simulation.print("Call Button " + floor + " pressed");

                if (req != null && !req.type.equals(Request.CALL_BUTTON)) {
                    throw new RuntimeException("Received an unexpected request");
                }

                elevator.addFloorRequest(super.floor);
            } catch (InterruptedException e) {
                System.out.println("Call Button " + floor + " terminating");
                break;
            }
        }

    }
}
