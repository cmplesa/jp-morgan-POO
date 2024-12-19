package org.poo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.poo.CommandHandler;
import org.poo.ExchangeRate;
import org.poo.User;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;

import java.util.ArrayList;

public class ChangeInterestRateHandler implements CommandHandler {

    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        String accountInterest = command.getAccount();
        Double interestRateChange = command.getInterestRate();

        User userInterest = null;
        Account accountInterestRate = null;

        // Find the user and the account
        for (User u : users) {
            for (Account accountToFindChange : u.getAccounts()) {
                if (accountToFindChange.getIBAN().equals(accountInterest) ||
                        (accountToFindChange.getAlias() != null && accountToFindChange.getAlias().equals(accountInterest))) {
                    userInterest = u;
                    accountInterestRate = accountToFindChange;
                    break;
                }
            }
            if (userInterest != null) break;
        }

        // Handle error if account is not found
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

        // Check if the account type is not "savings"
        if (accountInterestRate.getAccountType().equals("classic")) {
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

        // Change the interest rate
        accountInterestRate.setInterestRate(interestRateChange);

        // Log the transaction
        ObjectNode transactionInterest = objectMapper.createObjectNode();
        transactionInterest.put("timestamp", command.getTimestamp());
        transactionInterest.put("description", "Interest rate of the account changed to " + interestRateChange);
        userInterest.getTransactions().add(transactionInterest);

        // Add success response
        ObjectNode successResponse = objectMapper.createObjectNode();
        successResponse.put("command", command.getCommand());
        successResponse.put("timestamp", command.getTimestamp());
        successResponse.put("description", "Interest rate successfully changed");
//        out.add(successResponse);
    }
}