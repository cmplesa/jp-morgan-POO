package org.poo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import org.poo.fileio.CommandInput;
import org.poo.account.Account;

public class SendMoneyHandler implements CommandHandler {
    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        String senderIBAN = command.getAccount();
        String receiverIBAN = command.getReceiver();
        String receiverEmail = command.getReceiver();
        double amountToSend = command.getAmount();
        String emailSender = command.getEmail();
        String descriptionSendMoney = command.getDescription();

        User userSender = null;
        User userReceiver = null;

        // Find the user who is sending the money
        for (User u : users) {
            if (u.getEmail().equals(emailSender)) {
                userSender = u;
                break;
            }
        }

        boolean found = false;
        for (Account accountCheck : userSender.getAccounts()) {
            if (accountCheck.getIBAN().equals(senderIBAN)) {
                found = true;
            }
        }

        if (!found) {
            return;
        }

        if (userSender == null) {
            return;
        }

        // Verify if senderIBAN and email belong to the same user
        Account accountFoundSender = null;
        boolean found2 = false;
        for (Account accountFindSender : userSender.getAccounts()) {
            if (accountFindSender.getIBAN().equals(senderIBAN) ||
                    (accountFindSender.getAlias() != null && accountFindSender.getAlias().equals(senderIBAN))) {
                found2 = true;
                accountFoundSender = accountFindSender;
                break;
            }
        }

        if (!found2 || accountFoundSender == null) {
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("error", "Account not found");
            ObjectNode sendMoneyResponse2 = objectMapper.createObjectNode();
            sendMoneyResponse2.put("command", command.getCommand());
            sendMoneyResponse2.put("timestamp", command.getTimestamp());
            sendMoneyResponse2.set("output", errorResponse);
            out.add(sendMoneyResponse2);
            return;
        }

        // Find the user who is receiving the money
        Account accountFoundReceiver = null;
        for (User u2 : users) {
            for (Account accountFind : u2.getAccounts()) {
                if (accountFind.getIBAN().equals(receiverIBAN) ||
                        (accountFind.getAlias() != null && accountFind.getAlias().equals(receiverIBAN))) {
                    userReceiver = u2;
                    accountFoundReceiver = accountFind;
                    break;
                }
            }
            if (accountFoundReceiver != null) break;
        }

        if (userReceiver == null || accountFoundReceiver == null) {
            return;
        }

        // Currency conversion if necessary
        double convertedAmount2 = amountToSend;
        if (!accountFoundSender.getCurrency().equals(accountFoundReceiver.getCurrency())) {
            convertedAmount2 = convertCurrency(accountFoundSender.getCurrency(),
                    accountFoundReceiver.getCurrency(), amountToSend, exchangeRates);

            if (convertedAmount2 == -1) {
                ObjectNode errorNode = objectMapper.createObjectNode();
                errorNode.put("error", "Exchange rate path not found");
                ObjectNode sendMoneyResponse4 = objectMapper.createObjectNode();
                sendMoneyResponse4.put("command", command.getCommand());
                sendMoneyResponse4.put("timestamp", command.getTimestamp());
                sendMoneyResponse4.set("output", errorNode);
                out.add(sendMoneyResponse4);
                return;
            }
        }

        // Check if there are sufficient funds
        if (accountFoundSender.getBalance() < amountToSend) {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("timestamp", command.getTimestamp());
            errorNode.put("description", "Insufficient funds");
            userSender.getTransactions().add(errorNode);
            return;
        }

        // Perform the transfer
        accountFoundSender.setBalance(accountFoundSender.getBalance() - amountToSend);
        accountFoundReceiver.setBalance(accountFoundReceiver.getBalance() + convertedAmount2);

        // Create transaction objects
        ObjectNode sendMoneyResponse = objectMapper.createObjectNode();
        sendMoneyResponse.put("timestamp", command.getTimestamp());
        sendMoneyResponse.put("description", descriptionSendMoney);
        sendMoneyResponse.put("senderIBAN", senderIBAN);
        sendMoneyResponse.put("receiverIBAN", receiverIBAN);
        sendMoneyResponse.put("amount", amountToSend + " " + accountFoundSender.getCurrency());
        sendMoneyResponse.put("transferType", "sent");
        userSender.getTransactions().add(sendMoneyResponse);

        ObjectNode sendMoneyResponseReceiver = objectMapper.createObjectNode();
        sendMoneyResponseReceiver.put("timestamp", command.getTimestamp());
        sendMoneyResponseReceiver.put("description", descriptionSendMoney);
        sendMoneyResponseReceiver.put("senderIBAN", senderIBAN);
        sendMoneyResponseReceiver.put("receiverIBAN", receiverIBAN);
        sendMoneyResponseReceiver.put("amount", convertedAmount2 + " " + accountFoundReceiver.getCurrency());
        sendMoneyResponseReceiver.put("transferType", "received");
        userReceiver.getTransactions().add(sendMoneyResponseReceiver);

        // Add response to output
        ObjectNode response = objectMapper.createObjectNode();
        response.put("command", command.getCommand());
        response.put("timestamp", command.getTimestamp());
        response.set("output", sendMoneyResponse);
//        out.add(response);
    }

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
}