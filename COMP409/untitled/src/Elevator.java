import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Elevator extends Thread {
    private HashMap<Integer, ArrayList<Passenger>> passengerLeaveRequests;
    private HashMap<Integer, ArrayList<Passenger>> passengerEnterRequests;
    private Queue<Integer> floorRequests;
    private int currentFloor;

    private Door door0;
    private Door door1;
    private Door door2;

    public Elevator(Door door0, Door door1, Door door2) {
        passengerLeaveRequests = new HashMap<Integer, ArrayList<Passenger>>();
        passengerLeaveRequests.put(0, new ArrayList<Passenger>());
        passengerLeaveRequests.put(1, new ArrayList<Passenger>());
        passengerLeaveRequests.put(2, new ArrayList<Passenger>());

        passengerEnterRequests = new HashMap<Integer, ArrayList<Passenger>>();
        passengerEnterRequests.put(0, new ArrayList<Passenger>());
        passengerEnterRequests.put(1, new ArrayList<Passenger>());
        passengerEnterRequests.put(2, new ArrayList<Passenger>());

        floorRequests = new ConcurrentLinkedQueue<Integer>();
        currentFloor = (int) (Math.random() * 3);

        this.door0 = door0;
        this.door1 = door1;
        this.door2 = door2;
    }

    @Override
    public void run() {
        try {
            Simulation.print("Elevator started on floor " + currentFloor);

            while(true) {
                if (Thread.interrupted()) {
                    System.out.println("Elevator terminating");
                    break;
                }

                //if there are requests for floors, process one
                if (!floorRequests.isEmpty()) {
                    int currentRequest = floorRequests.remove();


                    //remove all instances of currentRequest
                    removeAllInstances(currentRequest);
                    if (currentRequest != currentFloor) {
                        goToFloor(currentRequest);
                    }

                    //open door
                    switch (currentRequest) {
                        case 0:
                            door0.open();
                            break;
                        case 1:
                            door1.open();
                            break;
                        case 2:
                            door2.open();
                            break;
                        default:
                            throw new RuntimeException("Encountered a floor that wasn't 0, 1, or 2");
                    }

                    //make sure all passengers leave/enter who want to
                    signalAllPassengers();

                    //close door
                    switch (currentRequest) {
                        case 0:
                            door0.close();
                            break;
                        case 1:
                            door1.close();
                            break;
                        case 2:
                            door2.close();
                            break;
                        default:
                            throw new RuntimeException("Encountered a floor that wasn't 0, 1, or 2");
                    }
                } else {
                    //System.out.println("There are no floor requests");
                }
            }
        }catch (InterruptedException e) {
            return;
        }
    }

    synchronized void signalAllPassengers() throws InterruptedException {
        ArrayList<Passenger> passengersToLeave = new ArrayList<Passenger>(passengerLeaveRequests.get(currentFloor));
        for(Passenger passenger : passengersToLeave) {
            passenger.exitElevator();
        }
        passengerLeaveRequests.put(currentFloor, new ArrayList<Passenger>());

        ArrayList<Passenger> passengersToEnter = new ArrayList<Passenger>(passengerEnterRequests.get(currentFloor));
        for(Passenger passenger : passengersToEnter) {
            passenger.enterElevator();
        }
        passengerEnterRequests.put(currentFloor, new ArrayList<Passenger>());
    }

    synchronized void removeAllInstances(int currentRequest) {
        while(floorRequests.contains(currentRequest)) {
            floorRequests.remove(currentRequest);
        }
    }

    public void addFloorRequest(int floor) throws InterruptedException {
        floorRequests.add(floor);
        Simulation.print("Received request to go to floor " + floor);
    }

    public void goToFloor(int floor) throws InterruptedException {
        Simulation.print("Elevator leaving floor " + currentFloor);
        currentFloor = floor;
        Simulation.print("Elevator arrived on floor " + currentFloor);
    }

    public void addEnterRequest(int startFloor, Passenger passenger) {
        passengerEnterRequests.get(startFloor).add(passenger);
    }

    public void addLeaveRequest(int destinationFloor, Passenger passenger) {
        passengerLeaveRequests.get(destinationFloor).add(passenger);
    }
}
