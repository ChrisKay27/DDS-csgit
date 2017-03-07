package stats;

import java.util.ArrayList;
import java.util.List;

public class Statistics {

    private int timeouts;
    private int completedOnTime, completedLate;
    private int numAborted;
    private int numAbortedAndRestarted;

    private List<Integer> completedOnTimeTrans = new ArrayList<>();
    private List<Integer> completedLateTrans = new ArrayList<>();


    public void addCompletedOnTime(int id) {
        completedOnTime++;
        completedOnTimeTrans.add(id);
    }

    public void addCompletedLate(int id) {
        completedLate++;
        completedLateTrans.add(id);
    }

    public int getCompletedOnTime() {
        return completedOnTime;
    }

    public void addTimeout() {
        timeouts++;
    }

    public int getTimeouts() {
        return timeouts;
    }

    public void setTimeouts(int timeouts) {
        this.timeouts = timeouts;
    }

    public int getCompletedLate() {
        return completedLate;
    }

    public int getNumAborted() {
        return numAborted;
    }

    public void addNumAborted() {
        this.numAborted += 1;
    }

    public void addNumAbortedAndRestarted() {
        numAbortedAndRestarted += 1;
    }

    public int getNumAbortedAndRestarted() {
        return numAbortedAndRestarted;
    }

    public void setNumAbortedAndRestarted(int numAbortedAndRestarted) {
        this.numAbortedAndRestarted = numAbortedAndRestarted;
    }
}
