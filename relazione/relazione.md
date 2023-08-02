<style>
h3,h4,h5 { margin-top: -1em; }
</style>

<center>
# Wordle 3.0
### Matteo Giorgi - 517183
</center>

<br>

<div style="font-style: italic; color: gray; border: 1px solid gray; background: #f0f0f0; padding: 1rem 3rem 2rem 3rem">
Il progetto consiste nella implementazione di [WORDLE](https://www.nytimes.com/games/wordle/index.html), un gioco di parole web-based del *New York Times*,divenuto virale alla fine del 2021. Ogni 24h WORDLE estrae casualmente dal proprio dizionario una *Secret-Word* di esattamente 5 lettere che gli utenti devono indovinare in un massimo di 6 tentativi; il giocatore quindi, ad ogni tentativo, propone una *Guessed-Word* e riceve indizi utili per indovinare la parola giornaliera.

L'implementazione consiste in una versione semplificata del gioco, che conserva la logica di base dell'originale ma apporta semplificazioni su alcune funzionalità come la condivisione dei risultati sui social network, realizzata invece tramite un gruppo multicast.
</div>




<!-- ## Struttura del server -->

<!-- Il server è implementato nella classe `ServerMain.java` -->


<!-- ## Struttura del client (ClientMain.java) -->


## Scopo del progetto

Il progetto mira a implementare un gioco di parole interattivo basato su una comunicazione client-server. Gli utenti possono registrarsi, effettuare il login e partecipare al gioco. Il server gestisce le richieste dei client, estrae una parola casuale dal vocabolario e fornisce gli indizi per aiutare gli utenti a individuare la parola segreta. Vengono anche tenute traccia delle statistiche di gioco di ciascun utente, come il numero di partite giocate, la percentuale di partite vinte e la lunghezza delle sequenze continue di vincite.


## Descrizione delle Classi

Il progetto è implementato attraverso due classi principali: `Word` e `WordList`.

La classe `Word` rappresenta una parola del gioco, composta da una stringa che rappresenta la parola segreta e un insieme di utenti che hanno giocato tale parola. La classe offre metodi per controllare la presenza di un utente nella lista dei partecipanti, aggiungere un utente alla lista e ottenere una maschera che mostra le corrispondenze tra la parola segreta e un tentativo fornito dall'utente.

La classe `WordList` rappresenta il vocabolario di parole disponibili per il gioco. Il vocabolario è rappresentato da una lista di stringhe, e il server estrae casualmente una parola dal vocabolario per ogni partita. La classe gestisce anche un `ScheduledExecutorService` che permette di estrarre una parola casuale dal vocabolario ogni tot secondi. Viene fornita anche la possibilità di controllare se una parola è presente nel vocabolario e di ottenere la parola corrente.


## Architettura del Gioco

L'architettura del gioco è basata su una comunicazione client-server. Il client è rappresentato dalla classe `ClientMain`, che gestisce l'interazione con l'utente attraverso una CLI (Command Line Interface) e comunica con il server tramite richieste per eseguire varie azioni, come la registrazione, il login, l'inizio del gioco, l'invio di una Guessed Word e la condivisione dei risultati. Il server è rappresentato dalla classe `ServerMain`, che gestisce le richieste dei client, memorizza gli utenti registrati, estrae una parola casuale dal vocabolario e fornisce gli indizi.

Le fasi principali del gioco sono:

1. Registrazione e login degli utenti.
2. Estrazione di una parola casuale dal vocabolario da parte del server.
3. Invio di una Guessed Word da parte del client.
4. Ricezione degli indizi da parte del client e iterazione del gioco fino alla soluzione.
5. Calcolo e visualizzazione delle statistiche degli utenti.
6. Condivisione dei risultati tramite un gruppo multicast.
