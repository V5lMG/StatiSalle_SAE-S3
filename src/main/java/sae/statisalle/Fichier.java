/*
 * Fichier.java               21/10/2024
 * IUT DE RODEZ               Pas de copyrights
 */
package sae.statisalle;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.err;

/**
 * La classe Fichier gère les opérations de lecture
 * sur des fichiers Comma Separated Values (csv). Elle permet de lire le contenu
 * d'un fichier ligne par ligne
 * <br>
 * Les fichiers pris en charge doivent avoir une extension .csv ou .CSV.
 * La classe gère également les erreurs courantes liées à l'ouverture,
 * la lecture et la fermeture des fichiers.
 * @author erwan.thierry
 * @author MathiasCambon
 */
public class Fichier {

    private static final String ERREUR_ECRITURE_FICHIER =
            "erreur : impossible d'écrire dans le fichier";

    /* fichier courant de l'instance */
    private File fichierExploite;

    /* lecteur du fichier courant de l'instance */
    private FileReader lecteurFichier;

    /* */
    private BufferedReader tamponFichier;

    /** impossible d'ouvrir le fichier renseigné */
    private static final String ERREUR_OUVERTURE_FICHIER =
            "erreur : Impossible d'ouvrir le fichier au lien : ";

    /** impossible de fermer le fichier renseigné */
    private static final String ERREUR_FERMETURE_FICHIER =
            "erreur : Impossible de fermer le fichier.";

    /** impossible d'acceder au contenu du fichier **/
    private static final String ERREUR_CONTENU_FICHIER =
            "erreur : Impossible d'accéder au contenu du fichier.";

    /** impossible de créer le fichier, droits insuffisants **/
    private static final String ERREUR_CREATION_FICHIER =
            "erreur : Impossible de créer le fichier, droits insuffisants. ";

    /** le format du fichier n'est pas .txt ou .TXT **/
    private static final String ERREUR_EXTENSION_FICHIER =
            "erreur : Le format du fichier doit être .csv";

    /** le format du paramètre est invalide **/
    private static final String ERREUR_FORMAT_PARAMETRE =
            "erreur : Le format du paramètre renseigné invalide.";

    /** Suffixe des fichiers pris en charge **/
    private static final String SUFFIXE_FICHIER = ".csv";

    /**
     * Constructeur de la classe Fichier.
     * <br>
     * Initialise les objets nécessaires pour lire le fichier
     * spécifié par le chemin fourni.
     * <p>
     * Vérifie si le chemin du fichier est valide et si le
     * fichier a une extension correcte (.txt ou .bin).
     *
     * @param cheminFichier : Le chemin du fichier à lire.
     */
    public Fichier(String cheminFichier) {
        if (cheminFichier == null || cheminFichier.isEmpty()) {
            err.println(ERREUR_FORMAT_PARAMETRE);
            return;
        }

        this.fichierExploite = new File(cheminFichier);

        if (!extensionValide()) {
            err.println(ERREUR_EXTENSION_FICHIER);
        }

        try {
            if (!fichierExploite.exists()) {
                err.println(ERREUR_OUVERTURE_FICHIER + cheminFichier + " : Le fichier n'existe pas.");
            } else if (!fichierExploite.canWrite()) {
                err.println(ERREUR_CREATION_FICHIER + cheminFichier);
            }

            this.lecteurFichier = new FileReader(fichierExploite);
            this.tamponFichier = new BufferedReader(this.lecteurFichier);

        } catch (IOException e) {
            err.println(ERREUR_OUVERTURE_FICHIER + cheminFichier + " : " + e.getMessage());
        }
    }


    /**
     * Vérifie si le fichier a une extension valide.
     *
     * @return true si l'extension du fichier est valide,
     * 	       false si l'extension du fichier n'est pas valide.
     */
    public boolean extensionValide(){
        return this.fichierExploite.getName().endsWith(SUFFIXE_FICHIER);
    }

