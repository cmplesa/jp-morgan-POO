package org.poo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;

import org.poo.fileio.CommandInput;

public class PrintTransactionsHandler implements CommandHandler {
    @Override
    public void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        String emailPrint = command.getEmail();
        ObjectNode printTransactions = objectMapper.createObjectNode();
        printTransactions.put("command", command.getCommand());
        User userPrint = null;
        for (User u : users) {
            if (u.getEmail().equals(emailPrint)) {
                userPrint = u;
                break;
            }
        }

        if (userPrint == null) {
            ObjectNode errResp = objectMapper.createObjectNode();
            errResp.put("error", "User not found");
            ObjectNode resp = objectMapper.createObjectNode();
            resp.put("command", command.getCommand());
            resp.put("timestamp", command.getTimestamp());
            resp.set("output", errResp);
            out.add(resp);
            return;
        }

        ArrayNode transactionsArray = objectMapper.createArrayNode();
        for (ObjectNode transaction : userPrint.getTransactions()) {
            if (transaction.get("description").asText().equals("Card payment")) {
                transaction.remove("IBAN");
            }
            transactionsArray.add(transaction);
        }

        printTransactions.set("output", transactionsArray);
        printTransactions.put("timestamp", command.getTimestamp());
        out.add(printTransactions);
    }
}