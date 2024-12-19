package org.poo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;
import org.poo.Card;
import org.poo.User;
import org.poo.ExchangeRate;

import java.util.ArrayList;

public class CreateOneTimeCardHandler implements CommandHandler {
    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        String accountIBAN2 = command.getAccount();
        String email2 = command.getEmail();

        User user3 = null;
        for (User u : users) {
            if (u.getEmail().equals(email2)) {
                user3 = u;
                break;
            }
        }

        if (user3 == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "User not found");
            ObjectNode createOneTimeCardResponse = objectMapper.createObjectNode();
            createOneTimeCardResponse.put("command", command.getCommand());
            createOneTimeCardResponse.put("timestamp", command.getTimestamp());
            createOneTimeCardResponse.set("output", errorResponse);
            out.add(createOneTimeCardResponse);
            return;
        }

        Card card2 = new Card("OneTime");
        for (Account account3 : user3.getAccounts()) {
            if (account3.getIBAN().equals(accountIBAN2) || (account3.getAlias() != null && account3.getAlias().equals(accountIBAN2))) {
                account3.getCards().add(card2);
                break;
            }
        }

        ObjectNode createOneTimeCardResponse = objectMapper.createObjectNode();
        createOneTimeCardResponse.put("timestamp", command.getTimestamp());
        createOneTimeCardResponse.put("description", "New card created");
        createOneTimeCardResponse.put("card", card2.getCardNumber());
        createOneTimeCardResponse.put("cardHolder", email2);
        createOneTimeCardResponse.put("account", accountIBAN2);
        user3.getTransactions().add(createOneTimeCardResponse);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("command", command.getCommand());
        response.set("output", createOneTimeCardResponse);
//        out.add(response);
    }
}