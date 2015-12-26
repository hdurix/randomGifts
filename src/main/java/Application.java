import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by hippo on 29/11/2015.
 */
public class Application {

    private static final Logger logger = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) {

        boolean sendMail = false;

        ArrayList<String> aRenvoyer = new ArrayList<>();

        if (args.length > 0 && args[0].toUpperCase().contains("RENVOI")) {
            if (args.length > 1) {
                for (int i = 1; i < args.length; i++) {
                    System.out.println("renvoie le mail des cadeaux à " + args[i]);
                    aRenvoyer.add(args[i].toUpperCase());
                }
            } else {
                System.out.println("renvoie le mail des cadeaux à tout le monde");
            }
            renvoiMail(aRenvoyer);
            return;
        }

        ArrayList<Personne> personnes = new ArrayList<>();

        ArrayList<String> prenoms = new ArrayList<>();

        Personne pers;

        ArrayList<String> tabLignes = new ArrayList<>();
        BufferedReader buff;
        try {
//            buff = new BufferedReader(new FileReader("D:\\Users\\Hippolyte\\Documents\\Java\\CadeauxNoel\\cadeau Durix.txt"));
            buff = new BufferedReader(new FileReader("cadeau Bad.txt"));
//            buff = new BufferedReader(new FileReader("cadeau Ripounais.txt"));

            String str;
            while ((str = buff.readLine()) != null) {
                tabLignes.add(str);
            }
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }

        System.out.println("Il y a " + tabLignes.size() + " personnes.");

        String[] tabPers;

        String[] ex;

        for (String p : tabLignes) {

            System.out.println("ligne = " + p);

            tabPers = p.split(";");
            pers = new Personne();
            pers.prenom = tabPers[0];
            prenoms.add(pers.prenom);
            pers.mail = tabPers[1];
            if (tabPers.length > 2) {
                ex = tabPers[2].split(",");
                System.out.println(ex.length + " choix possibles");
                pers.exclus.addAll(Arrays.asList(ex));
            }

            personnes.add(pers);
        }

        HashMap<Personne, String> expDest = null;

        int nbEssais = 0;

        while (expDest == null) {
            expDest = selectDest(personnes, prenoms);
            nbEssais++;
        }

        System.out.println(nbEssais + " essais");
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();

        if (sendMail) {
            String body;
            for (Personne p : expDest.keySet()) {
                System.out.println("mail to " + p.mail + " : Cadeau Noël 2015 pour " + p.prenom);

                body = "Tu dois offir un cadeau à "
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n..............................................\n"
                        + expDest.get(p);

                //System.out.println(body);
                if (!envoiMail(p.mail, "Cadeau Noël 2015 pour "
                        + p.prenom, body)) {
                    return;
                }

            }
        } else {

            for (Personne p : expDest.keySet()) {
                System.out.println("mail to " + p.mail + " : Cadeau Noël 2015 pour " + p.prenom);
            }
            int i = 0;
            Iterator<Personne> iterator = expDest.keySet().iterator();
            Personne p = iterator.next();
            List<Personne> ecrits = new ArrayList<>(expDest.size());
            System.out.print(p.prenom);
            for (int j = 0; j < expDest.size(); j++) {
                while (ecrits.contains(p)) {
                    p = iterator.next();
                    if (!ecrits.contains(p)) {
                        System.out.println();
                        System.out.print(p.prenom);
                    }
                }
                ecrits.add(p);
                String dest = expDest.get(p);
                System.out.print(" ==> " + dest);
                p = expDest.keySet().stream().filter(personne -> personne.prenom == dest).findFirst().get();
            }

            System.out.println();

        }

        System.out.println(nbEssais + " essais");

        ecritDansFichier(expDest);

    }

    public static HashMap<Personne, String> selectDest(
            ArrayList<Personne> personnes, ArrayList<String> prenoms) {

        HashMap<Personne, String> expDest = new HashMap<>();

        String sExclus;

        ArrayList<String> possibles;

        ArrayList<String> resteAfaire = (ArrayList<String>) prenoms.clone();

        String dest;

        for (Personne p : personnes) {
            sExclus = "";
            possibles = (ArrayList<String>) resteAfaire.clone();
            System.out.println(p.prenom + " : " + possibles.size() + " possibles");
            possibles.remove(p.prenom);
            for (String x : p.exclus) {
                System.out.println(p.prenom + " : remove " + x);
                possibles.remove(x);
            }
            if (possibles.isEmpty()) {
                return null;
            }
            for (String pos : possibles) {
                sExclus += "à " + pos + " ou ";
            }
            sExclus = sExclus.substring(0, sExclus.lastIndexOf(" ou "));
            System.out.println("Personne " + p.prenom + " peut offrir " + sExclus + " (" + possibles.size() + " possibles)");
            Random r = new Random();
            dest = possibles.get(r.nextInt(possibles.size()));
            resteAfaire.remove(dest);
            System.out.println("Personne " + p.prenom + " offre à " + dest);

            expDest.put(p, dest);
        }
        return expDest;
    }

    public static boolean envoiMail(String to, String subject, String text) {

        // Sender's email ID needs to be mentioned
        String from = "hippo_@hotmail.fr";

        // Assuming you are sending email from localhost
//        String host = "smtp.orange.fr";
//        String host = "smtp.sfr.fr";
        String host = "smtp.free.fr";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("hippo_@hotmail.fr"));

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setText(text);

            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            System.out.println(mex);
            return false;
        }
        return true;
    }

    public static void ecritDansFichier(HashMap<Personne, String> expDest) {

        try {

//            File file = new File("D:\\Users\\Hippolyte\\Documents\\Java\\CadeauxNoel\\sauvegarde Durix.txt");
//            File file = new File("sauvegarde Bad.txt");
            File file = new File(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".txt");

            System.out.println("sauvegarde dans " + file.getAbsolutePath());

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                for (Personne p : expDest.keySet()) {
                    bw.write(p.mail + ";" + p.prenom + ";" + expDest.get(p) + "\n");
                }
            }

            System.out.println("Done");

        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    public static void renvoiMail(ArrayList<String> personnesSpecifiques) {
        ArrayList<String> tabLignes = new ArrayList<>();
        BufferedReader buff;
        try {
            buff = new BufferedReader(new FileReader("D:\\Users\\Hippolyte\\Documents\\Java\\CadeauxNoel\\sauvegarde Durix.txt"));
            String str;
            while ((str = buff.readLine()) != null) {
                tabLignes.add(str);
            }
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }

        String[] split;
        String body;

        for (String line : tabLignes) {

            split = line.split(";");

            if (personnesSpecifiques.isEmpty()
                    || personnesSpecifiques.contains(split[1].toUpperCase())) {

                System.out.println("mail to " + split[0] + " : Cadeau Noël 2015 pour " + split[1]);

                body = "Tu dois offir un cadeau à "
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n.............................................."
                        + "\n..............................................\n"
                        + split[2];

                envoiMail(split[0], "[RAPPEL] Cadeau Noël 2015 pour " + split[1], body);
            }
        }
    }
}
