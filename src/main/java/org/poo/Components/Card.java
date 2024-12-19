package org.poo.Components;

import org.poo.utils.Utils;
import lombok.Data;

@Data
public class Card {
    private String cardNumber;
    private String cardType;
    private String status;

    public Card(final String cardType) {
        this.cardNumber = Utils.generateCardNumber();
        this.cardType = cardType;
        this.status = "active";
    }
}
