package simulator.protocols.deadlockResolution;

import simulator.SimParams;
import simulator.enums.ServerProcess;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.server.Server;
import simulator.server.network.Message;
import simulator.server.transactionManager.TransInfo;
import ui.Log;

import java.util.ArrayList;
import java.util.List;

public class FirstDeadlockResolution implements DeadlockResolutionProtocol {

    private final Server server;
    private final SimParams simParams;
    private final Log log;

    public FirstDeadlockResolution(Server server) {
        this.server = server;
        this.simParams = server.getSimParams();
        log = new Log(ServerProcess.DRP, server.getID(), simParams.timeProvider, simParams.log);
    }

    /**
     * Picks a random transaction that is at this server and aborts it
     */
    @Override
    public void resolveDeadlocks(Deadlock deadlock) {
        List<TransInfo> transactionsInDeadlock = deadlock.getTransactionsInvolved();
        TransInfo firstTrans = transactionsInDeadlock.get(0);

        if (Log.isLoggingEnabled())
            log.log("====== firstTrans = " + firstTrans.serverID + " and server ID = " + server.getID());

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