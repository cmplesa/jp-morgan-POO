package org.poo.account;

/**
 * Factory class for creating different types of accounts.
 */
public final class AccountFactory {

    // Private constructor to prevent instantiation
    private AccountFactory() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates an account of the specified type.
     *
     * @param accountType the type of the account
     * @param currency the currency of the account
     * @param interestRate the interest rate of the account
     * @return the created account
     * @throws IllegalArgumentException if the account type is unknown
     */
    public static Account createAccount(final String accountType, final String currency,
                                        final double interestRate) {
        switch (accountType.toLowerCase()) {
            case "classic":
                return new AccountClassic(currency);
            case "savings":
                return new AccountSavings(currency, interestRate);
            default:
                throw new IllegalArgumentException("Unknown account type: " + accountType);
        }
    }
}
