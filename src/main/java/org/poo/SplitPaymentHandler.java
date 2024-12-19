package org.poo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.Pair;
import org.poo.account.Account;
import org.poo.fileio.CommandInput;

import java.util.*;

public class SplitPaymentHandler implements CommandHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        double amountSplit = command.getAmount();
        String currencySplit = command.getCurrency();
        ArrayList<String> accountsForSplit = (ArrayList<String>) command.getAccounts();
        double amountPerAccount = amountSplit / accountsForSplit.size();

        List<Pair<User, Account>> participantAccounts = new ArrayList<>();
        List<Double> convertedShares = new ArrayList<>();

        boolean splitError = false;
        Account accountError = null;

        // 1. Identify all account objects from IBAN/alias
        for (String accId : accountsForSplit) {
            boolean foundAccc = false;
            for (User u : users) {
                for (Account acc : u.getAccounts()) {
                    if (acc.getIBAN().equals(accId) || (acc.getAlias() != null && acc.getAlias().equals(accId))) {
                        participantAccounts.add(new Pair<>(u, acc));
                        foundAccc = true;
                        break;
                    }
                }
                if (foundAccc) break;
            }
        }

        // 2. Convert per-account cost to each account's currency, check funds
        for (Pair<User, Account> pair : participantAccounts) {
            Account accountToSplit = pair.getValue();

            double localShare = amountPerAccount;
            if (!accountToSplit.getCurrency().equals(currencySplit)) {
                double c = convertCurrency(currencySplit, accountToSplit.getCurrency(), amountPerAccount, exchangeRates);
                if (c == -1) {
                    splitError = true;
                    break;
                }
                localShare = c;
            }

            if (accountToSplit.getBalance() < localShare) {
                splitError = true;
                accountError = accountToSplit;
            }
            convertedShares.add(localShare);
        }

        // 3. If ANY error => log an error transaction for each participating account
        if (splitError || participantAccounts.size() != accountsForSplit.size()) {
            for (Pair<User, Account> pair : participantAccounts) {
                User userPart = pair.getKey();

                ObjectNode errTx = objectMapper.createObjectNode();
                errTx.put("timestamp", command.getTimestamp());
                errTx.put("description", "Split payment of " + amountSplit + "0 " + currencySplit);
                errTx.put("currency", currencySplit);
                errTx.put("amount", amountPerAccount);
                errTx.set("involvedAccounts", objectMapper.valueToTree(accountsForSplit));

                if (accountError != null) {
                    errTx.put("error", "Account " + accountError.getIBAN() + " has insufficient funds for a split payment.");
                } else {
                    errTx.put("error", "Insufficient funds or conversion error");
                }

                userPart.getTransactions().add(errTx);
            }
        } else {
            // 4. Otherwise, all good => each account pays its share
            for (int i = 0; i < participantAccounts.size(); i++) {
                User userPart = participantAccounts.get(i).getKey();
                Account accPart = participantAccounts.get(i).getValue();
                double share = convertedShares.get(i);

                accPart.setBalance(accPart.getBalance() - share);

                ObjectNode splitTx = objectMapper.createObjectNode();
                splitTx.put("timestamp", command.getTimestamp());
                splitTx.put("description", "Split payment of " + amountSplit + "0 " + currencySplit);
                splitTx.put("currency", currencySplit);
                splitTx.put("amount", amountPerAccount);
                splitTx.set("involvedAccounts", objectMapper.valueToTree(accountsForSplit));

                userPart.getTransactions().add(splitTx);
            }
        }
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
        return -1;
    }
}