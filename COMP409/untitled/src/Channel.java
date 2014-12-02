
public class Channel {
    private Request msg;

    public Channel() {
        msg = null;
    }

    public synchronized void send(Request o) throws InterruptedException {
        msg = o;
        notify();
        while(msg != null) {
            wait();
        }
    }

    public synchronized Request receive() throws InterruptedException {
        while(msg == null) {
            wait();
        }
        Request ret = msg;
        msg = null; // Indicate it has been consumed
        notify();
        return ret;
    }

}