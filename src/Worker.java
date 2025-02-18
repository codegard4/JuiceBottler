import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;

public class Worker implements Runnable {

    // each thread represents 1 worker completing their juice bottling tasks
    private final Thread thread;
    // here is the queue of oranges they get
    private BlockingQueue<Orange> orangesToProcess;
    private BlockingQueue<Orange> orangesProcessed;
    private volatile boolean timeToWork;

    // if the value is modified within a thread the value is stored in a thread's cache and not written to main memory

    Worker(String job, BlockingQueue<Orange> orangesToProcess, BlockingQueue<Orange> orangesProcessed) {

        // initialize each worker -- both queues should be tied to another worker
        this.orangesToProcess = orangesToProcess;
        this.orangesProcessed = orangesProcessed;
        thread = new Thread(this, job+ "ing Worker");
    }

    public void startWork() {
        timeToWork = true;
        // start each worker on their juice bottling tasks
        thread.start();
    }

    public void stopWork() {
        timeToWork = false;
        thread.interrupt();
    }


    synchronized public void run() {
        while (timeToWork) {
//            System.out.println(job + "ing Being done");
            try{
            if (!orangesToProcess.isEmpty()) {
                Orange orange = orangesToProcess.remove(); // remove or wait until you can get an orange
                orange.runProcess(); // do something with the orange
                orangesProcessed.add(orange); // done with the orange, give it to the next worker!
            }} catch(NoSuchElementException ignore){}
        }
    }
}