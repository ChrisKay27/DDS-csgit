package simulator.protocols.priority;

import exceptions.WTFException;
import simulator.server.lockManager.Lock;
import simulator.server.transactionManager.Transaction;

import java.util.List;

/**
 * Created by Mani
 */
public interface PriorityProtocol {

    Transaction getHighestPriorityTrans(List<Transaction> transactions);
    Lock getHighestPriorityLock(List<Lock> locks);


    static PriorityProtocol getPp(String pp) {
        switch(pp){
            case "NoPriorityProtocol": return new NoPriorityProtocol();
            case "EarliestDeadlineFirst": return new EarliestDeadlineFirst();
        }
        throw new WTFException("Priority Protocol not registered! add them in the PriorityProtocol class!");
    }
}
