# *Wordle 3.0* <a href="https://github.com/matteogiorgi/wordle"><svg fill="#a80000" height="2rem" viewBox="-20 -20 84 84" xmlns="http://www.w3.org/2000/svg"><path d="M32 1.7998C15 1.7998 1 15.5998 1 32.7998C1 46.3998 9.9 57.9998 22.3 62.1998C23.9 62.4998 24.4 61.4998 24.4 60.7998C24.4 60.0998 24.4 58.0998 24.3 55.3998C15.7 57.3998 13.9 51.1998 13.9 51.1998C12.5 47.6998 10.4 46.6998 10.4 46.6998C7.6 44.6998 10.5 44.6998 10.5 44.6998C13.6 44.7998 15.3 47.8998 15.3 47.8998C18 52.6998 22.6 51.2998 24.3 50.3998C24.6 48.3998 25.4 46.9998 26.3 46.1998C19.5 45.4998 12.2 42.7998 12.2 30.9998C12.2 27.5998 13.5 24.8998 15.4 22.7998C15.1 22.0998 14 18.8998 15.7 14.5998C15.7 14.5998 18.4 13.7998 24.3 17.7998C26.8 17.0998 29.4 16.6998 32.1 16.6998C34.8 16.6998 37.5 16.9998 39.9 17.7998C45.8 13.8998 48.4 14.5998 48.4 14.5998C50.1 18.7998 49.1 22.0998 48.7 22.7998C50.7 24.8998 51.9 27.6998 51.9 30.9998C51.9 42.7998 44.6 45.4998 37.8 46.1998C38.9 47.1998 39.9 49.1998 39.9 51.9998C39.9 56.1998 39.8 59.4998 39.8 60.4998C39.8 61.2998 40.4 62.1998 41.9 61.8998C54.1 57.7998 63 46.2998 63 32.5998C62.9 15.5998 49 1.7998 32 1.7998Z"/></svg></a>
<h4 style="margin-top: -1rem;">Progetto di fine corso <i>Laboratorio di Reti 2022-2023</i><br><i>Matteo Giorgi</i> <code>517183</code></h4>

<br>

Il progetto consiste nella implementazione di *Wordle*, un gioco di parole web-based, divenuto virale alla fine del 2021. Il gioco consiste nel trovare una parola inglese formata da 5 lettere, impiegando un numero massimo di 6 tentativi. *Wordle* dispone di un vocabolario di parole di 5 lettere, da cui estrae casualmente una *Secret Word* che gli utenti devono indovinare. Ogni giorno viene selezionata una nuova *Secret-Word* che rimane invariata fino al giorno successivo e che viene proposta a tutti gli utenti che si collegano al sistema durante quel giorno. Esiste quindi una sola parola per ogni giorno e tutti gli utenti devono indovinarla: questo attribuisce al gioco un aspetto sociale. L’utente propone una parola *Guessed-Word*, il sistema inizialmente verifica se la parola è presente nel vocabolario e in caso negativo avverte l’utente che deve immettere un’altra parola. In caso la parola sia presente, il sistema fornisce all’utente alcuni indizi, utili per indovinare la parola.

- [Relazione *PDF*](https://www.geoteo.net/wordle/relazione/relazione.pdf)
- [Relazione *HTML*](https://www.geoteo.net/wordle/relazione/notes/relazione.html)
- [Documentazione *JavaDoc*](https://www.geoteo.net/wordle/doc/allclasses-index.html)
- [Sorgenti *Java*](https://github.com/matteogiorgi/wordle/tree/master/src)
- [Specifiche](https://www.geoteo.net/wordle/specifiche-wordle.pdf)




## Come creare gli archivi *Java*

Compilare i sorgenti `.java` del server in `bin-server`:

```bash
javac -cp lib/gson-2.10.jar -d bin-server/ \
    src/ServerMain.java \
    src/ServerSetup.java \
    src/multicastSender.java \
    src/Game.java \
    src/User.java \
    src/UserList.java \
    src/Word.java \
    src/WordList.java
```

Compilare i sorgenti `.java` del client in `bin-client`:

```bash
javac -d bin-client/ \
    src/ClientMain.java \
    src/ClientSetup.java \
    src/multicastReceiver.java \
```

Generare i file `.jar` per server e client:

```bash
echo "Main-Class: ServerMain" | jar cvfm WordleServer.jar - -C bin-server/ .
echo "Main-Class: ClientMain" | jar cvfm WordleClient.jar - -C bin-client/ .
```




## Come eseguire *Wordle 3.0*

Gli archivi `.jar` sono già presenti nella directory principale del repository. Per eseguire il programma lanciare quindi le seguenti istruzioni in due istanze separate di `sh`:

```bash
# SERVER
java -cp WordleServer.jar:lib/gson-2.10.jar ServerMain

# CLIENT
java -cp WordleClient.jar ClientMain
```
