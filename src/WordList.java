import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Classe che rappresenta il vocabolario usato da Wordle per estrarre la Secret-Word e controllare la validità
 * delle Guessed-Word inserite dall'utente
 * <br>
 * La classe è implementata usando tre strutture dati principali:
 * <ul>
 * <li>una <code>List&lt;String&gt;</code> che contiene le parole del vocabolario</li>
 * <li>un <code>ScheduledExecutorService</code> che estrae una parola casuale dal vocabolario ogni <i>n</i> secondi</li>
 * <li>un oggetto <code>Word</code> che rappresenta la parola corrente</li>
 * </ul>
 */
public class WordList {

    /**
     * Variabili utili.
     *
     * - wordVocabulary: List<String> che contiene le parole del vocabolario
     * - wordExtractor: ScheduledExecutorService che estrae una parola casuale dal vocabolario ogni n secondi
     * - currentWord: Word che rappresenta la parola corrente
     */
    private List<String> wordVocabulary;
    private ScheduledExecutorService wordExtractor;
    private Word currentWord;


    /**
     * Runnable che rappresenta il task di estrazione casuale di una parola (currentWord) dal vocabolario di parole.
     */
    private Runnable extractWord = () -> {
        synchronized (this) {
            currentWord = new Word(wordVocabulary.get((int) (Math.random() * wordVocabulary.size())));
        }
    };


    /**
     * Costruttore della classe <code>WordList</code>.
     *
     * @param pathVocabulary  percorso del file contenente il vocabolario di parole
     * @param wordTimer  tempo in secondi tra l'estrazione di una parola casuale dal vocabolario
     * @throws FileNotFoundException se il file contenente il vocabolario di parole non viene trovato
     * @throws IOException se si verifica un errore durante la lettura del file contenente il vocabolario di parole
     */
    public WordList(String pathVocabulary, int wordTimer) throws FileNotFoundException, IOException {
        wordVocabulary = new ArrayList<>();
        wordExtractor = Executors.newSingleThreadScheduledExecutor();
        // ---
        try (BufferedReader reader = new BufferedReader(new FileReader(pathVocabulary))) {
            String line;
            while ((line = reader.readLine()) != null) {
                wordVocabulary.add(line);
            }
        }
        // ---
        wordVocabulary.sort(String::compareToIgnoreCase);
        wordExtractor.scheduleAtFixedRate(extractWord, 0, wordTimer, TimeUnit.SECONDS);
    }


    /**
     * Metodo che restituisce la parola corrente.
     *
     * @return parola corrente (Secret-Word)
     */
    public synchronized Word getCurrentWord() {
        return currentWord;
    }


    /**
     * Metodo che restituisce l'ExecutorService utilizzato per l'estrazione della Secret-Word dal vocabolario.
     *
     * @return ExecutorService utilizzato per l'estrazione della Secret-Word
     */
    public ExecutorService getSheduler() {
        return wordExtractor;
    }


    /**
     * Metodo che controlla se la parola passata come argomento è presente nel vocabolario.
     *
     * @param word  parola da cercare nel vocabolario
     * @return <code>true</code> se la parola è presente nel vocabolario, <code>false</code> altrimenti
     */
    public boolean containsWord(String word) {
        return wordVocabulary.contains(word);
    }

}  // class WordList
