import java.util.Set;
import java.util.LinkedHashSet;


/**
 * Classe che rappresenta la parola da indovinare (Secret-Word).
 * <br>
 * La classe è implementata usando due strutture dati principali:
 * <ul>
 * <li>una <code>String</code> che rappresenta l'effettiva parola da indovinare</li>
 * <li>un <code>Set&lt;String&gt;</code> che contiene i nomi degli utenti che hanno già giocato la parola</li>
 * </ul>
 */
public class Word {

    /**
     * Variabili utili.
     *
     * - currentWord: String che rappresenta la parola da indovinare
     * - userSet: Set<String> che contiene i nomi degli utenti che hanno già giocato la parola
     */
    private final String currentWord;
    private Set<String> userSet;


    /**
     * Costruttore della classe <code>Word</code>.
     *
     * @param word  la parola da rappresentare
     * @throws IllegalArgumentException se la parola passata come argomento è <i>null</i>
     */
    public Word(String word) {
        if (word == null) {
            throw new IllegalArgumentException("Un oggetto di tipo Word non può contenere una parola nulla");
        }
        // ---
        this.currentWord = word;
        this.userSet = new LinkedHashSet<>();
    }


    /**
     * Metodo che restituisce la parola rappresentata dall'oggetto <code>Word</code>.
     *
     * @return la parola rappresentata dall'oggetto Word.
     */
    public String getWord() {
        return currentWord;
    }


    /**
     * Metodo che controlla se l'utente passato come argomento è presente nell'insieme degli utenti che hanno
     * già giocato la parola corrrente (Secret-Word).
     *
     * @param user  utente da cercare nell'insieme degli utenti che hanno già giocato la parola corrente
     * @return <i>true</i> se l'utente è presente nella lista degli utenti che hanno già giocato la parola corrente,
     *         <i>false</i> altrimenti.
     */
    public synchronized boolean containsUser(String user) {
        return userSet.contains(user);
    }


    /**
     * Metodo che aggiunge un utente alla lista degli utenti che hanno già giocato la parola corrente (Secret-Word).
     *
     * @param user  utente da aggiungere alla lista degli utenti che hanno già giocato la parola corrente
     * @return <i>true</i> se l'utente è stato aggiunto correttamente alla lista degli utenti che hanno già giocato
     *         la parola corrente, <i>false</i> altrimenti.
     * @throws IllegalArgumentException se l'utente passato come argomento è nullo.
     */
    public synchronized boolean addUser(String user) {
        return userSet.add(user);
    }


    /**
     * Metodo che restituisce una maschera (<code>String</code>) di caratteri speciali che rappresenta la corrispondenza
     * tra la parola rappresentata dall'oggetto <code>Word</code> e la stringa passata come argomento.
     *
     * @param tentativo  stringa da confrontare con la parola rappresentata dall'oggetto <code>Word</code>
     * @return una maschera (<code>String</code>) di caratteri speciali dove:
     *         <ul>
     *         <li><code>X</code> rappresenta una lettera sbagliata</li>
     *         <li><code>+</code> rappresenta una lettera giusta al posto giusto</li>
     *         <li><code>?</code> rappresenta una lettera giusta al posto sbagliato</li>
     *         </ul>
     */
    public String getMask(String tentativo) {
        String mask = "";
        for (int i = 0; i < tentativo.length(); i++) {
            for (int j = 0; j < currentWord.length(); j++) {
                if (currentWord.charAt(j) == tentativo.charAt(i)) {
                    if (i == j) {
                        mask += "+";
                    } else {
                        mask += "?";
                    }
                    break;
                } else if (j == currentWord.length() - 1) {
                    mask += "X";
                }
            }
        }
        return mask;
    }

}  // class Word
