package org.poo;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;

import org.poo.fileio.CommandInput;
import org.poo.account.Account;

public class DepositFundsHandler implements CommandHandler {
    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {

        String accountIBAN1 = command.getAccount();
        double amount = command.getAmount();

        User user2 = null;

        // Find user and account based on IBAN or alias
        for (User u : users) {
            for (Account account2 : u.getAccounts()) {
                if (account2.getIBAN().equals(accountIBAN1) ||
                        (account2.getAlias() != null && account2.getAlias().equals(accountIBAN1))) {
                    user2 = u;
                    break;
                }
            }
        }

        if (user2 == null) {
            System.out.println("User not found");
            // În enunț nu pare să fie cerut un output special pentru acest caz
            return;
        }

        for (Account account2 : user2.getAccounts()) {
            if (account2.getIBAN().equals(accountIBAN1) ||
                    (account2.getAlias() != null && account2.getAlias().equals(accountIBAN1))) {
                account2.deposit(amount);
                break;
            }
        }
    }
}