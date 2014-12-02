import java.util.concurrent.atomic.AtomicBoolean;

public class Passenger extends Thread {
    private AtomicBoolean isInElevator;
    private int startFloor;
    private int destinationFloor;
    private int id;
    private Channel callButton0Channel;
    private Channel callButton1Channel;
    private Channel callButton2Channel;

    private Channel destButton0Channel;
    private Channel destButton1Channel;
    private Channel destButton2Channel;

    private Elevator elevator;

    private int t;

    public Passenger(int id, int t, Elevator elevator, Channel callButton0Channel, Channel callButton1Channel, Channel callButton2Channel,
                     Channel destButton0Channel, Channel destButton1Channel, Channel destButton2Channel) {
        this.t = t;
        this.id = id;
        this.isInElevator = new AtomicBoolean(false);
        this.startFloor = getRandomFloor();
        destinationFloor = startFloor;
        while (destinationFloor == startFloor) {
            destinationFloor = getRandomFloor();
        }
        this.elevator = elevator;
        this.callButton0Channel = callButton0Channel;
        this.callButton1Channel = callButton1Channel;
        this.callButton2Channel = callButton2Channel;

        this.destButton0Channel = destButton0Channel;
        this.destButton1Channel = destButton1Channel;
        this.destButton2Channel = destButton2Channel;

    }

    @Override
    public void run() {
        try {
            Simulation.print("Passenger " + id + "\tstart floor " + startFloor + "\tdest floor " + destinationFloor);
            for (int i = 0; i < t; i++) {
                //press button
                Request request = new Request(Request.CALL_BUTTON, startFloor, id);
                Simulation.print("Passenger " + id + " pressed call button " + startFloor);

                switch (startFloor) {
                    case 0:
                        callButton0Channel.send(request);
                        break;
                    case 1:
                        callButton1Channel.send(request);
                        break;
                    case 2:
                        callButton2Channel.send(request);
                        break;
                    default:
                        throw new RuntimeException("Encountered a floor that wasn't 0, 1, or 2");
                }
                elevator.addEnterRequest(startFloor, this);

                //wait for door to open
                while (!isInElevator.get()) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //enter elevator

                //press destination button
                Request destRequest = new Request(Request.CALL_BUTTON, destinationFloor, id);
                Simulation.print("Passenger " + id + " pressed destination button " + destinationFloor);
                switch (destinationFloor) {
                    case 0:
                        destButton0Channel.send(destRequest);
                        break;
                    case 1:
                        destButton1Channel.send(destRequest);
                        break;
                    case 2:
                        destButton2Channel.send(destRequest);
                        break;
                    default:
                        throw new RuntimeException("Encountered a floor that wasn't 0, 1, or 2");
                }
                elevator.addLeaveRequest(destinationFloor, this);

                //wait for elevator to get to destination floor
                while (isInElevator.get()) {
                }

                //leave elevator
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    public int getRandomFloor() {
        return (int) (Math.random() * 3);
    }

    public void enterElevator() throws InterruptedException {
        isInElevator.set(true);
        Simulation.print("Passenger " + id + " entering elevator on floor " + startFloor);
    }

    public void exitElevator() throws InterruptedException {
        isInElevator.set(false);
        Simulation.print("Passenger " + id + " leaving elevator on floor " + destinationFloor);
        chooseNewDestinationFloor();
    }
    public void chooseNewDestinationFloor() throws InterruptedException {
        startFloor = destinationFloor;
        while (destinationFloor == startFloor) {
            destinationFloor = getRandomFloor();
        }
        Simulation.print("Passenger " + id + " chose new dest floor " + destinationFloor);
    }
}
