
public class Plant implements Runnable {
    // How long do we want to run the juice processing
    public static final long PROCESSING_TIME = 5 * 1000;
    //    private static volatile boolean wearingFuzzyPinkBunnySlippers = false;
    //    public volatile boolean betterGuitarPlayer = false;
    //    public static volatile boolean redGreenColorBlind = true;

    private static final int NUM_PLANTS = 3;

    private final BlockingMailbox fetchMailbox = new BlockingMailbox();
    private final BlockingMailbox peelMailbox = new BlockingMailbox();
    private final BlockingMailbox squeezeMailbox = new BlockingMailbox();
    private final BlockingMailbox bottleMailbox = new BlockingMailbox();
    private final BlockingMailbox processedMailbox = new BlockingMailbox();

    private final Worker fetchWorker;
    private final Worker peelWorker;
    private final Worker squeezeWorker;
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
//        for (Plant p : plants) {
//            p.waitToStop();
//        }

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
        // initialize each worker (thread)
        thread = new Thread(this, "Plant[" + (threadNum + 1) + "]");
        fetchWorker = new Worker(threadNum, "Fetch", fetchMailbox, peelMailbox);
        peelWorker = new Worker(threadNum, "Peel", peelMailbox, bottleMailbox);
        squeezeWorker = new Worker(threadNum, "Squeeze", squeezeMailbox, bottleMailbox);
        bottleWorker = new Worker(threadNum, "Bottle", bottleMailbox, processedMailbox);
    }

    public void startPlant() {
        timeToWork = true;
        thread.start();
        fetchWorker.startWork();
        peelWorker.startWork();
        squeezeWorker.startWork();
        bottleWorker.startWork();
    }


    public void stopPlant() {
        System.out.println("Stopping Plant");
        timeToWork = false;
        fetchWorker.stopWork();
        peelWorker.stopWork();
        squeezeWorker.stopWork();
        bottleWorker.stopWork();
        System.out.println("Plant stopped");
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
                Orange orange = new Orange();
                fetchMailbox.put(orange);
                orangesProvided++;
                System.out.print(".");
        }
        System.out.println();
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    public int getProvidedOranges() {
        return orangesProvided;
    }

    public int getProcessedOranges() {
        while(!processedMailbox.isEmpty()) {
            processedMailbox.get();
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