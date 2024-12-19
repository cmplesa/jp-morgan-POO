package org.poo.StrategyHandler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;

import org.poo.Components.ExchangeRate;
import org.poo.Components.User;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;

/**
 * Handles setting alias for accounts based on commands.
 */
public final class SetAliasHandler implements CommandHandler {
    @Override
    public void execute(final CommandInput command, final ArrayList<User> users,
                        final ArrayList<ExchangeRate> exchangeRates, final ArrayNode out) {
        ObjectMapper objectMapper = new ObjectMapper();

        String accountIBAN3 = command.getAccount();
        String alias = command.getAlias();
        String email4 = command.getEmail();

        User user4 = null;
        for (User u : users) {
            if (u.getEmail().equals(email4)) {
                user4 = u;
                break;
            }
        }

        if (user4 == null) {
            return;
        }

        // Check if alias is already in use
        boolean aliasInUse = false;
        for (User u : users) {
            for (Account accountAlias : u.getAccounts()) {
                if (alias.equals(accountAlias.getAlias())) {
                    aliasInUse = true;
                    break;
                }
            }
            if (aliasInUse) {
                break;
            }
        }

        if (aliasInUse) {
            return;
        }

        boolean foundAcc = false;
        for (Account account5 : user4.getAccounts()) {
            if (account5.getIBAN().equals(accountIBAN3)) {
                account5.setAlias(alias);
                foundAcc = true;
                break;
            }
        }

        if (!foundAcc) {
            ObjectNode errorResp = objectMapper.createObjectNode();
            errorResp.put("error", "Account not found");
            ObjectNode aliasResp = objectMapper.createObjectNode();
            aliasResp.put("command", command.getCommand());
            aliasResp.put("timestamp", command.getTimestamp());
            aliasResp.set("output", errorResp);
            out.add(aliasResp);
        }
    }
}
