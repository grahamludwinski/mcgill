import java.util.ArrayList;
import java.util.HashMap;

public class Simulation extends Thread {
    private Elevator elevator;

    private Door door0;
    private Door door1;
    private Door door2;

    private CallButton callButton0;
    private CallButton callButton1;
    private CallButton callButton2;

    private DestinationButton destButton0;
    private DestinationButton destButton1;
    private DestinationButton destButton2;

    private ArrayList<Passenger> passengers;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Je expect 2 arguments");
            return;
        }

        Simulation simulation = new Simulation(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        simulation.start();
    }

    public Simulation(int n, int t) {
        System.out.println("Passengers: " + n + "\t\tNumber of movements: " + t);

        Channel callButton0Channel = new Channel();
        Channel callButton1Channel = new Channel();
        Channel callButton2Channel = new Channel();

        Channel destButton0Channel = new Channel();
        Channel destButton1Channel = new Channel();
        Channel destButton2Channel = new Channel();

        door0 = new Door(0);
        door1 = new Door(1);
        door2 = new Door(2);

        elevator = new Elevator(door0, door1, door2);

        callButton0 = new CallButton(0, elevator, callButton0Channel);
        callButton1 = new CallButton(1, elevator, callButton1Channel);
        callButton2 = new CallButton(2, elevator, callButton2Channel);

        destButton0 = new DestinationButton(0, elevator, destButton0Channel);
        destButton1 = new DestinationButton(1, elevator, destButton1Channel);
        destButton2 = new DestinationButton(2, elevator, destButton2Channel);

        //create passengers
        passengers = new ArrayList<Passenger>();
        for (int i=0; i < n; i++) {
            passengers.add(new Passenger(i, t, elevator, callButton0Channel, callButton1Channel, callButton2Channel, destButton0Channel, destButton1Channel, destButton2Channel));
        }

    }

    @Override
    public void run() {
        System.out.println("Starting simulation\n");
        startEverything();

        for (Passenger passenger : passengers) {
            try {
                passenger.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nPassengers finished");

        elevator.interrupt();

        door0.interrupt();
        door1.interrupt();
        door2.interrupt();

        callButton0.interrupt();
        callButton1.interrupt();
        callButton2.interrupt();

        destButton0.interrupt();
        destButton1.interrupt();
        destButton2.interrupt();
    }

    public void startEverything() {
        elevator.start();

        door0.start();
        door1.start();
        door2.start();

        callButton0.start();
        callButton1.start();
        callButton2.start();

        destButton0.start();
        destButton1.start();
        destButton2.start();

        for (Passenger passenger : passengers) {
            passenger.start();
        }
    }

    public static void print(String message) throws InterruptedException {
        long sleepTime = (long)(Math.random() * 250 + 50);
        System.out.println(message);
        Thread.sleep(sleepTime);
    }
}
