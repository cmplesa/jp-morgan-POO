package org.poo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;
import java.util.ArrayList;

public class DeleteAccountHandler implements CommandHandler {

    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode deleteAccount = objectMapper.createObjectNode();
        deleteAccount.put("command", command.getCommand());
        deleteAccount.put("timestamp", command.getTimestamp());

        String accountIBANDelete = command.getAccount();
        String emailDelete = command.getEmail();

        User userDelete = null;

        // Find user by email
        for (User u : users) {
            if (u.getEmail().equals(emailDelete)) {
                userDelete = u;
                break;
            }
        }

        if (userDelete == null) {
            System.out.println("User not found");
            ObjectNode output = objectMapper.createObjectNode();
            output.put("error", "User not found");
            deleteAccount.set("output", output);
            out.add(deleteAccount);
            return;
        }

        boolean foundAccount = false;
        boolean accountFound = false;
        for (Account accountDelete : userDelete.getAccounts()) {
            if (accountDelete.getIBAN().equals(accountIBANDelete)) {
                accountFound = true;

                // Check if account balance is zero
                if (accountDelete.getBalance() != 0) {
                    ObjectNode errorResponse = objectMapper.createObjectNode();
                    errorResponse.put("command", command.getCommand());
                    ObjectNode output = objectMapper.createObjectNode();
                    output.put("error", "Account couldn't be deleted - see org.poo.transactions for details");
                    output.put("timestamp", command.getTimestamp());
                    errorResponse.set("output", output);
                    errorResponse.put("timestamp", command.getTimestamp());
                    out.add(errorResponse);

                    ObjectNode transDelete = objectMapper.createObjectNode();
                    transDelete.put("timestamp", command.getTimestamp());
                    transDelete.put("description", "Account couldn't be deleted - there are funds remaining");

                    userDelete.getTransactions().add(transDelete);
                    foundAccount = true;
                    break;
                }

                // Destroy all cards associated with the account
                accountDelete.getCards().clear();

                // Remove account from user's list
                userDelete.getAccounts().remove(accountDelete);

                // Success node
                ObjectNode output = objectMapper.createObjectNode();
                output.put("success", "Account deleted");
                output.put("timestamp", command.getTimestamp());
                deleteAccount.set("output", output);

                break;
            }
        }

        if (foundAccount) {
            return;
        }

        if (!accountFound) {
            System.out.println("Account not found");
            ObjectNode output = objectMapper.createObjectNode();
            output.put("error", "Account not found");
            deleteAccount.set("output", output);
            out.add(deleteAccount);
            return;
        }

        // Add node to general response
        out.add(deleteAccount);
    }
}