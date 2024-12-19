package org.poo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import org.poo.fileio.CommandInput;
import org.poo.account.Account;
import org.poo.Card;
import org.poo.User;
import org.poo.ExchangeRate;

public class PayOnlineHandler implements CommandHandler {

    private double convertCurrency(String from, String to, double amount, ArrayList<ExchangeRate> exchangeRates) {
        Map<String, List<ExchangeRate>> graph = new HashMap<>();
        for (ExchangeRate rate : exchangeRates) {
            graph.putIfAbsent(rate.getCurrencyFrom(), new ArrayList<>());
            graph.putIfAbsent(rate.getCurrencyTo(), new ArrayList<>());
            graph.get(rate.getCurrencyFrom()).add(rate);
            graph.get(rate.getCurrencyTo()).add(new ExchangeRate(rate.getCurrencyTo(), rate.getCurrencyFrom(), 1 / rate.getRate()));
        }

        Queue<Pair<String, Double>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(new Pair<>(from, 1.0));
        visited.add(from);

        while (!queue.isEmpty()) {
            Pair<String, Double> current = queue.poll();
            String currentCurrency = current.getKey();
            double currentRate = current.getValue();

            if (currentCurrency.equals(to)) {
                return amount * currentRate;
            }

            if (graph.containsKey(currentCurrency)) {
                for (ExchangeRate rate : graph.get(currentCurrency)) {
                    if (!visited.contains(rate.getCurrencyTo())) {
                        visited.add(rate.getCurrencyTo());
                        queue.add(new Pair<>(rate.getCurrencyTo(), currentRate * rate.getRate()));
                    }
                }
            }
        }
        return -1; // Placeholder return value
    }

    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        String cardNumberPayment = command.getCardNumber();
        double amountPayment = command.getAmount();
        String currencyPayment = command.getCurrency();
        String commerciant = command.getCommerciant();
        String description = command.getDescription();
        String emailPayment = command.getEmail();

        User userPayment = null;
        // Find the user
        for (User u : users) {
            if (u.getEmail().equals(emailPayment)) {
                userPayment = u;
                break;
            }
        }

        // Check if user exists
        if (userPayment == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "User not found");
            ObjectNode payOnlineResponse = objectMapper.createObjectNode();
            payOnlineResponse.put("command", command.getCommand());
            payOnlineResponse.put("timestamp", command.getTimestamp());
            payOnlineResponse.set("output", errorResponse);
            out.add(payOnlineResponse);
            return;
        }

        Account accountToPay = null;
        Card cardToPay = null;

        // Find the card and associated account
        for (Account account34 : userPayment.getAccounts()) {
            for (Card card34 : account34.getCards()) {
                if (card34.getCardNumber().equals(cardNumberPayment)) {
                    accountToPay = account34;
                    cardToPay = card34;
                    break;
                }
            }
            if (accountToPay != null) break;
        }

        // Check if card exists and is associated with the account
        if (accountToPay == null || cardToPay == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("timestamp", command.getTimestamp());
            errorResponse.put("description", "Card not found");
            ObjectNode payOnlineResponse = objectMapper.createObjectNode();
            payOnlineResponse.put("command", command.getCommand());
            payOnlineResponse.set("output", errorResponse);
            payOnlineResponse.put("timestamp", command.getTimestamp());
            out.add(payOnlineResponse);
            return;
        }

        // Currency conversion if necessary
        double convertedAmount = amountPayment;
        if (!accountToPay.getCurrency().equals(currencyPayment)) {
            convertedAmount = convertCurrency(currencyPayment, accountToPay.getCurrency(), amountPayment, exchangeRates);

            if (convertedAmount == -1) {
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("error", "Exchange rate path not found");
                ObjectNode payOnlineResponse = objectMapper.createObjectNode();
                payOnlineResponse.put("command", command.getCommand());
                payOnlineResponse.put("timestamp", command.getTimestamp());
                payOnlineResponse.set("output", errorNode);
                out.add(payOnlineResponse);
                return;
            }
        }

        if (cardToPay.getStatus().equals("frozen")) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("timestamp", command.getTimestamp());
            errorNode.put("description", "The card is frozen");
            userPayment.getTransactions().add(errorNode);
            return;
        }

        // Check if there are sufficient funds
        if (accountToPay.getBalance() < convertedAmount) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("timestamp", command.getTimestamp());
            errorNode.put("description", "Insufficient funds");
            userPayment.getTransactions().add(errorNode);
            return;
        }

        if ((accountToPay.getBalance() - convertedAmount) == accountToPay.getMinimumBalance()) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("timestamp", command.getTimestamp());
            errorNode.put("description", "You have reached the minimum amount of funds, the card will be frozen");
            userPayment.getTransactions().add(errorNode);
            return;
        }

        if (accountToPay.getBalance() - convertedAmount < accountToPay.getMinimumBalance()) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("timestamp", command.getTimestamp());
            errorNode.put("description", "The card is frozen");
            userPayment.getTransactions().add(errorNode);
            return;
        }

        accountToPay.setBalance(accountToPay.getBalance() - convertedAmount);

        // Perform the payment
        ObjectNode transactionNode = objectMapper.createObjectNode();
        transactionNode.put("timestamp", command.getTimestamp());
        transactionNode.put("description", "Card payment");
        transactionNode.put("amount", convertedAmount);
        transactionNode.put("commerciant", commerciant);
        userPayment.getTransactions().add(transactionNode);

        ObjectNode onlinePayment = objectMapper.createObjectNode();
        onlinePayment.put("timestamp", command.getTimestamp());
        onlinePayment.put("description", "Card payment");
        onlinePayment.put("amount", convertedAmount);
        onlinePayment.put("commerciant", commerciant);
        onlinePayment.put("IBAN", accountToPay.getIBAN());
        userPayment.getOnlinePayments().add(onlinePayment);

        if (cardToPay.getCardType().equals("OneTime")) {
            // Remove the used card
            accountToPay.getCards().remove(cardToPay);

            // Create a new one-time pay card
            Card newCard = new Card("OneTime");
            accountToPay.getCards().add(newCard);

            // Transaction for card destruction
            ObjectNode deleteCardOne = objectMapper.createObjectNode();
            deleteCardOne.put("timestamp", command.getTimestamp());
            deleteCardOne.put("description", "The card has been destroyed");
            deleteCardOne.put("card", cardNumberPayment);
            deleteCardOne.put("cardHolder", emailPayment);
            deleteCardOne.put("account", accountToPay.getIBAN());
            userPayment.getTransactions().add(deleteCardOne);

            // Add transaction for generating the new card
            ObjectNode cardGeneratedNode = objectMapper.createObjectNode();
            cardGeneratedNode.put("timestamp", command.getTimestamp());
            cardGeneratedNode.put("description", "New card created");
            cardGeneratedNode.put("card", newCard.getCardNumber());
            cardGeneratedNode.put("cardHolder", emailPayment);
            cardGeneratedNode.put("account", accountToPay.getIBAN());
            userPayment.getTransactions().add(cardGeneratedNode);
        }
    }
}