package es.ucm.fdi.isbc.g17.famreal;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;

import es.ucm.fdi.gaia.ontobridge.OntoBridge;

public final class FamiliaRealFrame extends JFrame {

    /* OntoBridge instance for recovering and tagging */
    private OntoBridge ontoBridge;

    /* Stored components */
    private JComboBox comboQueries;

    public FamiliaRealFrame () {
        setTitle("ISBC Grupo 17 - Práctica 5");

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);

        JPanel panel = new JPanel();
        panel.add(progress);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        setContentPane(panel);
    }

    public void setOntoBridge (OntoBridge ontoBridge) {
        this.ontoBridge = ontoBridge;

        setupInterface();
    }

    private void setupInterface () {
        JPanel panel = new JPanel(new BorderLayout());

        /* Setup the panel used for tagging */
        JPanel panelTagging = setupTaggingPanel();

        /* Setup the panel used for searching */
        JPanel panelRetrieval = setupRetrievalPanel();

        /* Setup the tabbed layout */
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Marcado", panelTagging);
        tabbedPane.addTab("Búsqueda", panelRetrieval);

        panel.add(tabbedPane, BorderLayout.CENTER);

        /* Set the new panel as content pane */
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel setupTaggingPanel () {
        return new JPanel();
    }

    private JPanel setupRetrievalPanel () {
        /* Get the names of the available searches and populate a dropdown */
        List<String> queries = obtainQueryNames();
        comboQueries = new JComboBox(queries.toArray());

        /* Create the search button */
        JButton buttonSearch = new JButton("Buscar");
        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                performSearch();
            }
        });

        /* Add the two elements */
        JPanel row = new JPanel(new FlowLayout());
        row.add(comboQueries);
        row.add(buttonSearch);

        /* Fill the panel */
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(row, BorderLayout.PAGE_START);
        return panel;
    }

    private List<String> obtainQueryNames () {
        List<String> queries = new ArrayList<String>();

        for (Iterator<String> it = ontoBridge.listSubClasses("Foto", false); it.hasNext();) {
            String name = it.next();
            String query = name.substring(name.lastIndexOf("#") + 1);
            if (!("Nothing").equals(query)) {
                queries.add(query);
            }
        }

        Collections.sort(queries);
        return queries;
    }

    /* package */void performSearch () {
        String query = comboQueries.getSelectedItem().toString();

        for (Iterator<String> it = ontoBridge.listInstances(query); it.hasNext();) {
            System.out.println(it.next());
        }
    }
}
