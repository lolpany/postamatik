package lol.lolpany.postamatik;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * For synchronization between AccountsConfigWatcher and Solver.
 * Also wrapper for gson.
 */
public class AccountsConfig {
     List<Account> accountsConfig;
}
