/*
 * TestChiffrement.java               11/11/2024
 * IUT DE RODEZ,pas de copyrights
 */
package sae.statisalle;

import org.junit.jupiter.api.Test;
import sae.statisalle.exception.ModuloNegatifException;
import sae.statisalle.modele.Vigenere;
import sae.statisalle.modele.Fichier;


import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CLasse qui teste chaque méthode de la classe Chiffrement
 * @author Robin Montes
 * @author Mathias Cambon
 * @author rodrigoxaviertaborda
 */
public class TestVigenere {

    /*
    public static void testDefTailleClef() {
       Vigenere.creerAlphabet();
       List<String> donnees = new ArrayList<>(); // Exemple de données
       donnees.add("Ceci");
       donnees.add("Salut");
       String cle = Vigenere.genererCleAleatoire(donnees);
       System.out.println("cle générer : " + cle);
       String cle2 = Vigenere.defTailleClef(donnees,cle);
       System.out.println("Clé adapter à la taille du texte : " + cle2);
    }
    */

    public static boolean testChiffrementDechiffrementDonnees(String cheminFichier) {
        Fichier fichier = new Fichier(cheminFichier);
        List<String> contenuOriginalList = fichier.contenuFichier(); // Assurez-vous que cette méthode retourne le contenu du fichier en String
        String contenuOriginal = String.join("\n", contenuOriginalList);

        if (contenuOriginal == null || contenuOriginal.isEmpty()) {
            System.err.println("Erreur : Le fichier est vide ou introuvable.");
            return false;
        }

        // Affiche le contenu original
        System.out.println("Contenu original :");
        System.out.println(contenuOriginal);

        // Générer une clé aléatoire pour le test
        String cle = Vigenere.genererCleAleatoire(contenuOriginal);
        System.out.println("Clé générée : " + cle);

        // Chiffrer le contenu avec la clé générée
        String contenuChiffre = Vigenere.chiffrementDonnees(contenuOriginal, cle);
        System.out.println("Contenu chiffré :");
        System.out.println(contenuChiffre);

        // Créer un nouveau fichier temporaire avec le contenu chiffré
        Fichier fichierTemp = new Fichier("/Users/rodrigoxaviertaborda/Documents/SAE/temp.csv");
        fichierTemp.reecritureFichier(Arrays.asList(contenuChiffre.split("\n")));

        // Déchiffrer le contenu chiffré en utilisant la même clé
        String contenuDechiffre = Vigenere.dechiffrementDonnees(contenuChiffre, cle);
        System.out.println("Contenu déchiffré :");
        System.out.println(contenuDechiffre);

        // Comparer le contenu déchiffré avec le contenu original
        String contenuOriginalNettoye = contenuOriginal.replaceAll("\r\n", "").trim();
        String contenuDechiffreNettoye = contenuDechiffre.replaceAll("\r\n", "").trim();
        boolean resultat = contenuDechiffreNettoye.equals(contenuOriginalNettoye);

        if (resultat) {
            System.out.println("Le chiffrement et le déchiffrement ont réussi. Le contenu est identique !");
        } else {
            System.err.println("Échec du test : le contenu déchiffré est différent du contenu original.");
        }

        return resultat;
    }


    public static void main(String[] args) {
        // Chemin vers le fichier que vous voulez tester
        String cheminFichierTest = "/Users/rodrigoxaviertaborda/Documents/SAE/salles 26_08_24 13_40.csv";

        // Appel de la méthode de test pour vérifier le chiffrement et le déchiffrement
        boolean estReussi = testChiffrementDechiffrementDonnees(cheminFichierTest);

        // Afficher le résultat du test dans la console
        if (estReussi) {
            System.out.println("Test réussi : le contenu déchiffré est identique au contenu original.");
        } else {
            System.out.println("Test échoué : le contenu déchiffré diffère du contenu original.");
        }

    }
}
