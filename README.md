# Wordle 3.0

Progetto di fine corso *Laboratorio di Reti* 2022-2023.

- [Relazione *PDF*](https://www.geoteo.net/wordle/relazione/relazione.pdf).
- [Relazione *HTML*](https://www.geoteo.net/wordle/relazione/notes/relazione.html)
- [Documentazione *JavaDoc*](https://www.geoteo.net/wordle/doc/allclasses-index.html)
- [Sorgenti *Java*](https://github.com/matteogiorgi/wordle/tree/master/src)
- [Specifiche](https://www.geoteo.net/wordle/specifiche-wordle.pdf)




## Generare gli eseguibili

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

Creare gli eseguibili `.jar` per server e client:

```bash
echo "Main-Class: ServerMain" | jar cvfm WordleServer.jar - -C bin-server/ .
echo "Main-Class: ClientMain" | jar cvfm WordleClient.jar - -C bin-client/ .
```




## Eseguire *Wordle 3.0*

Il repo contiene già i file `.jar` pronti per essere testati. Per eseguire il programma lanciare quindi le seguenti due istruzioni in due istanze separate di `sh`:

```
[ server ] >> java -cp WordleServer.jar:lib/gson-2.10.jar ServerMain
[ client ] >> java -cp WordleClient.jar ClientMain
```
