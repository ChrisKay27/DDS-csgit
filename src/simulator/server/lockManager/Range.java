package simulator.server.lockManager;

/**
 * Created by Chris on 6/10/2016.
 */
public class Range {
    private final int min, max;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean contains(int value){
        return value >= min && value <= max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
