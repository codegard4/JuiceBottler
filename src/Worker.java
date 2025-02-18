
public class Worker implements Runnable {


    public static void main(String[] args) {
        // we run the workers from the plant class
    }

    //    Fetched(15),
//    Peeled(38), // Peeled takes more than twice as much time as fetched
//    Squeezed(29), // Squeezed takes a long time too
//    Bottled(17),
//    Processed(1);

    // each thread represents 1 worker completing their juice bottling tasks
    private final Thread thread;
    // save the job that a worker should be doing as a string
    private final String job;
    // workers can do multiple tasks, or in my very monotenous (and simple factory) they will do one task
    // here is the queue of oranges they get
    private final BlockingMailbox orangesToProcess;
    private final BlockingMailbox orangesProcessed;
    private volatile boolean timeToWork;

    // if the value is modified within a thread the value is stored in a thread's cache and not written to main memory

    Worker(int threadNum, String job, BlockingMailbox orangesToProcess, BlockingMailbox orangesProcessed) {

        // initialize each worker -- both queues should be tied to another worker
        this.orangesToProcess = orangesToProcess;
        this.orangesProcessed = orangesProcessed;
        this.job = job;
        System.out.println("Worker " + threadNum + " job: " + job);
        thread = new Thread(this, " Worker[" + (threadNum + 1) + "]");
    }

    public String job() {
        return job;
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

    public void run() {
        while (timeToWork) {
            System.out.println(job + "ing Being done");
            Orange orange = orangesToProcess.get(); // get or wait until you can get an orange
            orange.runProcess(); // do something with the orange
            orangesProcessed.put(orange); // done with the orange, give it to the next worker!
            System.out.print(".");
        }
        System.out.println();
        System.out.println(Thread.currentThread().getName() + " Done");
    }

}