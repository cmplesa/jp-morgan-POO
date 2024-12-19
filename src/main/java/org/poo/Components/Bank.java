package org.poo.Components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.StrategyHandler.*;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a bank that handles various banking operations.
 */
public final class Bank {
    private final ArrayList<User> users;
    private final ArrayList<ExchangeRate> exchangeRates;
    private final ArrayList<CommandInput> commands;
    private final Map<String, CommandHandler> commandHandlers;

    public Bank() {
        this.users = new ArrayList<>();
        this.exchangeRates = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.commandHandlers = new HashMap<>();

        initializeHandlers(users, exchangeRates, commands);
    }

    public Bank(final ArrayList<User> usersList,
                final ArrayList<ExchangeRate> exchangeRatesList,
                final ArrayList<CommandInput> commandsList) {
        this.users = usersList;
        this.exchangeRates = exchangeRatesList;
        this.commands = commandsList;
        this.commandHandlers = new HashMap<>();

        initializeHandlers(usersList, exchangeRatesList, commandsList);
    }

    private void initializeHandlers(final ArrayList<User> usersList,
                                    final ArrayList<ExchangeRate> exchangeRatesList,
                                    final ArrayList<CommandInput> commandsList) {
        // Register all command handlers
        commandHandlers.put("printUsers", new PrintUsersHandler());
        commandHandlers.put("addAccount", new AddAccountHandler());
        commandHandlers.put("createCard", new CreateCardHandler(users, new ObjectMapper()));
        commandHandlers.put("addFunds", new DepositFundsHandler());
        commandHandlers.put("deleteAccount", new DeleteAccountHandler());
        commandHandlers.put("payOnline", new PayOnlineHandler());
        commandHandlers.put("sendMoney", new SendMoneyHandler());
        commandHandlers.put("setAlias", new SetAliasHandler());
        commandHandlers.put("printTransactions", new PrintTransactionsHandler());
        commandHandlers.put("setMinimumBalance", new SetMinimumBalanceHandler());
        commandHandlers.put("checkCardStatus", new CheckCardStatusHandler());
        commandHandlers.put("splitPayment", new SplitPaymentHandler());
        commandHandlers.put("report", new ReportHandler());
        commandHandlers.put("spendingsReport", new SpendingsReportHandler());
        commandHandlers.put("changeInterestRate", new ChangeInterestRateHandler());
        commandHandlers.put("addInterest", new AddInterestHandler());
        commandHandlers.put("createOneTimeCard", new CreateOneTimeCardHandler());
        commandHandlers.put("deleteCard", new DeleteCardHandler());
    }

    /**
     * Executes banking operations based on the provided commands.
     *
     * @param usersList       The list of users.
     * @param rateList        The list of exchange rates.
     * @param commandList     The list of commands.
     * @param out             The JSON array to which the result will be appended.
     */
    public void banking(final ArrayList<User> usersList, final ArrayList<ExchangeRate> rateList,
                        final ArrayList<CommandInput> commandList, final ArrayNode out) {
        for (CommandInput command : commandList) {
            CommandHandler handler = commandHandlers.get(command.getCommand());

            if (handler != null) {
                handler.execute(command, usersList, rateList, out);
            } else {
                System.out.println("Invalid command: " + command.getCommand());
            }
        }
    }
}
