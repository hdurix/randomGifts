import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Created by hippo on 26/12/2015.
 */
public class ConfigurationProperties {

    private static final String OUTPUT_FILE = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".txt";
    private static final String FROM = "NePasRepondre@randomGifts";
    private static final boolean SEND_MAIL = false;
    private static final boolean DEBUG = false;

    private String title;
    private String inputFile;
    private String outputFile;
    private String smtp;
    private String from;
    private String additionnalRecipient;
    private boolean sendMail;
    private boolean debug;

    public String getTitle() {
        return title;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getSmtp() {
        return smtp;
    }

    public String getFrom() {
        return from;
    }

    public String getAdditionnalRecipient() {
        return additionnalRecipient;
    }

    public boolean getSendMail() {
        return sendMail;
    }

    public boolean isDebug() {
        return debug;
    }

    public void init(String propFileName) throws IOException, GiftException {
        Properties propValues = getPropValues(propFileName);
        title = getProperty(propValues, "title", null);
        inputFile = getProperty(propValues, "input_file", null);
        outputFile = getProperty(propValues, "output_file", OUTPUT_FILE, false);
        smtp = getProperty(propValues, "smtp", null);
        from = getProperty(propValues, "from", FROM, false);
        additionnalRecipient = getProperty(propValues, "additionnal_recipient", null, false);
        sendMail = getBooleanProperty(propValues, "send_mail", SEND_MAIL);
        debug = getBooleanProperty(propValues, "debug", DEBUG);
    }

    public Properties getPropValues(String propFileName) throws IOException {
        Properties prop = new Properties();
        if (propFileName == null) {
            System.err.println("Fichier de configuration inexistant");
        }
        InputStream inputStream = new FileInputStream(propFileName);
        prop.load(inputStream);
        return prop;
    }

    private String getProperty(Properties properties, String propName, String def) throws GiftException {
        return getProperty(properties, propName, def, true);
    }
    private String getProperty(Properties properties, String propName, String def, boolean required) throws GiftException {
        Object prop = properties.get(propName);
        if (prop == null || prop.toString().isEmpty()) {
            if (required) {
                System.err.println("La propriété "  + prop + " est obligatoire.");
                throw new GiftException();
            }
            return def;
        } else {
            return prop.toString();
        }
    }

    private boolean getBooleanProperty(Properties properties, String propName, Boolean def) throws GiftException {
        Object prop = properties.get(propName);
        if (prop == null) {
            if (def == null) {
                throw new GiftException();
            }
            return def;
        } else {
            return Boolean.parseBoolean(prop.toString());
        }
    }

}
