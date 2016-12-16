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
        List<TransInfo> transAtThisServer = new ArrayList<>();

        for(TransInfo ti : deadlock.getTransactionsInvolved() )
            if( ti.serverID == server.getID() )
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