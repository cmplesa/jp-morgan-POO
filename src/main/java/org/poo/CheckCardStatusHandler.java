package org.poo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.poo.CommandHandler;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;
import org.poo.Card;
import org.poo.User;
import org.poo.ExchangeRate;

import java.util.ArrayList;

public class CheckCardStatusHandler implements CommandHandler {

    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        String cardNumberCheck = command.getCardNumber();
        boolean foundCard = false;

        for (User u : users) {
            for (Account accountCheck : u.getAccounts()) {
                for (Card cardCheck : accountCheck.getCards()) {
                    if (cardCheck.getCardNumber().equals(cardNumberCheck)) {
                        ObjectNode checkCardStatus = objectMapper.createObjectNode();
                        if (accountCheck.getBalance() == accountCheck.getMinimumBalance()) {
                            checkCardStatus.put("timestamp", command.getTimestamp());
                            checkCardStatus.put("description", "You have reached the minimum amount of funds, the card will be frozen");
                            cardCheck.setStatus("frozen");
                            u.getTransactions().add(checkCardStatus);
                        }
                        foundCard = true;
                        break;
                    }
                }
                if (foundCard) break;
            }
            if (foundCard) break;
        }

        if (!foundCard) {
            ObjectNode checkCardStatusResponse = objectMapper.createObjectNode();
            checkCardStatusResponse.put("command", command.getCommand());
            ObjectNode info = objectMapper.createObjectNode();
            info.put("timestamp", command.getTimestamp());
            info.put("description", "Card not found");
            checkCardStatusResponse.set("output", info);
            checkCardStatusResponse.put("timestamp", command.getTimestamp());
            out.add(checkCardStatusResponse);
        }
    }
}