package org.poo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;
import org.poo.Card;
import org.poo.User;
import org.poo.ExchangeRate;

import java.util.ArrayList;
import java.util.List;

public class CreateCardHandler implements CommandHandler {

    private final List<User> users;
    private final ObjectMapper objectMapper;

    public CreateCardHandler(List<User> users, ObjectMapper objectMapper) {
        this.users = users;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        String accountIBAN = command.getAccount();
        String email1 = command.getEmail();

        User user1 = null;

        for (User u : users) {
            if (u.getEmail().equals(email1)) {
                user1 = u;
                break;
            }
        }

        if (user1 == null) {
            return;
        }

        Card card = new Card("permanent");

        boolean accountFound = false;
        for (Account account1 : user1.getAccounts()) {
            if (account1.getIBAN().equals(accountIBAN) || (account1.getAlias() != null && account1.getAlias().equals(accountIBAN))) {
                account1.getCards().add(card);
                accountFound = true;
                break;
            }
        }

        if (!accountFound) {
            return;
        }

        ObjectNode createCardResponse = objectMapper.createObjectNode();
        createCardResponse.put("timestamp", command.getTimestamp());
        createCardResponse.put("description", "New card created");
        createCardResponse.put("card", card.getCardNumber());
        createCardResponse.put("cardHolder", email1);
        createCardResponse.put("account", accountIBAN);
        user1.getTransactions().add(createCardResponse);

    }

    private ObjectNode createErrorResponse(String message, CommandInput command) {
        ObjectNode errorResponse = objectMapper.createObjectNode();
        errorResponse.put("command", command.getCommand());
        errorResponse.put("timestamp", command.getTimestamp());
        ObjectNode output = objectMapper.createObjectNode();
        output.put("error", message);
        errorResponse.set("output", output);
        return errorResponse;
    }
}