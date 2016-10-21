package simulator.protocols.deadlockDetection;

import exceptions.WTFException;
import simulator.SimParams;
import simulator.enums.ServerProcess;
import simulator.eventQueue.Event;
import simulator.protocols.deadlockDetection.AgentDDP.AgentDeadlockDetectionProtocol;
import simulator.protocols.deadlockDetection.ChandyMisraHaas.ChandyMisraHaasDDP;
import simulator.protocols.deadlockDetection.WFG.Graph;
import simulator.protocols.deadlockDetection.WFG.GraphBuilder;
import simulator.protocols.deadlockDetection.WFG.WFGNode;
import simulator.server.Server;
import simulator.server.network.Message;
import ui.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Created by Mani
 */
public class DeadlockDetectionProtocol {

    private final Log log;
    protected Consumer<Deadlock> deadlockListener;
    protected final int serverID;
    protected final SimParams simParams;
    protected final Consumer<Event> eventQueue;
    protected final Consumer<List<Deadlock>> resolver;
    protected final Consumer<Integer> overheadIncurer;
    protected final Server server;




    public DeadlockDetectionProtocol(Server server, SimParams simParams, Consumer<List<Deadlock>> resolver, Consumer<Integer> overheadIncurer, Consumer<Deadlock> deadlockListener) {
        this.server = server;
        this.serverID = server.getID();
        this.simParams = simParams;
        this.eventQueue = simParams.eventQueue;
        this.resolver = resolver;
        this.overheadIncurer = overheadIncurer;
        log = new Log(ServerProcess.DDP,serverID,simParams.timeProvider,simParams.log);

        this.deadlockListener = deadlockListener;
    }


    public void start(){
        if(Log.isLoggingEnabled()) log.log("Start");
    }



    /**
     */
    public void receiveMessage(Message msg) {
        if(Log.isLoggingEnabled()) log.log("Received message: " + msg);

    }


    public void setGraphListener(BiConsumer<Graph<WFGNode>,Integer> consumer){

    }


    public static DeadlockDetectionProtocol get(Server server, String ddp, Consumer<Deadlock> deadlockListener) {
        switch (ddp){
            case "AgentDeadlockDetectionProtocol": return new AgentDeadlockDetectionProtocol(server,server.getSimParams(),server.getDRP()::resolveMultiple,server::incurOverhead, deadlockListener);
            case "TimeoutDeadlockDetection": return new TimeoutDeadlockDetection(server,server.getSimParams(),server.getDRP()::resolveMultiple,server::incurOverhead);
            case "ChandyMisraHaasDDP": return new ChandyMisraHaasDDP(server,server.getSimParams(),server.getDRP()::resolveMultiple,server::incurOverhead, deadlockListener);
//            case "TimeoutDeadlockDetection": return new TimeoutDeadlockDetection(server,server.getSimParams(),server.getDRP()::resolveMultiple,server::incurOverhead);
        }
        throw new WTFException("Deadlock Detection Protocol has not been registered! add it here in the WFG_DDP class!");
    }

}
