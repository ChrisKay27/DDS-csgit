package simulator.server.transactionManager;



import simulator.protocols.deadlockDetection.WFG.WFGNode;

import java.util.List;

/**
 * Created by Chris on 6/16/2016.
 */
public class TransInfo implements WFGNode {
    public final int serverID;
    public final int transID;
    public final int deadline;
    public final int workload;
    public final int executionTime;
    public final List<Integer> readPages, writePages;


    public TransInfo(int serverID, int transID, int deadline, int workload, int executionTime, List<Integer> readPages, List<Integer> writePages) {
        this.serverID = serverID;
        this.transID = transID;
        this.deadline = deadline;
        this.workload = workload;
        this.executionTime = executionTime;
        this.readPages = readPages;
        this.writePages = writePages;
    }


    public int getPriority() {
        return -deadline;
    }

    @Override
    public int getID() {
        return transID;
    }

    @Override
    public boolean equals(Object o){
        if( o == this )
            return true;
        if(!(o instanceof TransInfo))
            return false;
        TransInfo ti = (TransInfo) o;

        return ti.getID() == transID;
    }

    @Override
    public String toString() {
        return "TransInfo{"+transID+"}";
    }
}
