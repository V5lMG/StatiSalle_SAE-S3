/*
 * Reseau.java                21/10/2024
 * Pas de droits d'auteur ni de copyright
 */
package sae.statisalle.modele;

import sae.statisalle.exception.MauvaiseConnexionServeur;

import java.io.*;
import java.net.*;
import java.io.IOException;

/**
 * Classe responsable de la gestion des communications réseau.
 * Elle permet l'envoi et la réception de données entre deux
 * différentes machines via des sockets.
 * La classe fournit des méthodes pour initialiser un serveur,
 * accepter des connexions de clients, envoyer et recevoir des données,
 * ainsi que pour gérer la fermeture des connexions.
 * </p>
 * @author valentin.munier-genie
 * @author rodrigo.xavier-taborda
 */
public class Reseau {

    /** Socket serveur pour écouter les connexions clients. */
    private ServerSocket serverSocket;

    /** Socket pour la connexion avec un client. */
    private Socket clientSocket;

    /** Flux de sortie pour envoyer des données. */
    protected PrintWriter fluxSortie;

    /** Flux d'entrée pour recevoir des données. */
    protected BufferedReader fluxEntree;

    // --------- PARTIE SERVEUR -----------

    /**
     * Initialise le serveur en récupérant l'IP et le port depuis la session.
     * Si l'IP ou le port n'est pas défini, une exception est levée.
     * Si le port est null, le port par défaut est utilisé.
     *
     * @param portParDefaut Port par défaut à utiliser si le port dans
     *                      la session est null.
     * @throws IllegalArgumentException Si le port spécifié n'est pas
     *                                  valide (0 - 65535) ou si l'IP
     *                                  est invalide.
     * @throws IOException Si une erreur survient lors de la création
     *                     du ServerSocket.
     */
    public void preparerServeur(int portParDefaut) throws IOException {
        System.out.println("[SERVEUR] Initialisation du serveur...");

        String ip = Session.getIpServeur();
        String portText = Session.getPortServeur();

        int port;
        if (portText == null) {
            port = portParDefaut;
            System.out.println("[SERVEUR] Aucun port défini dans la session. Utilisation du port par défaut : " + port);
        } else {
            try {
                port = Integer.parseInt(portText);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("[SERVEUR] Le port doit être un nombre valide.");
            }

            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("[SERVEUR] Le port doit être compris entre 0 et 65535.");
            }
        }

