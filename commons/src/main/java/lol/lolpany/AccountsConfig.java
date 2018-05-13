package lol.lolpany;

import java.util.List;

/**
 * For synchronization between AccountsConfigWatcher and Solver.
 * Also wrapper for gson.
 */
public class AccountsConfig<T> {
     public List<Account<T>> accountsConfig;
}
