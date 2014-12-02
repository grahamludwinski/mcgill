
public class Door extends Thread {
    private boolean isOpen;
    private int floor;

    public Door(int floor) {
        this.isOpen = false;
        this.floor = floor;
    }

    @Override
    public void run() {
        while(true) {
            if(Thread.interrupted()){
                System.out.println("Door " + floor + " terminating");
                break;
            }
        }
    }

    public void open() throws InterruptedException {
        isOpen = true;
        Simulation.print("Door " + floor + " opened");
    }

    public void close() throws InterruptedException {
        isOpen = false;
        Simulation.print("Door " + floor + " closed");
    }
}
