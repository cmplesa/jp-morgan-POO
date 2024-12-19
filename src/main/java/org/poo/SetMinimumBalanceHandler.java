package org.poo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.poo.CommandHandler;
import org.poo.User;
import org.poo.account.Account;
import org.poo.fileio.CommandInput;
import org.poo.ExchangeRate;

import java.util.ArrayList;

public class SetMinimumBalanceHandler implements CommandHandler {
    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        String accountIBAN4 = command.getAccount();
        double minBalance = command.getAmount();

        // Get the account and set the minimum balance
        User user5 = null;
        for (User u : users) {
            for (Account account5 : u.getAccounts()) {
                if (account5.getIBAN().equals(accountIBAN4) || (account5.getAlias() != null && account5.getAlias().equals(accountIBAN4))) {
                    user5 = u;
                    account5.setMinimumBalance(minBalance);
                    break;
                }
            }
            if (user5 != null) break;
        }

        if (user5 == null) {
            // Log an error response if account not found
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "Account not found");
            errorResponse.put("timestamp", command.getTimestamp());
            ObjectNode response = objectMapper.createObjectNode();
            response.put("command", command.getCommand());
            response.set("output", errorResponse);
            out.add(response);
            return;
        }

        // Log the response
        ObjectNode successResponse = objectMapper.createObjectNode();
        successResponse.put("timestamp", command.getTimestamp());
        successResponse.put("description", "Minimum balance updated successfully");
        successResponse.put("account", accountIBAN4);
        successResponse.put("minimumBalance", minBalance);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("command", command.getCommand());
        response.set("output", successResponse);
//        out.add(response);
    }
}