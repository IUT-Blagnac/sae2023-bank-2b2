package app;

import app.control.Batch;

public class BatchApp {

    /**
     * Méthode main() de l'application. Qui permet de lancer le batch.
     * @param args
     */
    public static void main( String[] args ) {
		Batch batch = new Batch();
        batch.start();
    }
}
