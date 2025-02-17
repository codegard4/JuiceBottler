public class BlockingMailbox {
    private Orange orange;

    public BlockingMailbox() {
        orange = null;
    }

    public synchronized void put(Orange o) {
        while (!isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
        orange = o;
        notifyAll();
    }

    public synchronized Orange get() {
        while (isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }
        Orange ret = orange;
        orange = null;
        notifyAll();
        return ret;
    }

    public synchronized boolean isEmpty() {
        return orange == null;
    }
}