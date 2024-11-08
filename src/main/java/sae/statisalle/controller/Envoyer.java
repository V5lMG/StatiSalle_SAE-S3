/*
 * Envoyer.java                 30/10/2024
 * IUT DE RODEZ                 Pas de copyrights
 */
package sae.statisalle.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sae.statisalle.Reseau;
import sae.statisalle.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur pour gérer l'envoi de fichiers dans l'application StatiSalle.
 * Permet de sélectionner des fichiers à envoyer et de gérer l'interface.
 *
 * @author Valentin MUNIER-GENIE
 *         Erwan THIERRY
 */
public class Envoyer {

    /**
     * Instance de la classe Reseau utilisée pour gérer la connexion
     * et les communications avec le serveur.
     */
    private Reseau reseau;

    /**
     * Liste contenant les chemins des fichiers sélectionnés pour l'envoi.
     * Chaque chemin représente un fichier que l'utilisateur souhaite
     * envoyer au serveur.
     */
    private List<String> cheminsDesFichiers;

    @FXML
    private Text cheminFx;

    @FXML
    private Text nomFx;

    @FXML
    private Text cheminFichier;

    @FXML
    private Text nomFichier;

    @FXML
    private Text adresseIP;

    @FXML
    private Text ipFx;

    @FXML
    private Button btnEnvoyer;

    /**
     * Gère l'action de retour à l'écran d'accueil de l'application.
     */
    @FXML
    void actionRetour() {
        MainControleur.activerAccueil();
    }

    /**
     * Gère l'action d'affichage de l'aide relative à l'envoi de fichiers.
     */
    @FXML
    void actionAide() {
        MainControleur.activerAideEnvoyer();
    }

    /**
     * Initialise l'état de l'interface, désactivant le bouton d'envoi.
     */
    @FXML
    void initialize() {
        btnEnvoyer.setDisable(true);
    }

    /**
     * Ouvre un sélecteur de fichiers pour
     * choisir des fichiers à envoyer.
     * Met à jour l'interface avec les chemins
     * et noms des fichiers sélectionnés.
     */
    @FXML
    private void actionChoixFichier() {
        ipFx.setText(Session.getAdresseIp());

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir des fichiers");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );

        Stage stage = MainControleur.getFenetrePrincipale();
        List<File> fichiers = fileChooser.showOpenMultipleDialog(stage);

        if (fichiers != null && fichiers.size() <= 4) {
            StringBuilder chemins = new StringBuilder();
            StringBuilder noms = new StringBuilder();
            cheminsDesFichiers = new ArrayList<>();

            for (File fichier : fichiers) {
                cheminsDesFichiers.add(fichier.getPath());
                chemins.append(fichier.getAbsolutePath()).append("\n");
                noms.append(fichier.getName()).append("\n");
            }

            cheminFx.setText(chemins.toString());
            nomFx.setText(noms.toString());

            cheminFx.setStyle("-fx-fill: #000000;");
            nomFx.setStyle("-fx-fill: #000000;");
            cheminFichier.setStyle("-fx-fill: #000000;");
            nomFichier.setStyle("-fx-fill: #000000;");
            ipFx.setStyle("-fx-fill: #000000;");
            adresseIP.setStyle("-fx-fill: #000000;");
            btnEnvoyer.setStyle("-fx-background-color: #4CAF50;");
            btnEnvoyer.setDisable(false);

            System.out.println("Fichiers sélectionnés : \n" + chemins);
        } else if (fichiers != null) {
            System.out.println("Vous devez sélectionner "
                               + "au maximum 4 fichiers.");
            showAlert("Trop de fichier",
                    "Vous devez sélectionner au maximum 4 fichiers.");
        } else {
            System.out.println("Aucun fichier sélectionné.");
            showAlert("Pas de fichier",
                    "Aucun fichier sélectionné.");
        }
    }

    /**
     * Gère l'envoi des fichiers sélectionnés au serveur.
     * Tente d'envoyer chaque fichier et de traiter la réponse du serveur.
     */
    @FXML
    void actionEnvoyer() {
        Alert envoyer = new Alert (Alert.AlertType.CONFIRMATION);
        envoyer.setTitle("Envoyer");
        envoyer.setHeaderText(null);
        envoyer.setContentText("Voulez vous vraiment envoyer ce/ces fichier(s) ?");
        Optional<ButtonType> result = envoyer.showAndWait();

        if (result.get() == ButtonType.OK){
            try {
                reseau = Session.getReseau();
                List<File> fichiers = new ArrayList<>();

                // ajouter les chemins des fichiers sélectionnés
                for (String cheminFichier : cheminsDesFichiers) {
                    fichiers.add(new File(cheminFichier));
                }

                // envoyer les fichiers
                for (File fichier : fichiers) {
                    reseau.envoyer(fichier.getPath());

                    // recevoir une réponse du serveur
                    String reponse = reseau.recevoirReponse();
                    reseau.utiliserReponse(reponse);
                }

                System.out.println("Tous les fichiers ont été envoyés !");
                afficherConfirmationEtRetour();

            } catch (IllegalArgumentException e) {
                System.err.println("Erreur d'envoi : " + e.getMessage());
                showAlert("Erreur d'envoi",
                        "Une erreur est survenue lors de l'envoi : "
                                + e.getMessage());

            } catch (Exception e) {
                System.err.println("Erreur inattendue : " + e.getMessage());
                showAlert("Erreur inattendue",
                        "Une erreur inattendue est survenue : "
                                + e.getMessage());

            } finally {
                reseau.fermerClient();
            }
        } else if (result.get() == ButtonType.CANCEL){
            envoyer.close();
        }
    }

    /**
     * Affiche une alerte pour informer
     * l'utilisateur d'une situation spécifique.
     * @param title   le titre de l'alerte.
     * @param message le message de l'alerte.
     */
    private void showAlert(String title,
                           String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une alerte de confirmation et redirige vers l'écran de connexion.
     */
    private void afficherConfirmationEtRetour() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Envoi réussi");
        alert.setHeaderText(null);
        alert.setContentText("Les fichiers ont été envoyés avec succès.");

        alert.setOnHidden(evt -> MainControleur.activerConnexion());
        alert.showAndWait();
    }
}
