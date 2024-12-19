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
import java.util.Iterator;

public class DeleteCardHandler implements CommandHandler {
    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        String cardNumber = command.getCardNumber();
        String email3 = command.getEmail();

        User user34 = null;
        for (User u : users) {
            if (u.getEmail().equals(email3)) {
                user34 = u;
                break;
            }
        }

        if (user34 == null) {
            return;
        }

        // Find the IBAN of the account that has the card
        String accountIBAN34 = "";
        for (Account account34 : user34.getAccounts()) {
            for (Card card34 : account34.getCards()) {
                if (card34.getCardNumber().equals(cardNumber)) {
                    accountIBAN34 = account34.getIBAN();
                    break;
                }
            }
            if (!accountIBAN34.equals("")) {
                break;
            }
        }

        boolean cardDeleted = false;
        for (Account account4 : user34.getAccounts()) {
            Iterator<Card> it = account4.getCards().iterator();
            while (it.hasNext()) {
                Card c = it.next();
                if (c.getCardNumber().equals(cardNumber)) {
                    it.remove();
                    cardDeleted = true;
                    break;
                }
            }
            if (cardDeleted) break;
        }

        if (!cardDeleted) {
            return;
        }

        ObjectNode deleteCard = objectMapper.createObjectNode();
        deleteCard.put("timestamp", command.getTimestamp());
        deleteCard.put("description", "The card has been destroyed");
        deleteCard.put("card", cardNumber);
        deleteCard.put("cardHolder", email3);
        deleteCard.put("account", accountIBAN34);
        user34.getTransactions().add(deleteCard);

        ObjectNode response = objectMapper.createObjectNode();
        response.put("command", command.getCommand());
        response.set("output", deleteCard);
    }
}