package org.poo.StrategyHandler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.poo.Components.ExchangeRate;
import org.poo.Components.User;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;

import java.util.ArrayList;

/**
 * Handles adding interest to savings accounts.
 */
public final class AddInterestHandler implements CommandHandler {

    /**
     * Executes the add interest command.
     *
     * @param command       The command input containing details for the operation.
     * @param users         The list of users in the system.
     * @param exchangeRates The list of exchange rates (unused in this handler).
     * @param out           The output node to capture results.
     */
    @Override
    public void execute(final CommandInput command, final ArrayList<User> users,
                        final ArrayList<ExchangeRate> exchangeRates, final ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        final String accountInterest = command.getAccount();

        User userInterest = null;
        Account accountInterestRate = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIBAN().equals(accountInterest)
                        || (account.getAlias() != null && account
                        .getAlias().equals(accountInterest))) {
                    userInterest = user;
                    accountInterestRate = account;
                    break;
                }
            }
            if (userInterest != null) {
                break;
            }
        }

        if (accountInterestRate == null) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("command", command.getCommand());

            ObjectNode errorNotFound = objectMapper.createObjectNode();
            errorNotFound.put("timestamp", command.getTimestamp());
            errorNotFound.put("description", "Account not found");

            error.set("output", errorNotFound);
            error.put("timestamp", command.getTimestamp());
            out.add(error);
            return;
        }

        if ("classic".equals(accountInterestRate.getAccountType())) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("command", command.getCommand());

            ObjectNode errorNotSavings = objectMapper.createObjectNode();
            errorNotSavings.put("timestamp", command.getTimestamp());
            errorNotSavings.put("description", "This is not a savings account");

            error.set("output", errorNotSavings);
            error.put("timestamp", command.getTimestamp());
            out.add(error);
            return;
        }

        final double interestRate = accountInterestRate.getInterestRate();
        final double interest = accountInterestRate.getBalance() * interestRate / 100.0;
        accountInterestRate.setBalance(accountInterestRate.getBalance() + interest);

        ObjectNode addInterest = objectMapper.createObjectNode();
        addInterest.put("timestamp", command.getTimestamp());
        addInterest.put("description", "Interest added");
        addInterest.put("amount", interest);
        addInterest.put("IBAN", accountInterest);
        userInterest.getTransactions().add(addInterest);

        ObjectNode successResponse = objectMapper.createObjectNode();
        successResponse.put("command", command.getCommand());
        successResponse.put("timestamp", command.getTimestamp());
        successResponse.put("description", "Interest successfully added");
        successResponse.put("amount", interest);
        successResponse.put("IBAN", accountInterest);
        out.add(successResponse);
    }
}
