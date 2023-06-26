# WORDLE: un gioco di parole 3.0

Il progetto consiste nella implementazione di WORDLE, un gioco di parole web-based, divenuto virale alla fine del 2021.  Il gioco consiste nel trovare una parola inglese formata da 5 lettere, impiegando un numero massimo di 6 tentativi. WORDLE dispone di un vocabolario di parole di 5 lettere, da cui estrae casualmente una parola SW (Secret Word), che gli utenti devono indovinare. Ogni giorno viene selezionata una nuova SW, che rimane invariata fino al giorno successivo e che viene proposta a tutti gli utenti che si collegano al sistema durante quel giorno. Quindi esiste una sola parola per ogni giorno e tutti gli utenti devono indovinarla, questo attribuisce al gioco un aspetto sociale. L’utente propone una parola GW (Guessed Word) e il sistema inizialmente verifica se la parola è presente nel vocabolario. In caso negativo avverte l’utente che deve immettere un’altra parola. In caso la parola sia presente, il sistema fornisce all’utente alcuni indizi, utili per indovinare la parola.


## Folder Structure

The workspace contains two folders by default, where:

- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.


## Dependency Management

The `JAVA PROJECTS` view allows you to manage your dependencies. More details can be found [here](https://github.com/microsoft/vscode-java-dependency#manage-dependencies).
