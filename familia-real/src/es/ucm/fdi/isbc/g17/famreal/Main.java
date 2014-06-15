package es.ucm.fdi.isbc.g17.famreal;

import java.util.Collections;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import es.ucm.fdi.gaia.ontobridge.OntoBridge;
import es.ucm.fdi.gaia.ontobridge.OntologyDocument;

public final class Main {

    private static FamiliaRealFrame frame;

    private static void setLookAndFeel (String... lafs) {
        for (String laf : lafs) {
            try {
                UIManager.setLookAndFeel(laf);
                return;

            } catch (Exception e) {
                // Next!
            }
        }
    }

    public static void main (String[] args) throws Exception {
        setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel", UIManager.getSystemLookAndFeelClassName());
        System.out.println(UIManager.getLookAndFeel());

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
        frame = new FamiliaRealFrame();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    protected static OntoBridge loadOntology () {
        OntoBridge ob = new OntoBridge();
        ob.initWithPelletReasoner();

        OntologyDocument mainOnto = new OntologyDocument(Main.class.getResource("/familia-real.owl").toString(), null);

        ob.loadOntology(mainOnto, Collections.<OntologyDocument> emptyList(), false);

        return ob;
    }

    protected static void postLoadOntology (OntoBridge ob) {

        frame.setOntoBridge(ob);
    }

}
