package es.ucm.fdi.isbc.g17.famreal;

import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import es.ucm.fdi.gaia.ontobridge.OntoBridge;
import es.ucm.fdi.gaia.ontobridge.OntologyDocument;

public final class Main {

    private static ProgressMonitor progress;

    public static void main (String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                preLoadOntology();
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground () throws Exception {
                        loadOntology();
                        return null;
                    }

                    protected void done () {
                        postLoadOntology();
                    }
                }.execute();
            }
        });
    }

    private static void preLoadOntology () {
        JFrame frame = new JFrame();
        
        
        progress = new ProgressMonitor(frame, "Cargando Ontología...", "", 0, 1);
        progress.setMillisToDecideToPopup(100);
        progress.setMillisToPopup(1000);
        progress.setProgress(0);
    }

    protected static void loadOntology () {
        OntoBridge ob = new OntoBridge();
        ob.initWithPelletReasoner();

        OntologyDocument mainOnto = new OntologyDocument(Main.class.getResource("/familia-real.owl").toString(), null);

        ob.loadOntology(mainOnto, Collections.<OntologyDocument> emptyList(), false);
    }

    protected static void postLoadOntology () {
        progress.setProgress(1);
        progress.close();
    }

}
