package org.poo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.Comparator;

public class SpendingsReportHandler implements CommandHandler {

    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        Integer startTimestamp2 = command.getStartTimestamp();
        Integer endTimestamp2 = command.getEndTimestamp();
        String accountIBAN6 = command.getAccount();

        User userSpendings = null;
        Account accountSpendings = null;

        // Find the user and account
        for (User u : users) {
            for (Account account6 : u.getAccounts()) {
                if (account6.getIBAN().equals(accountIBAN6) || (account6.getAlias() != null && account6.getAlias().equals(accountIBAN6))) {
                    userSpendings = u;
                    accountSpendings = account6;
                    break;
                }
            }
            if (userSpendings != null) break;
        }

        // Handle errors if account or user not found
        if (accountSpendings == null || userSpendings == null) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("command", command.getCommand());

            ObjectNode errorNotFound = objectMapper.createObjectNode();
            errorNotFound.put("timestamp", command.getTimestamp());
            errorNotFound.put("description", "Account not found");

            error.set("output", errorNotFound);
            error.put("timestamp", command.getTimestamp());
            out.add(error);
            return;
        }

        if (accountSpendings.getAccountType().equals("savings")) {
            ObjectNode wrong = objectMapper.createObjectNode();
            wrong.put("command", command.getCommand());

            ObjectNode errorType = objectMapper.createObjectNode();
            errorType.put("error", "This kind of report is not supported for a saving account");

            wrong.set("output", errorType);
            wrong.put("timestamp", command.getTimestamp());
            out.add(wrong);
            return;
        }

        // Prepare the spendings report
        ObjectNode spendingsReport = objectMapper.createObjectNode();
        spendingsReport.put("command", command.getCommand());

        ObjectNode output2 = objectMapper.createObjectNode();
        output2.put("IBAN", accountIBAN6);
        output2.put("balance", accountSpendings.getBalance());
        output2.put("currency", accountSpendings.getCurrency());

        // Gather transactions within the interval and filter by description
        ArrayNode transactionsArray3 = objectMapper.createArrayNode();
        for (ObjectNode onlinePayment2 : userSpendings.getOnlinePayments()) {
            Integer timestamp = onlinePayment2.get("timestamp").asInt();
            if (timestamp >= startTimestamp2 && timestamp <= endTimestamp2 &&
                    onlinePayment2.get("IBAN") != null && onlinePayment2.get("IBAN").asText().equals(accountIBAN6)) {

                onlinePayment2.remove("IBAN");
                transactionsArray3.add(onlinePayment2);
            }
        }

        output2.set("transactions", transactionsArray3);

        // Aggregate amounts by merchant
        ArrayNode commerciantsArray = objectMapper.createArrayNode();
        for (JsonNode transactionSpend : transactionsArray3) {
            String commerciant2 = transactionSpend.get("commerciant").asText();
            double amount2 = transactionSpend.get("amount").asDouble();
            boolean foundCommerciant = false;
            for (JsonNode node : commerciantsArray) {
                if (node.get("commerciant").asText().equals(commerciant2)) {
                    ((ObjectNode) node).put("total", node.get("total").asDouble() + amount2);
                    foundCommerciant = true;
                    break;
                }
            }
            if (!foundCommerciant) {
                ObjectNode commerciantNode = objectMapper.createObjectNode();
                commerciantNode.put("commerciant", commerciant2);
                commerciantNode.put("total", amount2);
                commerciantsArray.add(commerciantNode);
            }
        }

        // Sort commerciantsArray by ASCII order
        ArrayList<JsonNode> commerciantList = new ArrayList<>();
        commerciantsArray.forEach(commerciantList::add);

        commerciantList.sort(Comparator.comparing(node -> node.get("commerciant").asText()));

        ArrayNode sortedCommerciantsArray = objectMapper.createArrayNode();
        for (JsonNode node : commerciantList) {
            sortedCommerciantsArray.add(node);
        }

        output2.set("commerciants", sortedCommerciantsArray);

        spendingsReport.put("output", output2);
        spendingsReport.put("timestamp", command.getTimestamp());

        out.add(spendingsReport);
    }
}