package simulator.protocols.priority;

import simulator.server.lockManager.Lock;
import simulator.server.transactionManager.Transaction;

import java.util.List;

/**
 * Created by Mani
 */
public class NoPriorityProtocol implements PriorityProtocol {
    @Override
    public Transaction getHighestPriorityTrans(List<Transaction> transactions) {
        int lowestID = Integer.MAX_VALUE;
        Transaction lowest = null;
        for(Transaction t : transactions)
            if( t.getID() < lowestID ){
                lowest = t;
                lowestID = lowest.getID();
            }
        return lowest;
    }

    @Override
    public Lock getHighestPriorityLock(List<Lock> locks) {
        int lowestID = Integer.MAX_VALUE;
        Lock lowest = null;
        for(Lock t : locks)
            if( t.getTransID() < lowestID ){
                lowest = t;
                lowestID = lowest.getTransID();
            }
        return lowest;
    }
}
