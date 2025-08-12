package org.GT659010.UserHandling;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    // La mappa ora è privata e gestita solo da questa classe
    private static final ConcurrentHashMap<String, User> USERS = new ConcurrentHashMap<>();
    private static final Path USER_FILE = Paths.get("users.json");
    private static final ObjectMapper USER_MAPPER =
            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void loadUsersFromFile() {
        File file = USER_FILE.toFile();
        if (file.exists() && file.length() > 0) {
            try {
                // Legge direttamente il file nella mappa USERS (con chiave UUID)
                TypeReference<ConcurrentHashMap<String, User>> typeRef = new TypeReference<ConcurrentHashMap<String, User>>() {};
                Map<String, User> loadedUsers = USER_MAPPER.readValue(file, typeRef);
                USERS.putAll(loadedUsers);
                System.out.println("UserManager: Caricati " + USERS.size() + " utenti (chiave: UUID).");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("UserManager: File utenti non trovato. Inizio con mappa vuota.");
        }
    }

    // Metodo per ottenere un utente
    public static User getUser(String uuid) {
        return USERS.get(uuid);
    }

    // Metodo per registrare un nuovo utente (thread-safe)
    public static void saveUsersToFile() {
        try {
            // Salva direttamente la mappa USERS (che ha già l'UUID come chiave)
            USER_MAPPER.writeValue(USER_FILE.toFile(), USERS);
            System.out.println("UserManager: Mappa utenti salvata su file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo per dare accesso all'intera mappa (da passare all'OrderBook)
    public static Map<String, User> getUsers() {
        return USERS;
    }
}
