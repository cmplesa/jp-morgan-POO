package org.poo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;

public class PrintUsersHandler implements CommandHandler {

    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create the main response node
        ObjectNode printUsers = objectMapper.createObjectNode();
        printUsers.put("command", command.getCommand());

        // Create an array for users
        ArrayNode usersArray = objectMapper.createArrayNode();

        for (User user : users) {
            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("firstName", user.getFirstName());
            userNode.put("lastName", user.getLastName());
            userNode.put("email", user.getEmail());

            // Create an array for the user's accounts
            ArrayNode accountsArray = objectMapper.createArrayNode();
            if (user.getAccounts() != null) {
                for (Account account : user.getAccounts()) {
                    ObjectNode accountNode = objectMapper.createObjectNode();
                    accountNode.put("IBAN", account.getIBAN());
                    accountNode.put("balance", account.getBalance());
                    accountNode.put("currency", account.getCurrency());
                    accountNode.put("type", account.getAccountType());

                    // Create an array for the cards associated with the account
                    ArrayNode cardsArray = objectMapper.createArrayNode();
                    if (account.getCards() != null) {
                        for (Card card : account.getCards()) {
                            ObjectNode cardNode = objectMapper.createObjectNode();
                            cardNode.put("cardNumber", card.getCardNumber());
                            cardNode.put("status", card.getStatus());
                            cardsArray.add(cardNode);
                        }
                    }
                    accountNode.set("cards", cardsArray);
                    accountsArray.add(accountNode);
                }
            }
            userNode.set("accounts", accountsArray);
            usersArray.add(userNode);
        }

        // Add users to the response
        printUsers.set("output", usersArray);
        printUsers.put("timestamp", command.getTimestamp());

        // Add the main node to the output array
        out.add(printUsers);
    }
}