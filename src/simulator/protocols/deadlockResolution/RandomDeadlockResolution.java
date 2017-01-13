package simulator.protocols.deadlockResolution;

import simulator.SimParams;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.server.Server;
import simulator.server.network.Message;
import simulator.server.transactionManager.TransInfo;

import java.util.ArrayList;
import java.util.List;

public class RandomDeadlockResolution implements DeadlockResolutionProtocol {

    private final Server server;
    private final SimParams simParams;

    public RandomDeadlockResolution(Server server) {
        this.server = server;
        this.simParams = server.getSimParams();
    }

    /**
     * Picks a random transaction that is at this server and aborts it
     */
    @Override
    public void resolveDeadlocks(Deadlock deadlock) {
        List<TransInfo> transactionsInDeadlock = deadlock.getTransactionsInvolved();
        TransInfo firstTrans = transactionsInDeadlock.get(0);

        if (firstTrans.serverID != server.getID())
            return;

        deadlock.setResolutionTime(simParams.getTime());
        simParams.getDeadlockResolutionListener().accept(deadlock, firstTrans.transID);
        server.getTM().abort(firstTrans.getID());
    }

    @Override
    public void resolveMultiple(List<Deadlock> l) {
        l.forEach(this::resolveDeadlocks);
    }

    @Override
    public void receiveMessage(Message msg) {

    }
}