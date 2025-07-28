package com.hamidou.whatsappclone.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    // Injecte le chemin de base pour les fichiers à uploader depuis application.properties
    @Value("${application.file.uploads.media-output.path}")
    private String fileUploadPath;

    /**
     * Enregistre un fichier dans un sous-répertoire spécifique à l'utilisateur
     * @param sourceFile Fichier à enregistrer
     * @param userId Identifiant de l'utilisateur
     * @return Chemin complet du fichier enregistré ou null en cas d'erreur
     */
    public String saveFile(@NonNull MultipartFile sourceFile, @NonNull String userId) {
        // Construit un sous-dossier basé sur l'identifiant de l'utilisateur
        final String fileUploadSubPath = "users" + File.separator + userId;

        // Appelle la méthode d'upload avec le sous-dossier
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    /**
     * Upload un fichier à l'emplacement spécifié
     * @param sourceFile Fichier source à enregistrer
     * @param fileUploadSubPath Sous-chemin relatif pour stocker le fichier
     * @return Chemin complet du fichier enregistré ou null
     */
    private String uploadFile(@NonNull MultipartFile sourceFile, @NonNull String fileUploadSubPath) {
        // Construit le chemin final où le fichier sera stocké
        final String finalUploadFile = fileUploadPath + File.separator + fileUploadSubPath;
        File targetFolder = new File(finalUploadFile);

        // Crée les dossiers si le chemin n'existe pas
        if (!targetFolder.exists()) {
            boolean folderCreated = targetFolder.mkdirs();
            if (!folderCreated) {
                log.error("Unable to create directory {}", finalUploadFile);
                return null;
            }
        }

        // Récupère l'extension du fichier (ex: .jpg, .pdf)
        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());

        // Crée un nom de fichier unique avec l'heure courante
        String targetFilePath = finalUploadFile + File.separator + System.currentTimeMillis() + fileExtension;
        Path targetPath = Paths.get(targetFilePath);

        try {
            // Écrit le contenu du fichier dans le fichier cible
            Files.write(targetPath, sourceFile.getBytes());
            log.info("File {} uploaded successfully", targetFilePath);
            return targetFilePath;
        } catch (Exception e) {
            log.error("Unable to write file {}", targetFilePath, e);
        }

        return null;
    }

    /**
     * Récupère l'extension d'un nom de fichier
     * @param filename Nom du fichier (ex: document.pdf)
     * @return Extension en minuscules (ex: .pdf), ou null si introuvable
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return null;
        }

        // Ajoute le point (.) à l'extension si tu veux, sinon retourne juste l'extension
        return "." + filename.substring(lastDotIndex + 1).toLowerCase();
    }
}