    /**
     * Lit le contenu d'un fichier texte ligne par ligne et retourne une liste
     * de chaînes de caractères contenant chaque ligne du fichier.
     * <br>
     * Si une erreur survient pendant la lecture du fichier,
     * un message d'erreur est affiché et la méthode retourne une liste vide.
     *
     * @return Une liste de chaînes de caractères contenant les lignes du fichier,
     *         ou Une liste vide si une erreur survient.
     */
    public List<String> contenuFichier() {
        List<String> contenu = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(this.fichierExploite))) {
            String line;
            while ((line = br.readLine()) != null) {
                contenu.add(line);
            }

        } catch (IOException e) {
            err.println(ERREUR_CONTENU_FICHIER);
        }
        return contenu;
    }

    /**
     * Stocke les données d'un fichier CSV sans l'en-tête dans une liste de listes.
     * Chaque ligne du fichier CSV est transformée en une liste de chaînes de caractères.
     *
     * La première ligne du fichier (l'en-tête) n'est pas pris en compte dans le traitement.
     * Les lignes suivantes représentent les données du fichier, chaque cellule étant séparée par un point-virgule (';').
     * Si la cellule est vide alors un espace est mis à sa place.
     *
     * @return une liste de listes de chaînes de caractères représentant les données du fichier CSV.
     *         Chaque sous-liste suivante représente une ligne de données.
     *         Si le fichier est vide ou ne contient pas de données, une liste vide est retournée.
     */
    public List<List<String>> recupererDonnees() {
        List<List<String>> tableau3D = new ArrayList<>();
        List<String> contenu = contenuFichier();

        if (contenu.isEmpty()) {
            return tableau3D;
        }

        // Récupérer les entêtes de colonnes pour déterminer la taille des lignes
        String[] entetes = contenu.get(0).split(";");

        // Parcourt les lignes de données du fichier
        for (int i = 1; i < contenu.size(); i++) {
            String[] ligne = contenu.get(i).split(";");
            List<String> ligneTraitee = new ArrayList<>();

            // Traite chaque cellule de la ligne
            for (String cellule : ligne) {
                // Remplace les cellules vides au milieu de la ligne par un espace
                if (cellule.isEmpty()) {
                    ligneTraitee.add(" ");
                } else {
                    ligneTraitee.add(cellule);
                }
            }

            // Complète la ligne avec des espaces afin de faire la même taille que l'en-tête
            while (ligneTraitee.size() < entetes.length) {
                ligneTraitee.add(" ");
            }
            tableau3D.add(ligneTraitee);
        }
        return tableau3D;
    }

    /**
     * Réécrit le contenu du fichier avec une nouvelle liste de chaînes de caractères.
     * <br>
     * Chaque chaîne dans la liste représente une ligne qui sera écrite dans le fichier.
     * Si une erreur survient lors de l'écriture, un message d'erreur est affiché.
     * <br>
     * Le fichier d'origine est écrasé et remplacé par les nouvelles lignes.
     *
     * @param ContenuFichier : La liste de chaînes de caractères à écrire dans le fichier.
     */
    public void reecritureFichier(List<String> ContenuFichier) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.fichierExploite))) {
            for (String ligne : ContenuFichier) {
                writer.write(ligne);
                writer.newLine();
            }

        } catch (IOException e) {
            err.println(ERREUR_ECRITURE_FICHIER);
        }
    }

    /**
     * Ecrit le contenu du fichier.
     * <br>
     * Chaque chaîne dans la liste représente une ligne qui sera écrite dans le
     * fichier.
     * Si une erreur survient lors de l'écriture, un message d'erreur est
     * affiché.
     * <br>
     * Création d'un nouveau fichier.
     *
     * @param contenuFichier : La liste de chaînes de caractères à écrire dans
     * le fichier.
     * @param cheminFichier le chemin du fichier dans lequel il faut écrire.
     */
    public static void ecritureFichier(List<String> contenuFichier, String cheminFichier) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier))) {
            for (String ligne : contenuFichier) {
                writer.write(ligne);
                if (contenuFichier.size() >= 2) {
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            err.println(ERREUR_ECRITURE_FICHIER);
        }
    }

    /**
     * Recupère le nom du fichier sans son extention
     * @return nom du fichier
     */
    public String nomFichier(){
        String nomFichier = this.fichierExploite.getName();
        int pointIndex = nomFichier.lastIndexOf(".");

        if (pointIndex == -1) {
            return nomFichier;
        }

        return nomFichier.substring(0, pointIndex);
    }

    public static boolean fichierExiste(String fichier) {
        Path path = Paths.get(fichier);
        return Files.exists(path) && !Files.isDirectory(path);
    }

    /**
     * Retourne l'objet File représentant le fichier exploité.
     *
     * @return fichier exploité.
     */
    public File getFichierExploite() {
        return this.fichierExploite;
    }

    /**
     * Prend la premiere ligne du contenu du fichier et renvoie le type en
     * fonction de ce qu'il contient.
     * @return typeFichier est soit Salle, Employe, Activite ou Reservation
     */
    public String getTypeFichier() {

        String typeFichier;
        List<String> contenu;

        typeFichier = null;
        contenu = contenuFichier();

        if (contenu.getFirst().contains("Ident;Nom;Capacite;videoproj;"
                          + "ecranXXL;ordinateur;type;logiciels;imprimante")) {
            typeFichier = "Salle";
        }
        if (contenu.getFirst().contains("Ident;Nom;Prenom;Telephone")){
            typeFichier = "Employe";
        }
        if (contenu.getFirst().contains("Ident;Activité")){
            typeFichier = "Activite";
        }
        if (contenu.getFirst().contains("Ident;salle;employe;activite;"
                                        + "date;heuredebut;heurefin")) {
            typeFichier = "Reservation";
        } // else

        return typeFichier;
    }

    /**
     * Ouvre le dossier contenant le fichier fournit en argument.
     * @author valentin.munier-genie
     *
     * @param cheminFichier Le chemin du fichier.
     */
    public static void ouvrirDossier(String cheminFichier) {

        File fichier = new File(cheminFichier);
        File dossier = fichier.getParentFile();

        if (dossier.exists() && dossier.isDirectory()) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(dossier);
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'ouverture du dossier : " + e.getMessage());
                }
            } else {
                System.err.println("Ouverture de dossier non supportée "
                                   + "sur ce système.");
            }
        } else {
            System.err.println("Le dossier spécifié n'existe pas ou n'est "
                               + "pas un répertoire.");
        }
    }

    /**
     * Compte le nombre d'occurrences d'un motif dans un texte.
     * @author valentin.munier-genie
     *
     * @param texte Le texte à analyser.
     * @param motif Le motif recherché.
     * @return Le nombre d'occurrences du motif.
     */
    public static int compterMotif(String texte, String motif) {
        int compteur = 0;
        int index = 0;

        while ((index = texte.indexOf(motif, index)) != -1) {
            compteur++;
            index += motif.length();
        }
        return compteur;
    }
}