import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
// ---
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;




public class WordList {

    private List<String> wordVocabulary;
    private ScheduledExecutorService wordExtractor;
    private Word currentWord;


    /**
     * Runnable che rappresenta il task di estrazione di una parola casuale dal vocabolario di parole.
     * La parola estratta viene assegnata all'attributo currentWord della classe WordList.
     */
    private Runnable extractWord = () -> {
        currentWord = new Word(wordVocabulary.remove((int) (Math.random() * wordVocabulary.size())));
    };


    /**
     * Costruttore della classe WordList.
     * @param pathVocabulary il percorso del file contenente il vocabolario di parole.
     * @param wordTimer il tempo in secondi tra l'estrazione di una parola casuale dal vocabolario.
     * @throws FileNotFoundException se il file contenente il vocabolario di parole non viene trovato.
     * @throws IOException se si verifica un errore durante la lettura del file contenente il vocabolario di parole.
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
     * @return la parola corrente.
     */
    public Word getCurrentWord() {
        return currentWord;
    }


    /**
     * Metodo che restituisce l'ExecutorService utilizzato per l'estrazione delle parole dal vocabolario.
     * @return l'ExecutorService utilizzato per l'estrazione delle parole dal vocabolario.
     */
    public ExecutorService getSheduler() {
        return wordExtractor;
    }


    public boolean containsWord(String word) {
        return wordVocabulary.contains(word);
    }

}  // class WordList
