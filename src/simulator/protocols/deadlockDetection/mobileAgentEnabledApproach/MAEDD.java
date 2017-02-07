package simulator.protocols.deadlockDetection.mobileAgentEnabledApproach;

import simulator.SimParams;
import simulator.enums.ServerProcess;
import simulator.eventQueue.Event;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.protocols.deadlockDetection.WFG.Graph;
import simulator.protocols.deadlockDetection.WFG.GraphBuilder;
import simulator.protocols.deadlockDetection.WFG.Task;
import simulator.protocols.deadlockDetection.WFG.WFGNode;
import simulator.protocols.deadlockDetection.WFG_DDP;
import simulator.server.Server;
import simulator.server.lockManager.Lock;
import ui.Log;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class MAEDD extends WFG_DDP {

    protected final Log log;
    protected List<Integer> allServers = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7);

    private final StaticAgent staticAgent;
    private MobileAgent fmdda, bmdda;

    private final String BACKWARD = "BACKWARD";
    private final String FORWARD = "FORWARD";

    public MAEDD(Server server, SimParams simParams, Consumer<List<Deadlock>> resolver, Consumer<Integer> overheadIncurer, Consumer<Deadlock> deadlockListener) {
        super(server, simParams, resolver, overheadIncurer, deadlockListener);
        simParams.usesWFG = true;
        log = new Log(ServerProcess.DDP, server.getID(), simParams.timeProvider, simParams.log);

        staticAgent = new StaticAgent(this, server);
        fmdda = new MobileAgent(this, server, BACKWARD);
        bmdda = new MobileAgent(this, server, FORWARD);
    }

    @Override
    public void start() {
        eventQueue.accept(new Event(simParams.getTime() + simParams.getDeadlockDetectInterval(), serverID, this::startDetectionIteration));
    }

    protected void searchGraph(Graph<WFGNode> build) {
        staticAgent.searchGraph(build);
    }

    protected void calculateAndIncurOverhead(Graph<WFGNode> WFG) {
        //calc overhead
        int overhead = 0;
        for (Task<WFGNode> rt : WFG.getTasks()) {
            //add one for each vertex
            overhead++;

            //add one for each edge
            overhead += rt.getWaitsForTasks().size();
        }

        if (Log.isLoggingEnabled())
            log.log("Incurring overhead- " + overhead);

        overheadIncurer.accept(overhead);
    }

    /**
     * This is called when a WFG is received
     */
    public void updateWFGraph(Graph<WFGNode> graph, int server) {
        if (Log.isLoggingEnabled())
            log.log("Updating graph (created at " + graph.getCreationTime() + ") with waits from server " + server);

        fmdda.updateWFGraph(graph, server);
        bmdda.updateWFGraph(graph, server);
        wfgBuilder = new GraphBuilder<>();
    }
}
