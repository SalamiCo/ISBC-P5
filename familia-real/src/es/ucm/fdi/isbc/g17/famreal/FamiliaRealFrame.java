package es.ucm.fdi.isbc.g17.famreal;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import es.ucm.fdi.gaia.ontobridge.OntoBridge;

/**
 * A GUI to search and tag the ontology for the Spanish Royal Family.
 * 
 * @author Daniel Escoz Solana
 * @author Pedro Morgado Alarcón
 * @author Arturo Pareja García
 */
public final class FamiliaRealFrame extends JFrame {

    /* OntoBridge instance for recovering and tagging */
    private OntoBridge ontoBridge;

    /* Stored components */
    private JComboBox comboQueries;
    private JTree treeOntology;

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

        /* Setup the ontology tree */
        treeOntology = setupOntologyTree();

        panel.add(tabbedPane, BorderLayout.CENTER);
        panel.add(new JScrollPane(treeOntology), BorderLayout.LINE_START);

        /* Set the new panel as content pane */
        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private JTree setupOntologyTree () {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Thing");

        final JTree tree = new JTree(root);
        tree.setCellRenderer(new OntologyCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        // tree.addTreeSelectionListener(this);

        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed (MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    // selectedConcept = selPath.toString();
                }
            }
        });

        tree.getModel().getRoot();

        for (Iterator<String> rc = ontoBridge.listRootClasses(); rc.hasNext();) {
            DefaultMutableTreeNode node = createNode(rc.next(), ontoBridge, 0);
            root.add(node);
        }
        tree.expandRow(0);

        return tree;
    }

    private DefaultMutableTreeNode createNode (String nodeName, OntoBridge ob, int depth) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(ob.getShortName(nodeName));
        // if(depth > maxdepth)
        // return node;

        Iterator<String> subClasses = ob.listSubClasses(nodeName, true);
        while (subClasses.hasNext()) {
            String subClassName = ob.getShortName(subClasses.next());
            if (!subClassName.equals("owl:Nothing"))
                node.add(createNode(subClassName, ob, depth + 1));
        }

        return node;
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
            // Special case, class "Nothing", which is not ours
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

    /**
     * Cell renderer for the ontology tree.
     * 
     * @author Daniel Escoz Solana
     * @author Pedro Morgado Alarcón
     * @author Arturo Pareja García
     */
    static final class OntologyCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 2836988791074735515L;

        private static final Icon ICON_CONCEPT = new ImageIcon(
                OntologyCellRenderer.class.getResource("/icons/class-orange.gif"));

        public OntologyCellRenderer () {
        }

        public Component getTreeCellRendererComponent (JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus)
        {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            setIcon(ICON_CONCEPT);

            return this;
        }
    }
}
