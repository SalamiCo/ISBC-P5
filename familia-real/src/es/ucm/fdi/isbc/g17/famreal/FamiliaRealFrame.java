package es.ucm.fdi.isbc.g17.famreal;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    private static final long serialVersionUID = 2778873259694986961L;

    /* OntoBridge instance for recovering and tagging */
    private OntoBridge ontoBridge;
    private final Map<String, Icon> photoIcons = new HashMap<String, Icon>();

    /* Stored components */
    private JComboBox<?> comboQueries;
    private JTree treeOntology;

    private JLabel labelPhotoSearch;
    private JSlider sliderPhotoSearch;

    private JLabel labelPhotoTag;
    private JSlider sliderPhotoTag;

    /* List of photos to show in search */
    private final List<String> photoNamesSearch = new ArrayList<String>();
    private int photoCurrentSearch = 0;

    /* List of photos to show */
    private final List<String> photoNamesTag = new ArrayList<String>();
    private int photoCurrentTag = 0;

    private JList<Object> listTagged;

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
        updateInterface();
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
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
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

        System.out.println("Generating Class Tree...");

        tree.getModel().getRoot();
        for (Iterator<String> rc = ontoBridge.listRootClasses(); rc.hasNext();) {
            DefaultMutableTreeNode node = createNode(rc.next(), ontoBridge, 0);
            root.add(node);
        }
        tree.expandRow(0);

        System.out.println();
        return tree;
    }

    private DefaultMutableTreeNode createNode (String nodeName, OntoBridge ob, int depth) {
        for (int i = 0; i < depth; i++) {
            System.out.print("  ");
        }
        System.out.println("> " + extractName(nodeName));

        DefaultMutableTreeNode node = new DefaultMutableTreeNode(extractName(nodeName));
        // if(depth > maxdepth)
        // return node;

        for (Iterator<String> subClasses = ob.listSubClasses(nodeName, true); subClasses.hasNext();) {
            String subClassName = ob.getShortName(subClasses.next());
            if (!subClassName.equals("owl:Nothing")) {
                node.add(createNode(subClassName, ob, depth + 1));
            }
        }

        return node;
    }

    private JPanel setupTaggingPanel () {
        for (Iterator<String> it = ontoBridge.listInstances("Foto"); it.hasNext();) {
            photoNamesTag.add(it.next());
        }
        Collections.sort(photoNamesTag);

        /* Tagged list */
        listTagged = new JList<>();

        /* Combo box for people */
        List<String> people = new ArrayList<>();
        for (Iterator<String> it = ontoBridge.listInstances("Persona"); it.hasNext();) {
            people.add(extractName(it.next()));
        }
        Collections.sort(people);
        final JComboBox<String> comboPeople = new JComboBox<String>(people.toArray(new String[people.size()]));

        /* Tag button */
        JButton buttonTag = new JButton("Marcar");
        buttonTag.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent arg0) {
                String photo = photoNamesTag.get(photoCurrentTag);
                String person = comboPeople.getSelectedItem().toString();

                System.out.printf("$ %s aparece %s%n", photo, person);
                ontoBridge.createOntProperty(photo, "aparece", person);

                updateInterface();
            }
        });

        /* Setup the tagging panel */
        Box subcolumn = Box.createVerticalBox();
        subcolumn.add(comboPeople);
        subcolumn.add(buttonTag);

        JPanel column = new JPanel(new BorderLayout());
        column.add(listTagged, BorderLayout.CENTER);
        column.add(subcolumn, BorderLayout.PAGE_END);

        /* Create the photo label */
        labelPhotoTag = new JLabel();

        /* Create the photo slider */
        sliderPhotoTag = new JSlider();
        sliderPhotoTag.setValue(0);
        sliderPhotoTag.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent arg0) {
                photoCurrentTag = sliderPhotoTag.getValue() - 1;
                updateInterface();
            }
        });

        /* Fill the panel */
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(column, BorderLayout.LINE_END);
        panel.add(new JScrollPane(labelPhotoTag), BorderLayout.CENTER);
        panel.add(sliderPhotoTag, BorderLayout.PAGE_END);
        return panel;
    }

    private JPanel setupRetrievalPanel () {
        /* Get the names of the available searches and populate a dropdown */
        List<String> queries = obtainQueryNames();
        comboQueries = new JComboBox<String>(queries.toArray(new String[queries.size()]));

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

        /* Create the photo label */
        labelPhotoSearch = new JLabel();

        /* Create the photo slider */
        sliderPhotoSearch = new JSlider();
        sliderPhotoSearch.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent arg0) {
                photoCurrentSearch = sliderPhotoSearch.getValue() - 1;
                updateInterface();
            }
        });

        /* Fill the panel */
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(row, BorderLayout.PAGE_START);
        panel.add(new JScrollPane(labelPhotoSearch), BorderLayout.CENTER);
        panel.add(sliderPhotoSearch, BorderLayout.PAGE_END);
        return panel;
    }

    /* package */void updateInterface () {
        /* Slider for search */
        if (photoCurrentSearch >= photoNamesSearch.size()) {
            photoCurrentSearch = photoNamesSearch.size() - 1;
        }
        if (photoCurrentSearch < 0) {
            photoCurrentSearch = 0;
        }

        /* Slider for tagging */
        if (photoCurrentTag >= photoNamesTag.size()) {
            photoCurrentTag = photoNamesTag.size() - 1;
        }
        if (photoCurrentTag < 0) {
            photoCurrentTag = 0;
        }

        /* Configure tag slider */
        String photoNameTag = photoNamesTag.get(photoCurrentTag);
        labelPhotoTag.setIcon(loadPhotoIcon(photoNameTag));
        sliderPhotoTag.setMinimum(1);
        sliderPhotoTag.setMaximum(photoNamesTag.size());
        sliderPhotoTag.setValue(photoCurrentTag + 1);
        sliderPhotoTag.setEnabled(true);

        /* Configure search slider */
        if (photoNamesSearch.isEmpty()) {
            labelPhotoSearch.setIcon(null);

        } else {
            String photoNameSearch = photoNamesSearch.get(photoCurrentSearch);
            labelPhotoSearch.setIcon(loadPhotoIcon(photoNameSearch));
        }

        if (photoNamesSearch.size() > 1) {
            sliderPhotoSearch.setMinimum(1);
            sliderPhotoSearch.setMaximum(photoNamesSearch.size());
            sliderPhotoSearch.setValue(photoCurrentSearch + 1);
            sliderPhotoSearch.setEnabled(true);

        } else {
            sliderPhotoSearch.setMinimum(0);
            sliderPhotoSearch.setMaximum(0);
            sliderPhotoSearch.setValue(0);
            sliderPhotoSearch.setEnabled(false);
        }

        /* Load current people */
        DefaultListModel<Object> photoPeople = new DefaultListModel<Object>();
        String photo = photoNamesTag.get(photoCurrentTag);
        for (Iterator<String> it = ontoBridge.listPropertyValue(photo, "aparece"); it.hasNext();) {
            String person = extractName(it.next());
            photoPeople.addElement(person);
        }

        listTagged.setModel(photoPeople);
    }

    private Icon loadPhotoIcon (String photoName) {
        if (photoIcons.containsKey(photoName)) {
            return photoIcons.get(photoName);
        }

        try {
            String photoPath = "/photos/" + extractName(findPhotoName(photoName));
            URL photoUrl = FamiliaRealFrame.class.getResource(photoPath);

            System.out.printf("Photo %s -> %s -> %s%n", photoName, photoPath, photoUrl);

            if (photoUrl == null) {
                return new ImageIcon();
            } else {
                Image photoImage = ImageIO.read(photoUrl);
                Icon photoIcon = new ImageIcon(photoImage);

                photoIcons.put(photoName, photoIcon);
                return photoIcon;
            }

        } catch (IOException exc) {
            exc.printStackTrace();
            return null;
        }
    }

    private List<String> obtainQueryNames () {
        List<String> queries = new ArrayList<String>();

        for (Iterator<String> it = ontoBridge.listSubClasses("Foto", false); it.hasNext();) {
            String query = extractName(it.next());
            // Special case, class "Nothing", which is not ours
            if (!("owl:Nothing").equals(query)) {
                queries.add(query);
            }
        }

        Collections.sort(queries);
        return queries;
    }

    /* package */void performSearch () {
        photoNamesSearch.clear();
        photoCurrentSearch = 0;

        String query = comboQueries.getSelectedItem().toString();
        System.out.printf("Finding photos for '%s'...%n", query);

        for (Iterator<String> it = ontoBridge.listInstances(query); it.hasNext();) {
            String instance = extractName(it.next());

            photoNamesSearch.add(instance);
            System.out.println(instance);
        }

        Collections.sort(photoNamesSearch);
        updateInterface();
    }

    private String findPhotoName (String instance) {
        System.out.printf("Finding photo name (URL) for '%s'...%n", instance);

        for (Iterator<String> it = ontoBridge.listPropertyValue(instance, "tieneURL"); it.hasNext();) {
            String photoName = it.next();
            System.out.printf("%s -> %s%n", instance, photoName);
            return photoName;
        }

        System.out.printf("%s -> NOT FOUND%n", instance);
        return "";
    }

    private String extractName (String name) {
        return ontoBridge.getShortName(name);// name.substring(name.lastIndexOf("#")
                                             // + 1);
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
