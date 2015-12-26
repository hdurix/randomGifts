import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Hippolyte
 */
public class Personne {
    
    private String prenom;
    
    private String mail;
    
    private List<String> exclus = new ArrayList<>();

    public Personne(String line) {
        String[] tabPers = line.split(";");
        prenom = tabPers[0];
        mail = tabPers[1];
        if (tabPers.length > 2) {
            String[] ex = tabPers[2].split(",");
            System.out.println(ex.length + " choix possibles");
            exclus.addAll(Arrays.asList(ex));
        }
    }

    public String getPrenom() {
        return prenom;
    }

    public String getMail() {
        return mail;
    }

    public List<String> getExclus() {
        return exclus;
    }

    @Override
    public String toString() {
        return prenom;
    }
}
