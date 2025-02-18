import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Plant implements Runnable {
    // How long do we want to run the juice processing
    public static final long PROCESSING_TIME = 5 * 1000;
    //    private static volatile boolean wearingFuzzyPinkBunnySlippers = false;
    //    public volatile boolean betterGuitarPlayer = false;
    //    public static volatile boolean redGreenColorBlind = true;

    private static final int NUM_PLANTS = 3;

    private final BlockingQueue<Orange> fetchMailbox = new LinkedBlockingQueue<Orange>();
    private final BlockingQueue<Orange> peelMailbox = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> squeezeMailbox = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> bottleMailbox = new LinkedBlockingQueue<>();
    private final BlockingQueue<Orange> processedMailbox = new LinkedBlockingQueue<>();

    private final Worker fetchWorker;
    private final Worker peelWorker;
    private final Worker peelWorker2;
    private final Worker squeezeWorker;
    private final Worker squeezeWorker2;
    private final Worker bottleWorker;


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
        System.out.println("Total provided/processed = " + totalProvided + "/" + totalProcessed);
        System.out.println("Created " + totalBottles +
                " bottles, wasted " + totalWasted + " oranges");
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

    Plant(int threadNum) {
        orangesProvided = 0;
        orangesProcessed = 0;
        // initialize each worker
        thread = new Thread(this, "Plant[" + (threadNum + 1) + "]");
        fetchWorker = new Worker("Fetch", fetchMailbox, peelMailbox);
        peelWorker = new Worker("Peel", peelMailbox, bottleMailbox);
        peelWorker2 = new Worker("Peel2", peelMailbox, bottleMailbox);
        squeezeWorker = new Worker("Squeeze", squeezeMailbox, bottleMailbox);
        squeezeWorker2 = new Worker("Squeeze2", squeezeMailbox, bottleMailbox);
        bottleWorker = new Worker("Bottle", bottleMailbox, processedMailbox);
    }

    public void startPlant() {
        // start all the workers and the main plant thread
        timeToWork = true;
        thread.start();
        fetchWorker.startWork();
        peelWorker.startWork();
        peelWorker2.startWork();
        squeezeWorker.startWork();
        squeezeWorker2.startWork();
        bottleWorker.startWork();
    }


    public void stopPlant() {
        // stop all the workers
        timeToWork = false;
        fetchWorker.stopWork();
        peelWorker.stopWork();
        peelWorker2.stopWork();
        squeezeWorker.stopWork();
        squeezeWorker2.stopWork();
        bottleWorker.stopWork();
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
            // start a new orange by putting it in the fetchMailbox
            try {
                Orange orange = new Orange();
                // since the minimum amount of time for a worker to fetch an orange is 15ms we need to wait that long
                // to put an orange in the queue.
                thread.sleep(15);
                fetchMailbox.add(orange);
                orangesProvided++;
//                System.out.print(".");
            } catch (InterruptedException ignored) {
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