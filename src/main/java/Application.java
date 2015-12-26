import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hippo on 29/11/2015.
 */
public class Application {

    private static ConfigurationProperties properties = new ConfigurationProperties();

    public static void main(String[] args) {

        String propFile;
        if(args.length == 0) {
            System.out.println("Attention. Le fichier de propriétés n'est pas renseigné, choix par défaut");
            propFile = "config.properties";
        } else {
            propFile = args[0];
        }
        try {
            properties.init(propFile);
        } catch (IOException | GiftException e) {
            System.err.println("Problème à la lecture de " + propFile);
            e.printStackTrace();
            return;
        }

        if (args.length > 1 && args[1].toUpperCase().contains("RENVOI")) {
            try {
                reSendMail(args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                randomGifts();
            } catch (IOException | GiftException e) {
                e.printStackTrace();
            }
        }

    }

    public static void randomGifts() throws IOException, GiftException {

        Path path = Paths.get(properties.getInputFile());
        List<String> tabLignes = Files.lines(path, Charset.forName("windows-1252")).collect(Collectors.toList());
        System.out.println("Il y a " + tabLignes.size() + " personnes.");

        List<Personne> personnes = tabLignes.stream().map(Personne::new).collect(Collectors.toList());

        HashMap<Personne, String> expDest = null;

        int nbEssais = 0;

        while (expDest == null) {
            expDest = selectDest(personnes);
            nbEssais++;
        }

        System.out.println(nbEssais + " essais");
        for (int i = 0; i < 25; i++) {
            System.out.println();
        }

        if (properties.getSendMail()) {
            if (prepareMails(expDest)) return;
        } else {
            prepareLoopPrint(expDest);
        }

        System.out.println(nbEssais + " essais");

        writeInOutputFile(expDest);

    }

    private static void prepareLoopPrint(HashMap<Personne, String> expDest) {
        for (Personne p : expDest.keySet()) {
            System.out.println("mail to " + p.getMail() + " : Cadeau " + properties.getTitle() + " pour " + p);
        }
        Iterator<Personne> iterator = expDest.keySet().iterator();
        Personne p = iterator.next();
        List<Personne> ecrits = new ArrayList<>(expDest.size());
        System.out.print(p);
        for (int j = 0; j < expDest.size(); j++) {
            while (ecrits.contains(p)) {
                p = iterator.next();
                if (!ecrits.contains(p)) {
                    System.out.println();
                    System.out.print(p);
                }
            }
            ecrits.add(p);
            String dest = expDest.get(p);
            System.out.print(" ==> " + dest);
            p = expDest.keySet().stream().filter(personne -> personne.getPrenom().equals(dest)).findFirst().get();
        }

        System.out.println();
    }

    private static boolean prepareMails(HashMap<Personne, String> expDest) {
        String body;
        for (Personne p : expDest.keySet()) {
            System.out.println("mail to " + p.getMail() + " : " + properties.getTitle() + " pour " + p);

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
            if (!envoiMail(p.getMail(), "Cadeau " + properties.getTitle() + " pour " + p, body)) {
                return true;
            }

        }
        return false;
    }

    public static HashMap<Personne, String> selectDest(List<Personne> personnes) {

        HashMap<Personne, String> expDest = new HashMap<>();

        String sExclus;

        ArrayList<String> possibles;

        ArrayList<String> resteAfaire = personnes.stream().map(Personne::getPrenom).collect(Collectors.toCollection(ArrayList::new));

        String dest;

        for (Personne p : personnes) {
            sExclus = "";
            possibles = (ArrayList<String>) resteAfaire.clone();
            System.out.println(p + " : " + possibles.size() + " possibles");
            possibles.remove(p.getPrenom());
            for (String x : p.getExclus()) {
                System.out.println(p + " : remove " + x);
                possibles.remove(x);
            }
            if (possibles.isEmpty()) {
                return null;
            }
            for (String pos : possibles) {
                sExclus += "à " + pos + " ou ";
            }
            sExclus = sExclus.substring(0, sExclus.lastIndexOf(" ou "));
            System.out.println("Personne " + p + " peut offrir " + sExclus + " (" + possibles.size() + " possibles)");
            Random r = new Random();
            dest = possibles.get(r.nextInt(possibles.size()));
            resteAfaire.remove(dest);
            System.out.println("Personne " + p + " offre à " + dest);

            expDest.put(p, dest);
        }
        return expDest;
    }

    public static boolean envoiMail(String to, String subject, String text) {

        String from = properties.getFrom();
        String host = properties.getSmtp();

        Properties systemProperties = System.getProperties();
        systemProperties.setProperty("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(systemProperties);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            String additionnalRecipient = properties.getAdditionnalRecipient();
            if (additionnalRecipient != null) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(additionnalRecipient));
            }

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setText(text);

            // Send message
            if (!properties.isDebug()) {
                Transport.send(message);
            }
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            System.err.println(mex);
            return false;
        }
        return true;
    }

    public static void writeInOutputFile(HashMap<Personne, String> expDest) throws IOException, GiftException {


        File file = new File(properties.getOutputFile());

        System.out.println("sauvegarde dans " + file.getAbsolutePath());

        // if file doesnt exists, then create it
        if (!file.exists()) {
            boolean created = file.createNewFile();
            if (!created) {
                System.err.println("impossible de créer " + file);
                throw new GiftException();
            }
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            for (Personne p : expDest.keySet()) {
                bw.write(p.getMail() + ";" + p.getPrenom() + ";" + expDest.get(p) + "\n");
            }
        }

        System.out.println("Done");

    }

    public static void reSendMail(String[] args) throws IOException {

        List<String> personnesSpecifiques = new ArrayList<>();

        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                System.out.println("renvoie le mail des cadeaux à " + args[i]);
                personnesSpecifiques.add(args[i].toUpperCase());
            }
        } else {
            System.out.println("renvoie le mail des cadeaux à tout le monde");
        }

        List<String> tabLignes = Files.lines(Paths.get("sauvegarde Bad.txt")).collect(Collectors.toList());

        String[] split;
        String body;

        for (String line : tabLignes) {

            split = line.split(";");

            if (personnesSpecifiques.isEmpty() || personnesSpecifiques.contains(split[1].toUpperCase())) {

                System.out.println("mail to " + split[0] + " : " + properties.getTitle() + " pour " + split[1]);

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

                envoiMail(split[0], "[RAPPEL] " + properties.getTitle() + " pour " + split[1], body);
            }
        }
    }
}
