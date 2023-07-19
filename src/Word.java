import java.util.Set;
import java.util.LinkedHashSet;


public class Word {

    /**
     * La parola da indovinare è rappresentata da una stringa
     * e da un insieme di utenti che hanno giocato tale parola.
     */
    private final String currentWord;
    private Set<String> userSet;


    /**
     * Costruttore della classe Word.
     * @param word la parola da rappresentare.
     * @throws IllegalArgumentException se la parola passata come parametro è nulla.
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
     * Metodo che restituisce la parola rappresentata dall'oggetto Word.
     * @return la parola rappresentata dall'oggetto Word.
     */
    public String getWord() {
        return currentWord;
    }


    /**
     * Metodo che controlla se l'utente passato come parametro è presente nella lista degli utenti che hanno indovinato la parola rappresentata dall'oggetto Word.
     * @param user l'utente da cercare nella lista degli utenti che hanno indovinato la parola rappresentata dall'oggetto Word.
     * @return true se l'utente è presente nella lista degli utenti che hanno indovinato la parola rappresentata dall'oggetto Word, false altrimenti.
     */
    public boolean containsUser(String user) {
        return userSet.contains(user);
    }


    /**
     * Metodo che aggiunge un utente alla lista degli utenti che hanno indovinato la parola rappresentata dall'oggetto Word.
     * @param user l'utente da aggiungere alla lista degli utenti che hanno indovinato la parola rappresentata dall'oggetto Word.
     * @return true se l'utente è stato aggiunto correttamente alla lista degli utenti che hanno indovinato la parola
     *         rappresentata dall'oggetto Word, false altrimenti.
     * @throws IllegalArgumentException se l'utente passato come parametro è nullo.
     */
    public boolean addUser(String user) {
        return userSet.add(user);
    }


    /**
     * Metodo che restituisce una stringa di maschera che rappresenta la corrispondenza tra la parola rappresentata dall'oggetto Word e la stringa passata come parametro.
     * @param tentativo la stringa da confrontare con la parola rappresentata dall'oggetto Word.
     * @return una stringa di maschera dove:
     *         X = lettera sbagliata
     *         + = lettera giusta al posto giusto
     *         ? = lettera giusta al posto sbagliato
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