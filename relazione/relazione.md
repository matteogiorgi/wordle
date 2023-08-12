<style>
h1 { margin-top: -1.5rem; }
h4 { margin-top: -1rem; }
code,pre { font-family: 'Ubuntu Mono'; font-size: 100%; }
</style>

<center>
# *Wordle 3.0*
#### *Matteo Giorgi* `517183`
</center>

<br>

<div style="font-style: italic; color: black; border: 1px solid black; border-radius: 4px; background: #e9edf3; padding: 1rem 3rem 2rem 3rem">
Il progetto consiste nella implementazione di [Wordle](https://www.nytimes.com/games/wordle/index.html), un gioco di parole web-based sviluppato da Josh Wardle nel 2021, acquistato poi dal New York Times a fine 2022.

Ogni 24h il gioco estrae casualmente dal proprio dizionario una Secret-Word di 5 lettere che il giocatore deve indovinare proponendo una Guessed-Word per ciascuno dei 6 tentativi massimi consentiti. Ad ogni tentativo, Wordle risponderà con indizi utili riguardo le lettere che compongono la Guessed-Word così da aiutare il giocatore a indovinare la Secret-Word giornaliera.

Questa implementazione consiste in una versione semplificata del gioco, che conserva la logica di base dell'originale ma apporta modifiche su alcune funzionalità come la condivisione social dei risultati (realizzata qui con un gruppo multicast), e l'assenza di una interfaccia grafica (sostituita da una semplice Command-Line UI).

La presente relazione è affiancata da documentazione [JavaDoc](file:///home/geoteo/Documents/reti/wordle/doc/allclasses-index.html) secondo quanto specificato nelle [Tecnical-Resources](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html) Oracle. **Questo testo esiste anche in [html]() wiki-page**.
</div>

*Wordle 3.0* usa una classica struttura client-server. Il server legge il proprio file di configurazione e si occupa di caricare in memoria l'elenco degli utenti, l'elenco delle parole (dizionario) e rimanere in attesa di connessioni su una welcome-socket (`ServerSocket`) appositamente allocata su una porta predefinita nel file di configurazione. Agganciato un client, il server lancierà dunque un nuovo Runnable (`Game`) con cui verranno soddisfatte le richieste, per poi rimettersi in attesa di una nuova connessione. Il client invece, dopo la lettura del proprio file di configurazione, ha l'unico scopo di connettersi al server con una socket (`Socket`) e inviare comandi sottoforma di *lines* (stringhe terminanti con il carattere di line-break).




## Struttura del Progetto

Prima di entrare nelle specifiche dell'implementazione ecco qua sotto l'ASF che illustra i possibili stati di un client nelle varie fasi di gioco (si consideri ovviamente che client e server abbiano superato la fase iniziale di setup).

<br>
<center>
<img src="asf.svg" width="100%" />
</center>




### Modelli principali

- `Word` e `WordList` gestiscono le parole del gioco: la classe `Word` rappresenta una singola parola, mentre `WordList` il dizionario usato da *Wordle* per estrarre ciclicamente la *Secret-Word* e controllare la validità delle *Guessed-Words* inserite dall'utente in gioco.
- `User` e `UserList` in modo analogo alle classi precedenti, rappresentano rispettivamente il singolo utente e l'insieme degli utenti registrati sul server del gioco.




### Funzionalità del server

- `ServerSetup` e `ServerMain` sono le classi che descrivono le proprietà e identificano il punto di ingresso principale del server.
- `Game` è la classe responsabile della sessione di gioco per ciascun client, secondo i quattro stati specificati nell'ASF.
- `MulticastSender` gestisce la condivisione dei risultati attraverso un gruppo multicast.




### Funzionalità del client

- `ClientSetup` e `ClientMain` analogamente alle loro controparti, rappresentano le proprietà e il punto di ingresso del client.
- `MulticastReceiver` gestisce la condivisione dei risultati attraverso un gruppo multicast.




## Classe [`Word`](file:///home/geoteo/Documents/reti/wordle/doc/Word.html)

Classe che rappresenta la parola che i giocatori devono indovinare, è implementata usando due variabili private.

- *`currentWord`*: `String` che identifica la *Secret-Word* corrente.
- *`userSet`*: `Set<String>` che contiene i nomi degli utenti che hanno già giocato la *Secret-Word* corrente. Un utente che avesse già giocato la *Secret-Word* corrente e chiedesse di iniziare una nuova partita, rimarrebbe in `[LOGIN SESSION]` (si veda AST).

Il [costruttore](https://github.com/matteogiorgi/wordle/blob/73926891f8bb2664cfd047886aeed3906a608a93/src/Word.java#L26-L39) della classe prende come parametro una `String` che rappresenta la parola da indovinare; non è consentito creare una parola *null*, causerebbe il lancio di una `IllegalArgumentException`.

Oltre ai metodi [*`getWord`*](https://github.com/matteogiorgi/wordle/blob/2dc0d25fd78f3db4454bc6c6ab7585bbbfd28ded/src/Word.java#L42-L49), [*`containsUser`*](https://github.com/matteogiorgi/wordle/blob/2dc0d25fd78f3db4454bc6c6ab7585bbbfd28ded/src/Word.java#L52-L62) e [*`addUser`*](https://github.com/matteogiorgi/wordle/blob/2dc0d25fd78f3db4454bc6c6ab7585bbbfd28ded/src/Word.java#L65-L75), la classe contiene anche [*`getMask`*](https://github.com/matteogiorgi/wordle/blob/2dc0d25fd78f3db4454bc6c6ab7585bbbfd28ded/src/Word.java#L78-L107), necessario a `Game` per fornire informazioni al client sulla correttezza della *Guessed-Word* inserita, sottoforma di una maschera di caratteri speciali (`X`, `+`, `?`) come da specifica.




## Classe [`WordList`](file:///home/geoteo/Documents/reti/wordle/doc/WordList.html)

Classe che rappresenta il vocabolario utilizzato da *Wordle* per estrarre la *Secret-Word* e controllare la validità delle *Guessed-Word* inserite dagli utenti durante una partita. Di seguito la struttura base.

- *`wordVocabulary`*: `List<String>` che contiene le parole del vocabolario.
- *`currentWord`*: istanza della classe `Word` che rappresenta la parola attualmente selezionata come *Secret-Word*.
- *`wordExtractor`*: `ScheduledExecutorService` che estrae una parola casuale dal vocabolario a intervalli regolari di tempo.

La classe contiene anche il `Runnable` [*`extractWord`*](https://github.com/matteogiorgi/wordle/blob/2dc0d25fd78f3db4454bc6c6ab7585bbbfd28ded/src/WordList.java#L38-L45) che rappresenta il task di estrazione casuale di una nuova *Secret-Word* dal vocabolario, eseguito da `wordExtractor` ogni `wordTimer` secondi (tempo specificato nel file di configurazione del server).




## Classe [`User`](file:///home/geoteo/Documents/reti/wordle/doc/User.html)

Classe che rappresenta l'utente come una mappa chiave-valore che ne specifica le proprietà.

- *`user`*: nome dell'utente.
- *`password`*: password per il login dell'utente.
- *`giocate`*: numero di partite giocate dall'utente.
- *`vinte`*: numero di partite vinte dall'utente.
- *`streaklast`*: lunghezza della serie di vittorie più recente dell'utente.
- *`streakmax`*: lunghezza massima della serie di vittorie ottenuta dall'utente.
- *`guessd`*: distribuzione che indica i tentativi impiegati dall'utente per arrivare alla soluzione nelle partite giocate.

Per evitare di esporre i metodi di modifica della mappa, invece di estendere una delle implementazioni di `Map`, è stata usata la variabile privata *`user`* di tipo `Map<String, Object>` per identificare l'utente con le sue proprietà.

Questa scelta permette di avere una struttura sicura e flessibile, facilmente modificabile al termine di ogni partita con l'apposito metodo [*`update`*](https://github.com/matteogiorgi/wordle/blob/73926891f8bb2664cfd047886aeed3906a608a93/src/User.java#L159-L174), con il quale `Game` può aggiornare i dati dell'utente.

Oltre ai [metodi getter](https://github.com/matteogiorgi/wordle/blob/73926891f8bb2664cfd047886aeed3906a608a93/src/User.java#L88-L156) con i quali recuperare i sette valori della mappa, la classe contiene il metodo [*`copy`*](https://github.com/matteogiorgi/wordle/blob/73926891f8bb2664cfd047886aeed3906a608a93/src/User.java#L63-L85) che permette di creare una copia profonda dell'utente.

La classe ha un unico [costruttore](https://github.com/matteogiorgi/wordle/blob/73926891f8bb2664cfd047886aeed3906a608a93/src/User.java#L33-L60) che permette di creare un nuovo utente partendo da una `Map<String, Object>`. Non è consentito creare un oggetto della classe `User` usando una mappa *null*, incompleta o contenente associazioni chiave-valore di tipo sbagliato: questi casi causerebbero il lancio di una `NullPointerException` o di una `IllegalArgumentException`.




## Classe [`UserList`](file:///home/geoteo/Documents/reti/wordle/doc/UserList.html)

La classe implementa l'elenco degli utenti registrati e contiene due strutture dati principali.

- *`userRegistrati`*: `Map<String, User>` contenente gli utenti registrati al gioco. Il nome utente rappresentata la chiave, l'oggetto `User` il valore associato (l'associazione è biunivoca: il nome identifica univocamemte l'utente registrato).
- *`userLoggati`*: `Set<String>` contenente i nomi degli utenti attualmente loggati al gioco.

La classe utilizza la libreria [*Gson*](https://github.com/google/gson) per gestire la serializzazione e deserializzazione degli utenti in formato JSON, permettendo di [caricare](https://github.com/matteogiorgi/wordle/blob/73926891f8bb2664cfd047886aeed3906a608a93/src/UserList.java#L45-L88)/[salvare](https://github.com/matteogiorgi/wordle/blob/73926891f8bb2664cfd047886aeed3906a608a93/src/UserList.java#L91-L118) l'elenco degli utenti da/verso un file `.json` il cui path è specificato nel file di configurazione del server.




## Classe [`ServerSetup`](file:///home/geoteo/Documents/reti/wordle/doc/ServerSetup.html)

Classe che fornisce al server tutte le informazioni necessarie per la sua configurazione iniziale. Estende la classe `Properties` e viene usata da `ServerMain` per leggere il file di configurazione all'avvio, memorizzando le varie proprietà.

1. *`PORT`*: numero di porta (`int`) su cui il server si mette in ascolto.
2. *`PATH_VOCABULARY`*: path (`String`) del file che contiene il vocabolario (dizionario) del gioco.
3. *`PATH_JSON`*: path (`String`) del file JSON che contiene i dati degli utenti.
4. *`WORD_TIMER`*: tempo in secondi (`int`) che intercorre tra le pubblicazioni della parola segreta.
5. *`MULTICAST_GROUP_ADDRESS`*: indirizzo (`String`) del gruppo multicast, utilizzato per la condivisione dei risultati.




## Classe [`ServerMain`](file:///home/geoteo/Documents/reti/wordle/doc/ServerMain.html)

Classe che contiene il punto di ingresso (`main`) ed è responsabile per l'inizializzazione, l'attesa di connessioni e la comunicazione con i client.

1. *Creazione strutture dati*: all'avvio, il server crea l'elenco degli utenti (`UserList`) e il dizionario (`WordList`).
2. *Attesa connessioni*: il server rimane in attesa di connessioni dai client; ad ogni connessione ricevuta, lancierà `Game` che si preoccuperà di gestire l'interazione con il client.
3. *Gestione socket e thread-pools*: la classe si occupa anche di gestire la chiusura delle socket e dei thread pools usando il `Runnable` [*`shutdownHook`*](https://github.com/matteogiorgi/wordle/blob/73926891f8bb2664cfd047886aeed3906a608a93/src/ServerMain.java#L43-L94) che esegue tutte le operazioni necessarie per un corretta chiusura del server.

Le variabili principali usate dalla classe sono le seguenti.

- *`PATH_CONF`*: `String` che rappresenta il path del file di configurazione del server.
- *`serverProperties`*: istanza di `ServerSetup` che memorizza le proprietà del server.
- *`listaUtenti`*: istanza di `UserList` che memorizza gli utenti registrati al gioco.
- *`listaParole`*: istanza di `WordList` che memorizza le parole del gioco.
- *`welcomeSocket`*: `ServerSocket` utilizzata per accettare connessioni dai client.




## Classe [`Game`](file:///home/geoteo/Documents/reti/wordle/doc/Game.html)

Il file `Game.java` definisce la classe `Game`, che rappresenta un thread per gestire l'interazione tra il server e un client. Ecco una panoramica iniziale del suo contenuto:

Questa classe implementa un thread che propone una interfaccia testuale al client, suddivisa in diverse fasi:

1. **[MAIN SESSION]**: È la fase iniziale in cui l'utente ha diverse opzioni:
   - **exit**: Uscire dal programma.
   - **register**: Registrarsi al gioco.
   - **remove**: Cancellarsi dal gioco.
   - **login**: Autenticarsi al gioco.

2. **[LOGIN SESSION]**: Una volta autenticato, l'utente ha altre opzioni:
   - **playwordle**: Iniziare una nuova partita.
   - **sendmestat**: Richiedere le proprie statistiche di gioco.
   - **sharemestat**: Condividere le proprie statistiche.

La classe `Game` quindi gestisce l'interazione con l'utente attraverso una serie di comandi testuali, guidandolo attraverso le diverse fasi del gioco e fornendo una serie di funzionalità basate sullo stato di sessione dell'utente.

Dalla panoramica iniziale, sembra che questa classe abbia un ruolo centrale nell'interazione con l'utente e nella logica del gioco. 

Proseguiamo con un'analisi più dettagliata di questa classe o preferisci passare alla successiva?




## Classe [`MulticastSender`](file:///home/geoteo/Documents/reti/wordle/doc/MulticastSender.html)

Il file `MulticastSender.java` definisce la classe `MulticastSender`, che sembra essere responsabile dell'invio di notifiche su un gruppo multicast. Ecco una breve descrizione del suo contenuto:

La classe `MulticastSender` implementa l'interfaccia `Runnable` e gestisce l'invio di notifiche tramite un gruppo multicast:

- **multicastGroupPort**: Un numero intero che rappresenta la porta del gruppo multicast.
- **multicastGroupAddress**: Una stringa che rappresenta l'indirizzo del gruppo multicast.
- **queue**: Una coda utilizzata per conservare temporaneamente le notifiche inviate dal server. Queste notifiche sono in attesa di essere lette e inviate sul canale multicast.

Dalla descrizione, sembra che `MulticastSender` funzioni come un thread che rimane in attesa (probabilmente utilizzando `wait()`) fino a quando non viene notificato dell'aggiunta di una nuova notifica. Una volta ricevuta questa notifica, la legge dalla coda e la invia sul gruppo multicast.

Questa classe svolge un ruolo chiave nella funzionalità di condivisione dei risultati del gioco, permettendo ai client di ricevere notifiche in tempo reale tramite il gruppo multicast.

Proseguiamo con un'analisi più dettagliata di questa classe o preferisci passare alla successiva?




## Classe [`ClientSetup`](file:///home/geoteo/Documents/reti/wordle/doc/ClientSetup.html)

Il file `ClientSetup.java` definisce la classe `ClientSetup`, che rappresenta le proprietà del client. Ecco una breve descrizione del suo contenuto:

Questa classe estende `Properties` e ha il compito di leggere il file di configurazione del client all'avvio, memorizzando le varie proprietà nelle appropriate strutture dati. Le proprietà disponibili sono:

1. **HOSTNAME**: Il nome del server al quale il client si connetterà. È una stringa.
2. **PORT**: Il numero di porta del server. È un valore intero.
3. **MULTICAST_GROUP_ADDRESS**: L'indirizzo del gruppo multicast, utilizzato per la condivisione dei risultati. È una stringa.
4. **MULTICAST_GROUP_PORT**: Il numero di porta del gruppo multicast. È un valore intero.

Similmente a `ServerSetup`, la classe `ClientSetup` fornisce al client tutte le informazioni necessarie per la sua configurazione iniziale, permettendo di personalizzare vari aspetti della connessione e dell'interazione con il server.

Proseguiamo con un'analisi più dettagliata di questa classe o preferisci passare alla successiva?




## Classe [`ClientMain`](file:///home/geoteo/Documents/reti/wordle/doc/ClientMain.html)

Il file `ClientMain.java` definisce la classe `ClientMain`, che rappresenta il punto di ingresso principale per il client. Ecco una breve descrizione del suo contenuto:

Questa è la classe principale che rappresenta il client:

- **PATH_CONF**: Contiene il percorso del file di configurazione del client.
- **clientProperties**: Un'istanza della classe `ClientSetup` che memorizza le proprietà del client.
- **multicastListener**: Un thread che rimane in ascolto delle notifiche sul gruppo multicast.
- **multicastReceiver**: Un'istanza della classe `MulticastReceiver` che memorizza le notifiche ricevute.
- **logStatus**: Indica lo stato del log (false = non loggato, true = loggato).

La classe `ClientMain` inizializza le configurazioni necessarie per il client, si connette al server e gestisce l'interazione con l'utente. Utilizza le configurazioni fornite dalla classe `ClientSetup` e le notifiche multicast attraverso la classe `MulticastReceiver`.

Questa classe è essenziale per avviare e gestire il comportamento del client nel gioco Wordle.

Proseguiamo con un'analisi più dettagliata di questa classe o preferisci passare alla successiva?




## Classe [`MulticastReceiver`](file:///home/geoteo/Documents/reti/wordle/doc/MulticastReceiver.html)

Il file `MulticastReceiver.java` definisce la classe `MulticastReceiver`, che gestisce la ricezione di notifiche da un gruppo multicast. Ecco una breve descrizione del suo contenuto:

La classe `MulticastReceiver` estende `ConcurrentLinkedQueue<String>` e implementa l'interfaccia `Runnable`. Questo suggerisce che la classe funge da coda thread-safe di stringhe (probabilmente le notifiche ricevute) e può essere eseguita come un thread.

Ecco le principali variabili e funzionalità della classe:

- **multicastGroupPort**: Il numero di porta del gruppo multicast.
- **multicastGroupAddress**: L'indirizzo del gruppo multicast.
- **userName**: Il nome dell'utente che sta ascoltando le notifiche sul gruppo multicast.

La classe `MulticastReceiver` rimane in ascolto delle notifiche sul gruppo multicast e aggiunge le notifiche ricevute alla coda delle notifiche. Questo permette ai client di ricevere notifiche in tempo reale e di processarle in modo asincrono.

Questa classe, insieme a `MulticastSender`, consente una comunicazione multicast tra il server e i client, fornendo una funzionalità di condivisione in tempo reale tra i giocatori.

Proseguiamo con un'analisi più dettagliata di questa classe o preferisci passare alla successiva?
