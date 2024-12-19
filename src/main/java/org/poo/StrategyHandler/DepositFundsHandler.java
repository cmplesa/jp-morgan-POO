package org.poo.StrategyHandler;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;

import org.poo.Components.ExchangeRate;
import org.poo.Components.User;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;

/**
 * Handles the deposit of funds into a user's account.
 */
public final class DepositFundsHandler implements CommandHandler {

    /**
     * Executes the deposit funds command.
     *
     * @param command        The command input containing the details of the deposit.
     * @param users          The list of users.
     * @param exchangeRates  The list of exchange rates.
     * @param out            The JSON array to which the result will be appended.
     */
    @Override
    public void execute(final CommandInput command, final ArrayList<User> users,
                        final ArrayList<ExchangeRate> exchangeRates, final ArrayNode out) {

        String accountIBAN1 = command.getAccount();
        double amount = command.getAmount();

        User user2 = null;

        for (User u : users) {
            for (Account account2 : u.getAccounts()) {
                if (account2.getIBAN().equals(accountIBAN1)
                        || (account2.getAlias() != null
                        && account2.getAlias().equals(accountIBAN1))) {
                    user2 = u;
                    break;
                }
            }
        }

        if (user2 == null) {
            System.out.println("User not found");
            return;
        }

        for (Account account2 : user2.getAccounts()) {
            if (account2.getIBAN().equals(accountIBAN1)
                    || (account2.getAlias() != null
                    && account2.getAlias().equals(accountIBAN1))) {
                account2.deposit(amount);
                break;
            }
        }
    }
}
