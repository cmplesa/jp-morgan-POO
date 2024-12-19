package org.poo.account;

import lombok.Data;
import org.poo.Card;
import org.poo.utils.Utils;

import java.util.ArrayList;

/**
 * Represents a classic bank account.
 */
@Data
public final class AccountClassic implements Account {
    private String iban;
    private double balance;
    private String currency;
    private ArrayList<Card> cards;
    private String alias;
    private double minimumBalance;

    /**
     * Default constructor for AccountClassic.
     */
    public AccountClassic() {
    }

    /**
     * Constructs a classic account with the specified currency.
     *
     * @param currency the currency of the account
     */
    public AccountClassic(final String currency) {
        this.iban = Utils.generateIBAN();
        this.balance = 0;
        this.currency = currency;
        this.cards = new ArrayList<>();
        this.minimumBalance = 0;
    }

    /**
     * Deposits an amount into the account.
     *
     * @param amount the amount to deposit
     */
    @Override
    public void deposit(final double amount) {
        this.balance += amount;
    }

    /**
     * Withdraws an amount from the account.
     *
     * @param amount the amount to withdraw
     */
    @Override
    public void withdraw(final double amount) {
        this.balance -= amount;
    }

    /**
     * Returns the type of the account.
     *
     * @return the type of the account ("classic")
     */
    @Override
    public String getAccountType() {
        return "classic";
    }

    /**
     * Sets the interest rate for the account.
     * This operation is not applicable for classic accounts.
     *
     * @param interestRate the interest rate to set
     */
    @Override
    public void setInterestRate(final double interestRate) {
        // Not applicable for classic accounts
    }

    /**
     * Gets the interest rate of the account. This operation is not applicable for classic accounts.
     *
     * @return 0 as interest rate is not applicable for classic accounts
     */
    @Override
    public double getInterestRate() {
        return 0;
    }

    /**
     * Gets the IBAN of the account.
     *
     * @return the IBAN of the account
     */
    @Override
    public String getIBAN() {
        return this.iban;
    }
}
