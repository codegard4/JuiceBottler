import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;

/**
 * Workers work in a juice bottler plant
 * A worker takes a queue of oranges to complete a task on, does work with the orange, and puts the orange in a "Processed" queue
 * Workers work until they are told to stop working, which is controlled in the plant class
 * These javadocs hopefully look AI generated and professional, but they were created by yours truly
 */

public class Worker implements Runnable {

    // each thread represents 1 worker completing their juice bottling tasks
    private final Thread thread;
    // here is the queue of oranges they get and the queue they send the processed oranges to
    private final BlockingQueue<Orange> orangesToProcess;
    private final BlockingQueue<Orange> orangesProcessed;
    private volatile boolean timeToWork;

    /**
     * Takes a string to name the worker (i.e. the worker is a "Peeler") and input and output queues to take and put oranges in
     *
     * @param job
     * @param orangesToProcess
     * @param orangesProcessed
     */
    Worker(String job, BlockingQueue<Orange> orangesToProcess, BlockingQueue<Orange> orangesProcessed) {

        // initialize each worker -- both queues should be tied to another worker
        this.orangesToProcess = orangesToProcess;
        this.orangesProcessed = orangesProcessed;
        thread = new Thread(this, job + "ing Worker");
    }

    /**
     * Starts the worker's thread of processing oranges
     */
    public void startWork() {
        timeToWork = true;
        // start each worker on their juice bottling tasks
        thread.start();
    }

    /**
     * Stops the worker from processing oranges
     */
    public void stopWork() {
        timeToWork = false;
        thread.interrupt();
    }

    /**
     * Try to join threads
     */
    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    /**
     * Take an orange from the queue of oranges to process if there are oranges and process it. Put the processed orange in the output queue
     * Do not grab the same orange as another worker with the same process
     */
    public synchronized void run() {
        while (timeToWork) {
            try {
                if (!orangesToProcess.isEmpty()) {
                    Orange orange = orangesToProcess.remove(); // remove or wait until you can get an orange
                    orange.runProcess(); // do something with the orange
                    orangesProcessed.add(orange); // done with the orange, give it to the next worker!
                }
            } catch (NoSuchElementException ignore) {
            }
        }
    }
}