package simulator.protocols.priority;

import simulator.server.lockManager.Lock;
import simulator.server.transactionManager.Transaction;
import java.util.List;

public class LeastSlackFirst implements PriorityProtocol {

    @Override
    public Transaction getHighestPriorityTrans(List<Transaction> transactions) {
        int lowestSlackTime = Integer.MAX_VALUE;
        Transaction lowest = null;
        for(Transaction t : transactions)
            if( t.getSlackTime() < lowestSlackTime ){
                lowest = t;
                lowestSlackTime = lowest.getSlackTime();
            }
        return lowest;
    }

    @Override
    public Lock getHighestPriorityLock(List<Lock> locks) {
        Lock lowest = null;
        return lowest;
    }
}
