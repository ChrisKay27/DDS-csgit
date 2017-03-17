package simulator.protocols.deadlockResolution;

import simulator.SimParams;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.server.Server;
import simulator.server.network.Message;
import simulator.server.transactionManager.TransInfo;
import java.util.ArrayList;
import java.util.List;

public class PriorityDeadlockResolution implements DeadlockResolutionProtocol {
    private final Server server;
    private final SimParams simParams;

    public PriorityDeadlockResolution(Server server) {
        this.server = server;
        this.simParams = server.getSimParams();
    }

    /**
     * Picks the lowest priority transaction and aborts it
     */
    @Override
    public void resolveDeadlocks(Deadlock deadlock) {
        int lowestPriority = Integer.MAX_VALUE;
        List<TransInfo> transactionsInDeadlock = deadlock.getTransactionsInvolved();

        TransInfo lowest = null;
        for(TransInfo ti : transactionsInDeadlock )
            if( ti.getPriority(transactionsInDeadlock, simParams.getPp()) < lowestPriority ) {
                lowest = ti;
                lowestPriority = lowest.getPriority(transactionsInDeadlock, simParams.getPp());
            }

        List<TransInfo> transAtThisServer = new ArrayList<>();

        for(TransInfo ti : transactionsInDeadlock )
            if( ti.getPriority(transactionsInDeadlock, simParams.getPp())== lowestPriority && ti.serverID == server.getID())
                transAtThisServer.add(ti);

        if( transAtThisServer.isEmpty() )
            return;

        TransInfo tInfo = transAtThisServer.get((int) (simParams.rand.get()*transAtThisServer.size()));
        deadlock.setResolutionTime(simParams.getTime());
        simParams.getDeadlockResolutionListener().accept(deadlock,tInfo.transID);
        server.getTM().abort(tInfo.getID());
    }

    @Override
    public void resolveMultiple(List<Deadlock> l) {
        l.forEach(this::resolveDeadlocks);
    }

    @Override
    public void receiveMessage(Message msg) {

    }
}