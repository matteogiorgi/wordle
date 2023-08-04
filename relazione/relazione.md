<style>
h1 { margin-top: -1.5rem; }
h4 { margin-top: -1rem; }
code { font-family: 'Ubuntu Mono'; font-size: 85%; }
pre { font-family: 'Ubuntu Mono'; font-size: 100%; }
</style>

<center>
# Wordle 3.0
#### *Matteo Giorgi* `517183`
</center>

<br>

<div style="font-style: italic; color: black; border: 1px solid black; border-radius: 4px; background: #e9edf3; padding: 1rem 3rem 2rem 3rem">
Il progetto consiste nella implementazione di [Wordle](https://www.nytimes.com/games/wordle/index.html), un gioco di parole web-based sviluppato da Josh Wardle nel 2021, acquistato poi dal New York Times a fine 2022.

Ogni 24h il gioco estrae casualmente dal proprio dizionario una Secret-Word di 5 lettere che il giocatore deve indovinare proponendo una Guessed-Word per ciascuno dei 6 tentativi massimi consentiti. Ad ogni tentativo, Wordle risponderà con indizi utili riguardo le lettere che compongono la Guessed-Word così da aiutare il giocatore a indovinare la Secret-Word giornaliera.

Questa implementazione consiste in una versione semplificata del gioco, che conserva la logica di base dell'originale ma apporta modifiche su alcune funzionalità come la condivisione social dei risultati (realizzata qui con un gruppo multicast), e l'assenza di una interfaccia grafica (sostituita da una semplice Command-Line UI).

La presente relazione è affiancata da una documentazione [JavaDoc](file:///home/geoteo/Documents/reti/wordle/doc/allclasses-index.html) scritta come da specifica Oracle e dettagliata nelle linee guida delle [Tecnical-Resources](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html).
</div>

*Wordle 3.0* usa una classica struttura client-server. Il server legge il proprio file di configurazione e si occupa di caricare in memoria l'elenco degli utenti, l'elenco delle parole (dizionario) e rimanere in attesa di connessioni su una welcome-socket (`ServerSocket`) appositamente allocata su una porta predefinita nel file di configurazione. Agganciato un client, il server lancierà dunque un nuovo runnable (`Game`) con cui verranno soddisfatte le richieste, per poi rimettersi in attesa di una nuova connessione. Il client invece, dopo la lettura del proprio file di configurazione, ha l'unico scopo di connettersi al server con una socket (`Socket`) e inviare comandi sottoforma di *lines* (stringhe terminanti con il carattere di line-break).

Prima di entrare nelle specifiche dell'implementazione ecco qua sotto l'ASF che illustra i possibili stati di un client nelle varie fasi di gioco (si consideri ovviamente che client e server abbiano superato la fase iniziale di setup).

<br>
<center>
<img src="wordle.drawio.svg" width="100%" />
</center>


## Struttura server

### [`ServerMain`](file:///home/geoteo/Documents/reti/wordle/doc/ServerMain.html)

### [`ServerSetup`](file:///home/geoteo/Documents/reti/wordle/doc/ServerSetup.html)
### [`MulticastSender`](file:///home/geoteo/Documents/reti/wordle/doc/MulticastSender.html)
### [`Game`](file:///home/geoteo/Documents/reti/wordle/doc/Game.html)


## Struttura client

### [`ClientMain`](file:///home/geoteo/Documents/reti/wordle/doc/ClientMain.html)
### [`ClientSetup`](file:///home/geoteo/Documents/reti/wordle/doc/ClientSetup.html)
### [`MulticastReceiver`](file:///home/geoteo/Documents/reti/wordle/doc/MulticastReceiver.html)
