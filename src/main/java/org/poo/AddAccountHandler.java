package org.poo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;
import org.poo.account.AccountFactory;

/**
 * Handles the "addAccount" command.
 */
public class AddAccountHandler implements CommandHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        // Create response for the new account creation
        ObjectNode addAccount = objectMapper.createObjectNode();
        addAccount.put("timestamp", command.getTimestamp());
        addAccount.put("description", "New account created");

        // Find the user by email
        String email = command.getEmail();
        User user = null;
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                user = u;
                break;
            }
        }

        // Handle user not found
        if (user == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "User not found");

            ObjectNode addAccountResponse = objectMapper.createObjectNode();
            addAccountResponse.put("command", command.getCommand());
            addAccountResponse.put("timestamp", command.getTimestamp());
            addAccountResponse.set("output", errorResponse);

            out.add(addAccountResponse);
            return;
        }

        // Add transaction log to the user
        user.getTransactions().add(addAccount);

        // Create the account based on the command input
        String type = command.getAccountType();
        String currency = command.getCurrency();
        double interestRate = "savings".equals(type) ? command.getInterestRate() : 0.0;

        Account account = AccountFactory.createAccount(type, currency, interestRate);
        user.getAccounts().add(account);

    }
}
