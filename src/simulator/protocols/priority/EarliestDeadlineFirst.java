package simulator.protocols.priority;

import simulator.server.lockManager.Lock;
import simulator.server.transactionManager.Transaction;

import java.util.List;

/**
 * Created by Mani
 */
public class EarliestDeadlineFirst implements PriorityProtocol {

    @Override
    public Transaction getHighestPriorityTrans(List<Transaction> transactions) {
        int lowestDeadline = Integer.MAX_VALUE;
        Transaction lowest = null;
        for(Transaction t : transactions)
            if( t.getDeadline() < lowestDeadline ){
                lowest = t;
                lowestDeadline = lowest.getDeadline();
            }
        return lowest;
    }

    @Override
    public Lock getHighestPriorityLock(List<Lock> locks) {
        int lowestDeadline = Integer.MAX_VALUE;
        Lock lowest = null;
        for(Lock t : locks)
            if( t.getDeadline() < lowestDeadline ){
                lowest = t;
                lowestDeadline = lowest.getDeadline();
            }
        return lowest;
    }
}
