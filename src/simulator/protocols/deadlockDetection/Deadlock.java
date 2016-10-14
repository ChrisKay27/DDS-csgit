package simulator.protocols.deadlockDetection;

import simulator.server.transactionManager.TransInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Chris on 6/26/2016.
 */
public class Deadlock{

    private static int nextDeadlockID;

    private final int deadlockID = getNextDeadlockID();
    private final List<TransInfo> transactionsInvolved = new ArrayList<>();
    private final int serverID;
    private final int detectionTime;

    public Deadlock(List<TransInfo> transactionsInvolved, int serverID, int detectionTime) {
        this.serverID = serverID;
        this.detectionTime = detectionTime;
        this.transactionsInvolved.addAll(transactionsInvolved);
    }


    public int getDeadlockID() {
        return deadlockID;
    }

    public List<TransInfo> getTransactionsInvolved() {
        return transactionsInvolved;
    }

    @Override
    public boolean equals(Object obj) {
        if( obj == this ) return true;
        if(!(obj instanceof Deadlock)) return false;
        Deadlock d = (Deadlock) obj;

        return d.deadlockID == deadlockID && transactionsInvolved.equals(d.transactionsInvolved);
    }


    private static int getNextDeadlockID() {
        return nextDeadlockID++;
    }

    public int getDetectionTime() {
        return detectionTime;
    }

    public int getServerID() {
        return serverID;
    }

    @Override
    public String toString() {
        return "Deadlock{" +
                "deadlockID=" + deadlockID +
                ", serverID=" + serverID +
                ", detectionTime=" + detectionTime +
                '}';
    }


    public String toLongString() {
        return "Deadlock{" +
                "deadlockID=" + deadlockID +
                ", transactionsInvolved=" + transactionsInvolved +
                ", serverID=" + serverID +
                ", detectionTime=" + detectionTime +
                '}';
    }
}
