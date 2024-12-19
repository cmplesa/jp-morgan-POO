package org.poo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;
import org.poo.CommandHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Bank {
    private ArrayList<User> users;
    private ArrayList<ExchangeRate> exchangeRates;
    private ArrayList<CommandInput> commands;
    private Map<String, CommandHandler> commandHandlers;

    public Bank() {
        this.users = new ArrayList<>();
        this.exchangeRates = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.commandHandlers = new HashMap<>();

        initializeHandlers(users, exchangeRates, commands);
    }

    public Bank(ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayList<CommandInput> commands) {
        this.users = users;
        this.exchangeRates = exchangeRates;
        this.commands = commands;
        this.commandHandlers = new HashMap<>();

        initializeHandlers(users, exchangeRates, commands);
    }

    private void initializeHandlers(ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayList<CommandInput> commands) {
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

    public void banking(ArrayList<User> users, ArrayList<ExchangeRate> exchangeRates, ArrayList<CommandInput> commands, ArrayNode out) {
        for (CommandInput command : commands) {
            CommandHandler handler = commandHandlers.get(command.getCommand());

            if (handler != null) {
                handler.execute(command, users, exchangeRates, out);
            } else {
                System.out.println("Invalid command: " + command.getCommand());
            }
        }
    }
}
