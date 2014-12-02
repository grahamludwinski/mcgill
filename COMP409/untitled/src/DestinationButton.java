public class DestinationButton extends Button {
    public Elevator elevator;
    public Channel myChannel;

    public DestinationButton(int floor) {
        super(floor);
    }

    public DestinationButton(int floor, Elevator elevator, Channel myChannel) {
        super(floor);
        this.elevator = elevator;
        this.myChannel = myChannel;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (Thread.interrupted()) {
                    System.out.println("Destination Button " + floor + " terminating");
                    break;
                } else {
                    Request req = myChannel.receive();

                    Simulation.print("Destination Button " + floor + " pressed");
                    if (req != null && !req.type.equals(Request.CALL_BUTTON)) {
                        throw new RuntimeException("Received an unexpected request");
                    }

                    elevator.addFloorRequest(super.floor);
                }
            } catch (InterruptedException e) {
                return;
            }
        }

    }
}