        try {
            if (ip != null && !ip.isEmpty()) {
                serverSocket = new ServerSocket(port, 50, InetAddress.getByName(ip));
                System.out.println("[SERVEUR] Serveur démarré sur l'IP " + ip + " et le port : " + port);
            } else {
                serverSocket = new ServerSocket(port);
                System.out.println("[SERVEUR] Aucune ip définie, le serveur se bind automatiquement. Serveur démarré sur le port : " + port);
            }
        } catch (IOException e) {
            throw new IOException("Erreur lors de la création du serveur : " + e.getMessage());
        } finally {
            if (serverSocket == null) {
                System.err.println("[SERVEUR] Impossible de créer le serveur.");
            }
        }
    }

    /**
     * Attendre une connexion d'un client et renvoyer un objet Reseau
     * configuré pour ce client.
     * @return Un objet Reseau représentant la connexion au client, ou null en cas d'erreur.
     */
    public Reseau attendreConnexionClient() {
        System.out.println("[SERVEUR] Attente d'une connexion client...");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                clientSocket = serverSocket.accept();
            } else {
                return null;
            }

            Reseau clientReseau = new Reseau();
            clientReseau.clientSocket = clientSocket;
            clientReseau.fluxSortie = new PrintWriter(clientSocket.getOutputStream(), true);
            clientReseau.fluxEntree = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String clientIP = clientSocket.getInetAddress().getHostAddress();
            String clientNomMachine = clientSocket.getInetAddress().getHostName();
            System.out.println("[SERVEUR] Client connecté : " + clientNomMachine + " (" + clientIP + ")");

            return clientReseau;
        } catch (SocketException e) {
            return null;
        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur lors de l'attente d'un client : " + e.getMessage());
            return null;
        }
    }

    /**
     * Reçoit les données envoyées par le client.
     *
     * @return La ligne de texte reçue du client, ou null en cas d'erreur de lecture.
     */
    public String recevoirDonnees() {
        System.out.println("[SERVEUR] Réception des données...");
        if (fluxEntree == null) {
            // Flux non initialisé ou fermé
            return null;
        }
        try {
            String donnees = fluxEntree.readLine();
            System.out.println("[SERVEUR] Données reçues : " + donnees);
            return donnees;
        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur lors de la réception des données : " + e.getMessage());
            return null;
        }
    }

    /**
     * Traite la requête reçue du client en séparant la clé et les données chiffrées.
     *
     * @param requete La requête reçue du client sous forme de chaîne de caractères.
     * @return Les données déchiffrées formatées.
     * @throws IllegalArgumentException Si la requête est null ou mal formatée.
     */
    public String traiterRequete(String requete) {
        System.out.println("[SERVEUR] Traitement de la requête...");
        if (requete == null) {
            throw new IllegalArgumentException("[SERVEUR] Requête null reçue.");
        }

        String[] parties = requete.split("/DELIM/");
        if (parties.length != 2) {
            throw new IllegalArgumentException("[SERVEUR] Format de la requête incorrect.");
        }

        String cle = parties[0];
        String donneesChiffrees = parties[1];
        String donneesDechiffrees = Vigenere.dechiffrementDonnees(donneesChiffrees, cle);
        System.out.println("[SERVEUR] Données déchiffrées avec succès.");
        return donneesDechiffrees;
    }

    /**
     * Envoie une réponse au client.
     *
     * @param reponse La réponse à envoyer au client sous forme de chaîne de caractères.
     */
    public void envoyerReponse(String reponse) {
        System.out.println("[SERVEUR] Envoi de la réponse.");
        fluxSortie.println(reponse);
    }

    /**
     * Ferme toutes les connexions et libère les ressources du serveur.
     * Cette méthode ferme les flux de communication et les sockets
     * du client et du serveur, s'ils sont ouverts,
     * pour garantir une libération propre des ressources.
     * En cas d'erreur, un message est affiché dans la console.
     */
    public void fermerServeur() {
        System.out.println("[SERVEUR] Fermeture du serveur...");
        try {
            if (fluxEntree != null) fluxEntree.close();
            if (fluxSortie != null) fluxSortie.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
            System.out.println("[SERVEUR] Toutes les connexions ont été fermées.");
        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur lors de la fermeture : " + e.getMessage());
        }
    }

    // --------- PARTIE CLIENT -----------

    /**
     * Préparation du client en établissant la connexion au serveur.
     * Cette méthode tente de créer une socket pour se connecter à un serveur
     * spécifié par son adresse IP et son port.
     * Si la connexion échoue, une exception {@link MauvaiseConnexionServeur}
     * est levée avec un message d'erreur approprié.
     *
     * @param adresse L'adresse du serveur (IP ou nom d'hôte).
     * @param port Le numéro de port du serveur.
     * @throws MauvaiseConnexionServeur Si la connexion échoue (serveur non disponible).
     */
    public void preparerClient(String adresse, int port) throws MauvaiseConnexionServeur {
        try {
            System.out.println("[CLIENT] Tentative de connexion au serveur : " + adresse + ":" + port);
            clientSocket = new Socket(adresse, port);
            fluxSortie = new PrintWriter(clientSocket.getOutputStream(), true);
            fluxEntree = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("[CLIENT] Connexion réussie au serveur " + adresse + " sur le port " + port);
        } catch (IOException e) {
            System.err.println("[CLIENT] Erreur lors de la connexion au serveur : " + e.getMessage());
            throw new MauvaiseConnexionServeur("Impossible de se connecter à " + adresse + ":" + port);
        }
    }

    /**
     * Envoie une chaîne de données au serveur après chiffrement.
     * Ajoute des informations de débogage pour suivre le processus.
     *
     * @param donnees Les données à envoyer.
     * @throws IllegalArgumentException Si les données sont nulles ou invalides.
     */
    public void envoyer(String donnees) {
        if (donnees == null || donnees.isEmpty()) {
            throw new IllegalArgumentException("[CLIENT] Les données à envoyer sont nulles ou vides.");
        }

        String contenu = donnees.replace("\n", "/N").replace("\r", "/R");
        String cle = Vigenere.genererCleAleatoire(contenu);
        System.out.println("[CLIENT] Clé générée : " + cle);

        String donneesChiffrees = Vigenere.chiffrementDonnees(contenu, cle);
        System.out.println("[CLIENT] Données chiffrées : " + donneesChiffrees);

        String message = cle + "/DELIM/" + donneesChiffrees;

        fluxSortie.println(message);
        System.out.println("[CLIENT] Message envoyé au serveur.");
    }

    /**
     * Reçoit la réponse du serveur.
     *
     * @return La réponse du serveur sous forme de chaîne, ou une erreur si elle est null.
     */
    public String recevoirReponse() {
        try {
            System.out.println("[CLIENT] Lecture de la réponse du serveur...");
            String reponse = fluxEntree.readLine();
            if (reponse == null) {
                System.out.println("[CLIENT] La réponse reçue est null.");
                return null;
            } else {
                System.out.println("[CLIENT] Réponse reçue : " + reponse);
                return reponse;
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Erreur lors de la réception de la réponse : " + e.getMessage());
            return "[CLIENT] Erreur lors de la réception de la réponse.";
        }
    }

    /**
     * Utilise la réponse du serveur, avec suivi des étapes pour le débogage.
     * @param reponse La réponse à traiter.
     */
    public void traiterReponse(String reponse) {
        // TODO : traiter la réponse du serveur
    }

    /**
     * Ferme les ressources client (sockets et flux).
     * Ajoute des messages de débogage à chaque étape.
     */
    public void fermerClient() {
        try {
            if (fluxEntree != null) {
                fluxEntree.close();
                System.out.println("[CLIENT] Flux d'entrée fermé.");
            }
            if (fluxSortie != null) {
                fluxSortie.close();
                System.out.println("[CLIENT] Flux de sortie fermé.");
            }
            if (clientSocket != null) {
                clientSocket.close();
                System.out.println("[CLIENT] Socket client fermé.");
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Erreur lors de la fermeture des ressources client : " + e.getMessage());
        }
    }

    /**
     * Renvoie l'adresse IP de la machine locale utilisée pour se connecter
     * à un serveur externe et son nom de machine.
     * @return l'adresse IP de la machine sous forme d'objet InetAddress.
     */
    public static InetAddress renvoyerIP() {
        // 8.8.8.8 correspond au DNS de google
        try (Socket socket = new Socket("8.8.8.8", 53)) {
            InetAddress ipLocale = socket.getLocalAddress();
            System.out.println("[CLIENT] IP locale : " + ipLocale.getHostAddress());
            System.out.println("[CLIENT] Nom de la machine : " + ipLocale.getHostName());
            return ipLocale;
        } catch (IOException e) {
            System.err.println("[CLIENT] Erreur lors de la récupération de l'IP : " + e.getMessage());
            return null; // retourne null pour éviter les erreurs
        }
    }

    /**
     * Renvoie le flux d'entrée utilisé pour recevoir des données.
     * Ce flux peut être utilisé pour lire directement les messages entrants.
     *
     * @return Le flux d'entrée (BufferedReader).
     */
    public BufferedReader getFluxEntree() {
        return fluxEntree;
    }

}