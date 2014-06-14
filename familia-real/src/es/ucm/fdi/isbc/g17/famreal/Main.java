package es.ucm.fdi.isbc.g17.famreal;

import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import es.ucm.fdi.gaia.ontobridge.OntoBridge;
import es.ucm.fdi.gaia.ontobridge.OntologyDocument;

public final class Main {

    private static JFrame frame;
    private static ProgressMonitor progress;

    public static void main (String[] args) throws InterruptedException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                preLoadOntology();
                new SwingWorker<OntoBridge, Void>() {
                    @Override
                    protected OntoBridge doInBackground () throws Exception {
                        return loadOntology();
                    }

                    protected void done () {
                        try {
                            postLoadOntology(get());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }.execute();
            }
        });
    }

    private static void preLoadOntology () {
        frame = new JFrame();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        progress = new ProgressMonitor(frame, "Cargando Ontología...", "", 0, 100);
        progress.setMillisToDecideToPopup(0);
        progress.setMillisToPopup(0);
        progress.setProgress(0);
    }

    protected static OntoBridge loadOntology () {
        OntoBridge ob = new OntoBridge();
        ob.initWithPelletReasoner();

        progress.setProgress(10);
        OntologyDocument mainOnto = new OntologyDocument(Main.class.getResource("/familia-real.owl").toString(), null);

        progress.setProgress(20);
        ob.loadOntology(mainOnto, Collections.<OntologyDocument> emptyList(), false);

        progress.setProgress(90);
        return ob;
    }

    protected static void postLoadOntology (OntoBridge ob) {
        progress.setProgress(100);
        progress.close();
        frame.dispose();
    }

}
