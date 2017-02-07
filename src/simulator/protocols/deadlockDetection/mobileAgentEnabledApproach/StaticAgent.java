package simulator.protocols.deadlockDetection.mobileAgentEnabledApproach;

import simulator.SimParams;
import simulator.protocols.deadlockDetection.Deadlock;
import simulator.protocols.deadlockDetection.WFG.Graph;
import simulator.protocols.deadlockDetection.WFG.Task;
import simulator.protocols.deadlockDetection.WFG.WFGNode;
import simulator.server.Server;
import simulator.server.transactionManager.TransInfo;
import ui.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static simulator.protocols.deadlockDetection.WFG_DDP.convertToList;

public class StaticAgent {
    private final MAEDD maedd;
    private final Log log;
    private final Server server;
    private final SimParams simParams;
    private List<List<WFGNode>> deadlocks = new LinkedList<>();

    private List<Integer> S_List = new LinkedList<>();

    public StaticAgent(MAEDD maedd, Server server) {
        this.maedd = maedd;
        log = maedd.log;
        this.server = server;
        simParams = server.getSimParams();
    }

    protected void searchGraph(Graph<WFGNode> build) {
        if (Log.isLoggingEnabled())
            log.log("MAEDD- Searching graph");

        maedd.calculateAndIncurOverhead(build);
        deadlocks.clear();

        List<Integer> serversInvolved = maedd.allServers;

        List<Task<WFGNode>> allTransactions = new ArrayList<>(build.getTasks());
        List<TransInfo> transThisAgentCaresAbout = new ArrayList<>();

        //collect all transactions this agent cares about
        for (int i = 0; i < allTransactions.size(); i++) {
            //if (i % serversInvolved.size() == thisNodesIndex)
            int transactionServer = ((TransInfo) allTransactions.get(i).getId()).serverID;
            if( transactionServer == server.getID())
                transThisAgentCaresAbout.add((TransInfo) allTransactions.get(i).getId());
            else
                S_List.add(transactionServer);
        }

        if (transThisAgentCaresAbout.isEmpty()) {
            if (Log.isLoggingEnabled())
                log.log("No transactions for this agent");

            return;
        }

        if (Log.isLoggingEnabled())
            log.log("This agent cares about - " + transThisAgentCaresAbout);


        // MANI: CHANGE!!!
        //Get transInfo and start searching through its children
        for (TransInfo t : transThisAgentCaresAbout) {

            //Convert the TransInfo list to list of WFGNodes

            List<Task<WFGNode>> edgesFrom = build.getEdgesFrom(t);

            followCycle(t, edgesFrom, new LinkedList<>());
        }

        //Convert the list of lists of WFGNodes to a list of lists of Deadlocks
        List<List<TransInfo>> deadlocksTransInfo = new ArrayList<>();
        List<Deadlock> deadlocksList = new ArrayList<>();

        deadlocks.forEach(deadlock -> {
            List<TransInfo> deadlockTransInfo = new ArrayList<>();
            deadlock.forEach(wfgNode -> deadlockTransInfo.add((TransInfo) wfgNode));

            //If the deadlock was detected twice, ignore it the second time.
            if (!deadlocksTransInfo.contains(deadlockTransInfo)) {
                deadlocksTransInfo.add(deadlockTransInfo);

                deadlocksList.add(new Deadlock(deadlockTransInfo, server.getID(), simParams.getTime(), false));
            }
        });

        if (deadlocksList.isEmpty()) {
            if (Log.isLoggingEnabled())
                log.log("Found no local deadlocks");

            return;
        }

        deadlocksList.forEach(maedd.getDeadlockListener());
        if (Log.isLoggingEnabled())
            log.log("Found local deadlocks - " + deadlocksTransInfo + " on server " + server.getID());

        //Resolve the deadlocks
        maedd.getResolver().accept(deadlocksList);
    }

    private void followCycle(WFGNode lookingFor, List<Task<WFGNode>> edges, List<WFGNode> path) {
        if (edges.isEmpty())
            return;

        for (Task<WFGNode> t : edges) {
            WFGNode edge = t.getId();

            if (edge == lookingFor) {
                path.add(edge);
                LinkedList<WFGNode> deadlockPath = new LinkedList<>(path);
                deadlockPath.addFirst(deadlockPath.removeLast());
                deadlocks.add(deadlockPath);
                if (Log.isLoggingEnabled())
                    log.log("Found local deadlock path- " + deadlockPath);

                path.remove(edge);
            } else if ((edge.getID() > lookingFor.getID() && !path.contains(edge))) {
                path.add(edge);
                followCycle(lookingFor, convertToList(t.getWaitsForTasks()), path);
                path.remove(edge);
            }
        }
    }
}
