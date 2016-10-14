package simulator.protocols.deadlockResolution;

import simulator.SimParams;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.server.Server;
import simulator.server.network.Message;
import simulator.server.transactionManager.TransInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 8/2/2016.
 */
public class RandomDeadlockResolution implements DeadlockResolutionProtocol {

    private final Server server;
    private final SimParams simParams;

    public RandomDeadlockResolution(Server server) {
        this.server = server;
        this.simParams = server.getSimParams();
    }

    @Override
    public void resolveDeadlocks(Deadlock deadlock) {
        List<TransInfo> transAtThisServer = new ArrayList<>();
        for(TransInfo ti : deadlock.getTransactionsInvolved() )
            if( ti.serverID == server.getID() )
                transAtThisServer.add(ti);

        if( transAtThisServer.isEmpty() )
            return;

        TransInfo tInfo = transAtThisServer.get((int) (simParams.rand.get()*transAtThisServer.size()));
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
