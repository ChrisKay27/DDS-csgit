package simulator.protocols.deadlockDetection.mobileAgentEnabledApproach;

import simulator.SimParams;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.protocols.deadlockDetection.WFG_DDP;
import simulator.server.Server;
import simulator.server.lockManager.Lock;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Chris on 7/3/2016.
 *
 * Builds a WFG of locks not transInfos!
 *
 */
public class MAEDD extends WFG_DDP {



    public MAEDD(Server server, SimParams simParams, Consumer<List<Deadlock>> resolver, Consumer<Integer> overheadIncurer, Consumer<Deadlock> deadlockListener) {
        super(server, simParams, resolver, overheadIncurer, deadlockListener);
    }



    @Override
    public void start() {
        super.start();
    }



    public void addWait(Lock waitingLock, Lock heldLock) {

        wfgBuilder.addTask(waitingLock);
        wfgBuilder.addTask(heldLock);

        wfgBuilder.addTaskWaitsFor(waitingLock, heldLock);


    }

    public void removeAllWaitsOn(Lock heldLock) {
        wfgBuilder.removeTask(heldLock);
    }


}
