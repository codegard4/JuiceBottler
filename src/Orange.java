/**
 * Orange is processed by plant
 * An orange can be in different states like Fetched, Peeled, Squeezed, Bottled, and Processed.
 * <p>
 * ****NOTE: Javadocs for the orange class only were created by AI -- I wanted "properly" formatted
 * javadocs for the methods to base my worker and plant (the classes I actually wrote) javadocs off of
 */
public class Orange {

    /**
     * Enum representing the possible states an orange can be in during the processing pipeline.
     * Each state has a timeToComplete value representing the time (in milliseconds) it takes to complete that state.
     */
    public enum State {

        // Peeled and Squeezed take roughly twice as long as Fetched and Bottled so they will be bottlenecks in our plant

        Fetched(15), Peeled(38), // Peeled takes more than twice as much time as fetched
        Squeezed(29), // Squeezed takes a long time too
        Bottled(17), Processed(1);

        private static final int finalIndex = State.values().length - 1;

        final int timeToComplete;

        /**
         * Constructs a State with a specified time to complete.
         *
         * @param timeToComplete Time in milliseconds required to complete this state
         */
        State(int timeToComplete) {
            this.timeToComplete = timeToComplete;
        }

        /**
         * Gets the next state in the processing pipeline.
         *
         * @return The next state in the pipeline
         */
        State getNext() {
            int currIndex = this.ordinal();
            if (currIndex >= finalIndex) {
                throw new IllegalStateException("Already at final state");
            }
            return State.values()[currIndex + 1];
        }
    }

    private State state;

    /**
     * Constructs an Orange -- fetching happens automatically
     */
    public Orange() {
        state = State.Fetched;
        doWork();
    }

    /**
     * Gets the current state of the orange.
     *
     * @return The current state of the orange
     */
    public State getState() {
        return state;
    }

    /**
     * Runs the processing for the orange, advancing it to the next state in the pipeline.
     */
    public void runProcess() {
        // Don't attempt to process an already completed orange
        if (state == State.Processed) {
            throw new IllegalStateException("This orange has already been processed");
        }
        state = state.getNext();
        doWork();
    }

    /**
     * Simulates the work required to process the orange
     */
    private void doWork() {
        // Sleep for the amount of time necessary to do the work
        try {
            Thread.sleep(state.timeToComplete);
        } catch (InterruptedException e) {
            System.err.println("Incomplete orange processing, juice may be bad");
        }
    }
}
