import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Plant implements Runnable {
    // How long do we want to run the juice processing
    public static final long PROCESSING_TIME = 5 * 1000;

    private static final int NUM_PLANTS = 6;
    private static final int NUM_WORKERS = 6; // THIS MUST BE AT LEAST 4 WORKERS

    private final BlockingQueue<Orange> fetchMailbox = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> peelMailbox = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> squeezeMailbox = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> bottleMailbox = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> processedMailbox = new LinkedBlockingQueue<>();

    private final Worker[] workers = new Worker[NUM_WORKERS];


    public static void main(String[] args) {

        Plant[] plants = new Plant[NUM_PLANTS];
        for (int i = 0; i < NUM_PLANTS; i++) {
            plants[i] = new Plant(i);
            plants[i].startPlant();
        }

        // Give the plants time to do work
        delay(PROCESSING_TIME, "Plant malfunction");

        // Stop the plant, and wait for it to shutdown
        for (Plant p : plants) {
            p.stopPlant();
        }
        for (Plant p : plants) {
            p.waitToStop();
        }

        // Summarize the results
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
        delay(50, "Waiting to summarize results");
        System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
        System.out.println();
        System.out.println("Created " + totalBottles +
                " bottles, wasted " + totalWasted + " oranges");
        System.out.println();
    }

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
    private int orangesProcessed;// workers come in "teams" of 7
    private volatile boolean timeToWork; // ensures that the value is written when written and read when read -- only needed across threads
    // if the value is modified within a thread the value is stored in a thread's cache and not written to main memory
    private final String[] jobs = new String[]{"Fetch", "Peel", "Squeeze", "Bottle", "Peel", "Squeeze"};
    private final BlockingQueue<Orange>[] inputQueues = new BlockingQueue[]{fetchMailbox, peelMailbox, squeezeMailbox, bottleMailbox, peelMailbox, squeezeMailbox};
    private final BlockingQueue<Orange>[] outputQueues = new BlockingQueue[]{peelMailbox, squeezeMailbox, bottleMailbox, processedMailbox, squeezeMailbox, bottleMailbox};

    Plant(int threadNum) {
        orangesProvided = 0;
        orangesProcessed = 0;
        // initialize each worker
        thread = new Thread(this, "Plant[" + (threadNum + 1) + "]");
        for (int i = 0; i < NUM_WORKERS; i++) {
            // find out which queues and what job the worker will have / pull from
            int index = i % 6;
//            System.out.println("Creating worker " + jobs[index]);
            workers[i] = new Worker(jobs[index], inputQueues[index], outputQueues[index]);
        }
    }

    public void startPlant() {
        // start all the workers and the main plant thread
        timeToWork = true;
        thread.start();
        for (Worker w : workers) {
            w.startWork();
        }
    }


    public void stopPlant() {
        // stop all the workers
        timeToWork = false;
        for (Worker w : workers) {
            w.stopWork();
        }
    }

    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println(thread.getName() + " stop malfunction");
        }
    }

    public void run() {
        System.out.println(Thread.currentThread().getName() + " Processing oranges");
        while (timeToWork) {
            if(fetchMailbox.isEmpty()) {
                Orange orange = new Orange();
                orangesProvided++;
                fetchMailbox.add(orange);
            }
        }
        System.out.println();
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    public int getProvidedOranges() {
        return orangesProvided;
    }

    public int getProcessedOranges() {
        while (!processedMailbox.isEmpty()) {
            processedMailbox.remove();
            orangesProcessed++;
        }
        return orangesProcessed;
    }

    public int getBottles() {
        return getProcessedOranges() / ORANGES_PER_BOTTLE;
    }

    public int getWaste() {
        return getProcessedOranges() % ORANGES_PER_BOTTLE;
    }
}