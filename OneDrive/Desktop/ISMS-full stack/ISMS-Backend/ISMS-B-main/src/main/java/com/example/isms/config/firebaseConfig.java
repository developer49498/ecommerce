package com.example.isms.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.FirebaseDatabase;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class firebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(firebaseConfig.class);

    @PostConstruct
    public void initialize() throws IOException {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                logger.info("Initializing Firebase...");
                FileInputStream serviceAccount = new FileInputStream("ServiceAccountKey.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://isms-f64f7-default-rtdb.firebaseio.com/")
                        .build();

                FirebaseApp.initializeApp(options);
                logger.info("Firebase initialized successfully");
            }

        } catch (Exception e) {
            logger.error("Failed to initialize Firebase: ", e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }

    @Bean
    public Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    @Bean
    public FirebaseDatabase firebaseDatabase() {
        return FirebaseDatabase.getInstance();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
