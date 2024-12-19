package org.poo;

import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.ArrayList;
import org.poo.fileio.CommandInput;

public interface CommandHandler {
    /**
     * Executes a specific command based on the implementation.
     *
     * @param command       The command input to process.
     * @param users         List of users in the bank.
     * @param exchangeRates List of exchange rates.
     * @param out           Output result as a JSON array.
     */
    void execute(CommandInput command, ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayNode out);
}
