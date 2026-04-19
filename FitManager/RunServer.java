package com.fitmanager;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Point d’entrée pour lancer l’appli depuis l’IDE (bouton Run) : une appli servlet n’a pas de {@code main} par défaut.
 * <p>
 * Avant la première exécution : {@code mvnw.cmd package} (génère {@code target/FitManager.war}).
 */
public final class RunServer {

    private RunServer() {
    }

    public static void main(String[] args) throws Exception {
        File war = new File("target/FitManager.war");
        if (!war.isFile()) {
            System.err.println("Fichier introuvable : " + war.getAbsolutePath());
            System.err.println("Exécutez d'abord depuis la racine du projet : mvnw.cmd package");
            System.exit(1);
        }

        Server server = new Server(8080);
        WebAppContext ctx = new WebAppContext();
        ctx.setContextPath("/FitManager");
        ctx.setWar(war.getAbsolutePath());
        server.setHandler(ctx);
        server.start();

        String url = "http://localhost:8080/FitManager/";
        System.out.println();
        System.out.println("========== FitManager demarre ==========");
        System.out.println("Ouvrez ce lien dans votre navigateur :");
        System.out.println("  " + url);
        System.out.println("(Le programme reste actif tant que le serveur tourne — c'est normal.)");
        System.out.println("Pour arreter : bouton Stop dans l'IDE ou fermer la console.");
        System.out.println("========================================");
        System.out.println();

        openBrowser(url);

        server.join();
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }
        } catch (Exception ignored) {
            // ignore
        }

        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            try {
                new ProcessBuilder("cmd", "/c", "start", "", url).start();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }
}
