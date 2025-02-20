import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A juice bottler plant can run multiple plants with multiple workers at one time in order to complete
 * various juice bottling tasks. A juice bottler has one specific job (i.e. to "peel" or "squeeze").
 * At the end of the juice bottling day the number of bottles bottled, oranges wasted and oranges processed is
 * printed to the screen.
 */
public class Plant implements Runnable {
    // how long do we want to run the juice processing
    public static final long PROCESSING_TIME = 5 * 1000;

    private static final int NUM_PLANTS = 6;
    private static final int NUM_WORKERS = 6;

    // create queues to hold oranges in different states
    private final BlockingQueue<Orange> fetchQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> peelQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> squeezeQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> bottleQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> processedQueue = new LinkedBlockingQueue<>();

    private final Worker[] workers = new Worker[NUM_WORKERS];


    public static void main(String[] args) {

        Plant[] plants = new Plant[NUM_PLANTS];

        // start each plant
        for (int i = 0; i < NUM_PLANTS; i++) {
            plants[i] = new Plant(i);
            plants[i].startPlant();
        }

        // give the plants time to do work
        delay(PROCESSING_TIME, "Plant malfunction");

        // stop the plant, and wait for it to shutdown
        for (Plant p : plants) {
            p.stopPlant();
        }
        // join all of the plant threads
        for (Plant p : plants) {
            p.waitToStop();
        }

        // summarize the results
        int totalProvided = 0;
        int totalProcessed = 0;
        int totalBottles = 0;
        int totalWasted = 0;
        for (Plant p : plants) {
            totalProvided += p.getProvidedOranges();
            totalProcessed += p.getProcessedOranges();
            totalBottles += p.getBottles();
            totalWasted += p.getWaste();
        }
        // add a 1 second delay so that the results are neatly summarized at the bottom of the output
        delay(100, "Waiting to summarize results");
        System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
        System.out.println();
        System.out.println("Created " + totalBottles +
                " bottles, wasted " + totalWasted + " oranges");
        System.out.println();
    }

    /**
     * Delay the plant for a set amount of time (either to bottle juice or to wait to summarize the results
     *
     * @param time
     * @param errMsg
     */
    private static void delay(long time, String errMsg) {
        long sleepTime = Math.max(1, time);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            System.err.println(errMsg);
        }
    }

    public final int ORANGES_PER_BOTTLE = 3;

    // each thread represents 1 worker completing their juice bottling tasks
    private final Thread thread;
    private int orangesProvided;
    private int orangesProcessed;
    private volatile boolean timeToWork;
    // All of the jobs a worker can have and queues they can pull from.
    // We create workers in groups of 6 because we want multiple peelers and squeezers (these are the bottlenecks in juice bottling)
    private final String[] jobs = new String[]{"Fetch", "Peel", "Squeeze", "Bottle", "Peel", "Squeeze"};
    private final BlockingQueue<Orange>[] inputQueues = new BlockingQueue[]{fetchQueue, peelQueue, squeezeQueue, bottleQueue, peelQueue, squeezeQueue};
    private final BlockingQueue<Orange>[] outputQueues = new BlockingQueue[]{peelQueue, squeezeQueue, bottleQueue, processedQueue, squeezeQueue, bottleQueue};

    /**
     * Create a plant and initialize all of the workers at the plant
     *
     * @param threadNum
     */
    Plant(int threadNum) {
        orangesProvided = 0;
        orangesProcessed = 0;
        // initialize each worker
        thread = new Thread(this, "Plant[" + (threadNum + 1) + "]");
        for (int i = 0; i < NUM_WORKERS; i++) {

            // find out which queues and what job the worker will have / pull from
            int index = i % 6;
            workers[i] = new Worker(jobs[index], inputQueues[index], outputQueues[index]);
        }
    }

    /**
     * Start the plant and all of the worker threads
     */
    public void startPlant() {
        // start all the workers and the main plant thread
        timeToWork = true;
        thread.start();
        for (Worker w : workers) {
            w.startWork();
        }
    }


    /**
     * Stop the plant and all of the worker threads
     */
    public void stopPlant() {
        // stop all the workers
        timeToWork = false;
        for (Worker w : workers) {
            w.stopWork();
        }
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
     * If the plant is running put an orange in the fetch queue and let the workers do work!
     */
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Processing oranges");
        while (timeToWork) {
            if (fetchQueue.isEmpty()) {
                Orange orange = new Orange();
                orangesProvided++;
                fetchQueue.add(orange);
            }
        }
        System.out.println();
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    /**
     * @return the number of oranges provided
     */
    public int getProvidedOranges() {
        return orangesProvided;
    }

    /**
     * Loop through and count the number of processed oranges then save that number and return it
     *
     * @return number of processed oranges
     */
    public int getProcessedOranges() {
        // count the number of processed oranges in our queue
        // yes, this will remove all of the oranges, but orangesProcessed saves it so we either count oranges in the queue
        // OR we already counted all the oranges (or no oranges were processed) and we return orangesProcessed
        while (!processedQueue.isEmpty()) {
            processedQueue.remove();
            orangesProcessed++;
        }
        return orangesProcessed;
    }

    /**
     * @return number of juice bottles
     */
    public int getBottles() {
        return getProcessedOranges() / ORANGES_PER_BOTTLE;
    }

    /**
     * @return number of wasted oranges
     */
    public int getWaste() {
        return getProcessedOranges() % ORANGES_PER_BOTTLE;
    }
}