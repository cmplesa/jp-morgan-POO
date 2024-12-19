# J. POO Morgan Chase & Co. - Etapa 1

## Student realizator
- **Plesa Marian-Cosmin**


## Arhitectura Proiectului

### Pachete

- **`account`**:
    - Găzduiește entitățile asociate conturilor bancare.
    - **Clase**:
        - `Account`: Clasa abstractă pentru conturi.
        - `AccountClassic`: Implementare pentru conturile clasice.
        - `AccountSavings`: Implementare pentru conturile de economii.
        - `AccountFactory`: Design pattern Factory pentru crearea dinamică a conturilor.

- **`Components`**:
    - Entități principale folosite în operațiuni.
    - **Clase**:
        - `Bank`: Gestionarea generală a băncii.
        - `Card`: Reprezentarea cardurilor.
        - `ExchangeRate`: Gestionarea cursurilor valutare.
        - `Pair`: Pereche generică pentru mapări.
        - `User`: Reprezentarea utilizatorilor.

- **`StrategyHandler`**:
    - Conține implementările pentru design pattern-ul Strategy, gestionând comenzile utilizatorilor.
    - **Clase**:
        - `AddAccountHandler`: Adăugarea unui cont.
        - `AddInterestHandler`: Încasarea dobânzii pentru conturi de economii.
        - `ChangeInterestRateHandler`: Modificarea dobânzii unui cont.
        - `CheckCardStatusHandler`: Verificarea status-ului unui card.
        - `CreateCardHandler`: Crearea unui card permanent.
        - `CreateOneTimeCardHandler`: Crearea unui card de tip „one-time pay”.
        - `DeleteAccountHandler`: Ștergerea unui cont bancar.
        - `DeleteCardHandler`: Ștergerea unui card asociat unui cont.
        - `DepositFundsHandler`: Depunerea de fonduri într-un cont.
        - `PayOnlineHandler`: Gestionarea plăților online.
        - `PrintTransactionsHandler`: Afișarea tranzacțiilor unui utilizator.
        - `PrintUsersHandler`: Afișarea tuturor utilizatorilor și conturilor asociate.
        - `ReportHandler`: Generarea de rapoarte generale.
        - `SendMoneyHandler`: Transfer de bani între conturi.
        - `SetAliasHandler`: Asignarea unui alias pentru un cont.
        - `SetMinimumBalanceHandler`: Setarea unei balanțe minime pentru un cont.
        - `SpendingsReportHandler`: Generarea unui raport de cheltuieli.
        - `SplitPaymentHandler`: Gestionarea plăților distribuite între conturi.


## Design Patterns

### 1. **Factory Pattern**:
- Implementat în `AccountFactory`.
- Permite crearea dinamică a tipurilor de conturi (`classic`, `savings`) pe baza input-ului.

### 2. **Strategy Pattern**:
- Gestionat prin pachetul `StrategyHandler`.
- Fiecare comandă este delegată unei clase specifice care implementează 
- `CommandHandler`, oferind modularitate și ușurință în adăugarea de noi
- funcționalități.

