package simulator.protocols.deadlockDetection;

import simulator.SimParams;
import simulator.server.Server;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Mani
 */
public class TimeoutDeadlockDetection extends WFG_DDP {
    public TimeoutDeadlockDetection(Server server, SimParams simParams, Consumer<List<Deadlock>> resolver, Consumer<Integer> overheadIncurer) {
        super(server, simParams, resolver, overheadIncurer, null);
    }

}
