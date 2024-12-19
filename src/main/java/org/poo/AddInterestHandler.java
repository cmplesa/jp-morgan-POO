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

public class AddInterestHandler implements CommandHandler {

    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        String accountInterest2 = command.getAccount();

        User userInterest2 = null;
        Account accountInterestRate2 = null;

        // Find the user and the account
        for (User u : users) {
            for (Account accountToFindChange : u.getAccounts()) {
                if (accountToFindChange.getIBAN().equals(accountInterest2) ||
                        (accountToFindChange.getAlias() != null && accountToFindChange.getAlias().equals(accountInterest2))) {
                    userInterest2 = u;
                    accountInterestRate2 = accountToFindChange;
                    break;
                }
            }
            if (userInterest2 != null) break;
        }

        // Handle errors if account is not found or invalid type
        if (accountInterestRate2 == null) {
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

        if (accountInterestRate2.getAccountType().equals("classic")) {
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

        // Calculate and add interest
        double interest = accountInterestRate2.getBalance() * accountInterestRate2.getInterestRate() / 100;
        accountInterestRate2.setBalance(accountInterestRate2.getBalance() + interest);

        // Log the transaction
        ObjectNode addInterest = objectMapper.createObjectNode();
        addInterest.put("timestamp", command.getTimestamp());
        addInterest.put("description", "Interest added");
        addInterest.put("amount", interest);
        addInterest.put("IBAN", accountInterest2);
        userInterest2.getTransactions().add(addInterest);

        // Add success response
        ObjectNode successResponse = objectMapper.createObjectNode();
        successResponse.put("command", command.getCommand());
        successResponse.put("timestamp", command.getTimestamp());
        successResponse.put("description", "Interest successfully added");
        successResponse.put("amount", interest);
        successResponse.put("IBAN", accountInterest2);
        out.add(successResponse);
    }
}