
public class Worker implements Runnable {


    public static void main(String[] args) {
    // we run the workers from the plant class
    }

//    Fetched(15),
//    Peeled(38), // Peeled takes more than twice as much time as fetched
//    Squeezed(29), // Squeezed takes a long time too
//    Bottled(17),
//    Processed(1);
    public final int ORANGES_PER_BOTTLE = 3;

    // each thread represents 1 worker completing their juice bottling tasks
    private final Thread thread;
    private final String[] Jobs = new String[]{"Fetched", "Peeled", "Squeezed", "Bottled", "Processed", "Peeled", "Squeezed"};
    private int orangesProvided;
    private int orangesProcessed;
    private String job;
    private volatile boolean timeToWork; // ensures that the value is written when written and read when read -- only needed across threads
    // if the value is modified within a thread the value is stored in a thread's cache and not written to main memory

    Worker(int threadNum) {
        orangesProvided = 0;
        orangesProcessed = 0;
        // initialize each worker (thread)
        job = Jobs[threadNum % 7];
        System.out.println("Worker " + threadNum + " job: " + job);
        thread = new Thread(this, " Worker[" + (threadNum+1) + "]");
    }

    public String job() { return job; }

    public void startWork() {
        timeToWork = true;
        // start each worker on their juice bottling tasks

            thread.start();
    }

    public void stopWork() {
        timeToWork = false;
    }

    public void run() {
        while (timeToWork) {
            processEntireOrange(new Orange());
            orangesProvided++;
            System.out.print(".");
        }
        System.out.println();
        System.out.println(Thread.currentThread().getName() + " Done");
    }

    public void processEntireOrange(Orange o) {
        while (o.getState() != Orange.State.Bottled) {
            o.runProcess();
        }
        orangesProcessed++;
    }

    public int getProvidedOranges() {
        return orangesProvided;
    }

    public int getProcessedOranges() {
        return orangesProcessed;
    }

    public int getBottles() {
        return orangesProcessed / ORANGES_PER_BOTTLE;
    }

    public int getWaste() {
        return orangesProcessed % ORANGES_PER_BOTTLE;
    }
}