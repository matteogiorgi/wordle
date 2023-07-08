import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


public class User extends HashMap<String, Object> {
    public User() {
        super();
    }


    public User(Map<String, Object> map) {
        super(map);
    }


    public String getUserName() {
        return (String) get("user");
    }


    public String getPassword() {
        return (String) get("password");
    }


    public int getGiocate() {
        return (int) get("giocate");
    }


    public int getVinte() {
        return (int) get("vinte");
    }


    public int getStreakLast() {
        return (int) get("streaklast");
    }


    public int getStreakMax() {
        return (int) get("streakmax");
    }


    @SuppressWarnings("unchecked")
    public LinkedList<Integer> getGuessDistribution() {
        return (LinkedList<Integer>) get("guessd");
    }


    public void update(boolean win, int tentativi) {
        int streakLast = win ? getStreakLast()+1 :0;
        int streakMax = getStreakMax();

        // aggiorno i campi
        put("giocate", getGiocate() + 1);
        put("vinte", getVinte() + (win ? 1 : 0));
        put("streaklast", streakLast);
        put("streakmax", streakLast>=streakMax ? streakLast : streakMax);
        getGuessDistribution().add(tentativi);
    }
}
