package com.hamidou.whatsappclone.file;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileUtils {

    // Constructeur privé pour empêcher l’instanciation de la classe utilitaire
    private FileUtils() {}

    /**
     * Lit un fichier depuis le disque et retourne son contenu sous forme de tableau de bytes
     * @param fileUrl Chemin absolu du fichier à lire
     * @return Contenu du fichier sous forme de byte[], ou tableau vide en cas d'erreur
     */
    public static byte[] readFileFromLocation(String fileUrl) {
        // Vérifie si le chemin est vide ou nul
        if (StringUtils.isBlank(fileUrl)) {
            return new byte[0]; // Retourne un tableau vide
        }

        try {
            // Convertit le chemin en objet Path et lit le fichier
            Path filePath = new File(fileUrl).toPath();
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            // Log en cas d'erreur de lecture
            log.warn("No file found in the path {}", fileUrl);
        }

        return new byte[0]; // Retourne un tableau vide si erreur
    }
}

