package org.poo.Components;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.account.Account;

@Data
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private ArrayList<Account> accounts;
    private ArrayList<ObjectNode> transactions;
    private ArrayList<ObjectNode> onlinePayments;

    public User() {
    }

    public User(final String firstName,
                final String lastName, final String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.accounts = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.onlinePayments = new ArrayList<>();
    }
}
