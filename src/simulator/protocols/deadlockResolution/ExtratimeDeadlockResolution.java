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

public class ExtratimeDeadlockResolution implements DeadlockResolutionProtocol {
    private final Server server;
    private final SimParams simParams;
    private final Log log;

    public ExtratimeDeadlockResolution(Server server) {
        this.server = server;
        this.simParams = server.getSimParams();
        log = new Log(ServerProcess.DRP, server.getID(), simParams.timeProvider, simParams.log);
    }

    /**
     * Picks the lowest extra time transaction and aborts it
     */
    @Override
    public void resolveDeadlocks(Deadlock deadlock) {
        int lowestExtraTime = Integer.MAX_VALUE;
        List<TransInfo> transactionsInDeadlock = deadlock.getTransactionsInvolved();

        TransInfo lowest = null;
        for (TransInfo ti : transactionsInDeadlock)
            if (ti.getDeadline() - server.getSimParams().timeProvider.get() < lowestExtraTime) {
                lowest = ti;
                lowestExtraTime = lowest.getDeadline() - server.getSimParams().timeProvider.get();
            }

        if(simParams.agentBased){
            List<TransInfo> lowestExtraTimetrans = new ArrayList<>();

            for (TransInfo ti : transactionsInDeadlock)
                if (ti.getDeadline() - server.getSimParams().timeProvider.get() == lowestExtraTime)
                    lowestExtraTimetrans.add(ti);

            if (lowestExtraTimetrans.isEmpty())
                return;

            TransInfo LowestTInfo = lowestExtraTimetrans.get((int) (simParams.rand.get() * lowestExtraTimetrans.size()));

            server.getNIC().sendMessage(new Message(LowestTInfo.serverID, ServerProcess.DRP, "A:" + LowestTInfo.getID() + ":" + server.getID(), deadlock, LowestTInfo.getDeadline()));
            return;
        }

        List<TransInfo> transAtThisServer = new ArrayList<>();

        for (TransInfo ti : transactionsInDeadlock)
            if (ti.getworkload() == lowestExtraTime && ti.serverID == server.getID())
                transAtThisServer.add(ti);

        if (transAtThisServer.isEmpty())
            return;

        TransInfo tInfo = transAtThisServer.get((int) (simParams.rand.get() * transAtThisServer.size()));

        deadlock.setResolutionTime(simParams.getTime());
        simParams.getDeadlockResolutionListener().accept(deadlock, tInfo.transID);
        server.getTM().abort(tInfo.getID());
    }

    @Override
    public void resolveMultiple(List<Deadlock> l) {
        l.forEach(this::resolveDeadlocks);
    }

    @Override
    public void receiveMessage(Message msg) {
        String message = msg.getContents();
        String[] components = message.split(":");
        int transID = Integer.parseInt(components[1]);
        Deadlock deadlock = (Deadlock) msg.getObject();

        if (Log.isLoggingEnabled())
            log.log("ExtratimeDeadlockResolution - Server " + components[2] + " told to abort transaction " + transID);

        deadlock.setResolutionTime(simParams.getTime());
        simParams.getDeadlockResolutionListener().accept(deadlock, transID);
        server.getTM().abort(transID);
    }
}
