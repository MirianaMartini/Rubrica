package database;

import jdk.internal.access.JavaIOFileDescriptorAccess;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.nio.file.Paths;


public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        //per permettere la lettura del file properties con i dati per connettere il DB esterni al progetto devo inserire il file nello stesso percorso dove si trova il .jar
        String currentDirectory = System.getProperty("user.dir");
        /* //Per farlo funzionare runnandolo da codice e non da jar commente le due righe sotto questo commento e decommenta questo
        Path parentPath = Paths.get(currentDirectory).getParent();
        String parentDirectory = parentPath.toString();
        String fullPath = Paths.get(parentDirectory, "/credenziali_database.properties").toString();
        */
        String fullPath = Paths.get(currentDirectory, "/credenziali_database.properties").toString();
        System.out.println("Il percorso completo del file è: " + fullPath);



        //try (InputStream input = database.DatabaseConfig.class.getClassLoader().getResourceAsStream("credenziali_database.properties")) {
        try (InputStream input = new FileInputStream(fullPath)) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String getUsername() {
        return properties.getProperty("username");
    }

    public static String getPassword() {
        return properties.getProperty("password");
    }

    public static String getServerIP() {
        return properties.getProperty("ip-server-mysql");
    }

    public static String getPorta() {
        return properties.getProperty("porta");
    }

}
