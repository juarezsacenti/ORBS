package br.ufsc.lapesd.orbs.ui.swing;

import java.awt.EventQueue;

import javax.swing.JFrame;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;

import br.ufsc.lapesd.orbs.tokit.EngineParameter;
import br.ufsc.lapesd.orbs.ui.control.SROSetupControl;

import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ConfigWindow {

	private static SROSetupControl control;
	
	private JFrame frmSroSetup;
	private JTextField dataSourceLocation;
	private JTextField dataSourceEnclosure;
	private JTextField dataSourceDelimiter;
	private JRadioButton rdbtnDataHeaderLine;
	private JTextField ontologyURI;
	private JComboBox<String> ontologyModelSpec;
	private JComboBox<String> ontologyTripleStore;
	private JTextField ontologyDirectory;
	private JTextField ontologyName;
	private JTextField contextOntologyURI;
	private JTextField contextOntologyLocation;
	private JComboBox<String> contextOntologyFormat;
	private JTextField annotationSourceLocation;
	private JTextField annotationSourceEnclosure;
	private JTextField annotationSourceDelimiter;
	private JRadioButton rdbtnAnnotationHeaderLine;

	private LoadWindow loadWindow;

	private SaveWindow saveWindow;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					control = new SROSetupControl();
					ConfigWindow window = new ConfigWindow();
					window.frmSroSetup.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ConfigWindow() {
		this.loadWindow = new LoadWindow();
		this.saveWindow = new SaveWindow();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmSroSetup = new JFrame();
		frmSroSetup.setTitle("SRO Setup");
		frmSroSetup.setBounds(100, 100, 613, 545);
		frmSroSetup.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(12, 59, 569, 279);
		
		JButton btnNewButton = new JButton("Start training...");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onStartTraining();
			}
		});
		btnNewButton.setBounds(288, 351, 119, 25);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		frmSroSetup.getContentPane().setLayout(null);
		
		JLabel lblSroSetttings = new JLabel("SRO Setttings");
		lblSroSetttings.setBounds(12, 26, 569, 20);
		lblSroSetttings.setForeground(Color.DARK_GRAY);
		lblSroSetttings.setFont(new Font("Tahoma", Font.BOLD, 16));
		frmSroSetup.getContentPane().add(lblSroSetttings);
		frmSroSetup.getContentPane().add(btnNewButton);
		frmSroSetup.getContentPane().add(tabbedPane);
		
		JScrollPane datasourcePane = new JScrollPane();
		datasourcePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		datasourcePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Data source", null, datasourcePane, null);
		
		JPanel panel = new JPanel();
		datasourcePane.setViewportView(panel);
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblLocation = new JLabel("Location:");
		panel.add(lblLocation, "2, 2, right, default");
		
		dataSourceLocation = new JTextField();
		panel.add(dataSourceLocation, "4, 2, fill, default");
		dataSourceLocation.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Enclosure:");
		panel.add(lblNewLabel, "2, 4, right, default");
		
		dataSourceEnclosure = new JTextField();
		panel.add(dataSourceEnclosure, "4, 4, fill, default");
		dataSourceEnclosure.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Delimiter:");
		panel.add(lblNewLabel_1, "2, 6, right, default");
		
		dataSourceDelimiter = new JTextField();
		panel.add(dataSourceDelimiter, "4, 6, fill, default");
		dataSourceDelimiter.setColumns(10);
		
		rdbtnDataHeaderLine = new JRadioButton("Header line");
		panel.add(rdbtnDataHeaderLine, "4, 8");
		
		JScrollPane contextOntologyPane = new JScrollPane();
		contextOntologyPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		contextOntologyPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Context ontology", null, contextOntologyPane, null);
		
		JPanel panel_1 = new JPanel();
		contextOntologyPane.setViewportView(panel_1);
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblOntologyUri = new JLabel("Ontology URI:");
		panel_1.add(lblOntologyUri, "2, 2, right, default");
		
		ontologyURI = new JTextField();
		panel_1.add(ontologyURI, "4, 2, fill, default");
		ontologyURI.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Model spec:");
		panel_1.add(lblNewLabel_2, "2, 4, right, default");
		
		ontologyModelSpec = new JComboBox<String>();
		ontologyModelSpec.setModel(new DefaultComboBoxModel<String>(new String[] {"OWL_DL_MEM"}));
		panel_1.add(ontologyModelSpec, "4, 4, fill, default");
		
		JLabel lblTripleStore = new JLabel("Triple store:");
		panel_1.add(lblTripleStore, "2, 6, right, default");
		
		ontologyTripleStore = new JComboBox<String>();
		ontologyTripleStore.setModel(new DefaultComboBoxModel<String>(new String[] {"Text"}));
		panel_1.add(ontologyTripleStore, "4, 6, fill, default");
		
		JLabel lblLocation_1 = new JLabel("Directory:");
		panel_1.add(lblLocation_1, "2, 8, right, default");
		
		ontologyDirectory = new JTextField();
		panel_1.add(ontologyDirectory, "4, 8, fill, default");
		ontologyDirectory.setColumns(10);
		
		JLabel lblName = new JLabel("Name:");
		panel_1.add(lblName, "2, 10, right, default");
		
		ontologyName = new JTextField();
		panel_1.add(ontologyName, "4, 10, fill, default");
		ontologyName.setColumns(10);
		
		JLabel lblContextOntologyUri = new JLabel("Context ontology URI:");
		panel_1.add(lblContextOntologyUri, "2, 12, right, default");
		
		contextOntologyURI = new JTextField();
		panel_1.add(contextOntologyURI, "4, 12, fill, default");
		contextOntologyURI.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Context ontology Location:");
		panel_1.add(lblNewLabel_3, "2, 14, right, default");
		
		contextOntologyLocation = new JTextField();
		panel_1.add(contextOntologyLocation, "4, 14, fill, default");
		contextOntologyLocation.setColumns(10);
		
		JLabel lblContextOntologyFormat = new JLabel("Context ontology Format:");
		panel_1.add(lblContextOntologyFormat, "2, 16, right, default");
		
		contextOntologyFormat = new JComboBox<String>();
		contextOntologyFormat.setModel(new DefaultComboBoxModel<String>(new String[] {"RDF/XML"}));
		panel_1.add(contextOntologyFormat, "4, 16, fill, default");
		
		JScrollPane annotationSourcePane = new JScrollPane();
		annotationSourcePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		annotationSourcePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tabbedPane.addTab("Annotation source", null, annotationSourcePane, null);
		
		JPanel panel_2 = new JPanel();
		annotationSourcePane.setViewportView(panel_2);
		panel_2.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblLocation_2 = new JLabel("Location:");
		panel_2.add(lblLocation_2, "2, 2, right, default");
		
		annotationSourceLocation = new JTextField();
		panel_2.add(annotationSourceLocation, "4, 2, fill, top");
		annotationSourceLocation.setColumns(10);
		
		JLabel lblEnclosure = new JLabel("Enclosure:");
		panel_2.add(lblEnclosure, "2, 4, right, default");
		
		annotationSourceEnclosure = new JTextField();
		panel_2.add(annotationSourceEnclosure, "4, 4, fill, default");
		annotationSourceEnclosure.setColumns(10);
		
		JLabel lblDelimiter = new JLabel("Delimiter:");
		panel_2.add(lblDelimiter, "2, 6, right, default");
		
		annotationSourceDelimiter = new JTextField();
		panel_2.add(annotationSourceDelimiter, "4, 6, fill, default");
		annotationSourceDelimiter.setColumns(10);
		
		rdbtnAnnotationHeaderLine = new JRadioButton("Header line");
		panel_2.add(rdbtnAnnotationHeaderLine, "4, 8");
		
		JScrollPane semanticExpansionerPane = new JScrollPane();
		tabbedPane.addTab("Semantic expansioner", null, semanticExpansionerPane, null);
		
		JButton btnSave = new JButton("Save");
		btnSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onSaveSROSettings();
			}
		});
		btnSave.setBounds(62, 351, 97, 25);
		frmSroSetup.getContentPane().add(btnSave);
		
		JButton btnLoad = new JButton("Load");
		btnLoad.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onLoadSROSettings();
			}
		});
		btnLoad.setBounds(179, 351, 97, 25);
		frmSroSetup.getContentPane().add(btnLoad);
	}

	private String[] getViewFields() {
		String[] viewFields = new String[16];
		viewFields[0] = dataSourceLocation.getText();
		viewFields[1] = dataSourceEnclosure.getText();
		viewFields[2] = dataSourceDelimiter.getText();
		viewFields[3] = rdbtnDataHeaderLine.isSelected() ? "true" : "false";
		
		viewFields[4] = ontologyURI.getText();
		viewFields[5] = (String) ontologyModelSpec.getSelectedItem();
		viewFields[6] = (String) ontologyTripleStore.getSelectedItem();
		viewFields[7] = ontologyDirectory.getText();
		viewFields[8] = ontologyName.getText();
		viewFields[9] = contextOntologyURI.getText();
		viewFields[10] = contextOntologyLocation.getText();
		viewFields[11] = (String) contextOntologyFormat.getSelectedItem();
		
		viewFields[12] = annotationSourceLocation.getText();
		viewFields[13] = annotationSourceEnclosure.getText();
		viewFields[14] = annotationSourceDelimiter.getText();
		viewFields[15] = rdbtnAnnotationHeaderLine.isSelected() ? "true" : "false";
		
		return viewFields;
	}
	
	private void onLoadSROSettings() {
		EngineParameter eparams = control.onLoadSROSettings();
		String[] viewFields = eparams.toViewFields();
		dataSourceLocation.setText(viewFields[0]);
		dataSourceEnclosure.setText(viewFields[1]);
		dataSourceDelimiter.setText(viewFields[2]);
		rdbtnDataHeaderLine.setSelected((viewFields[3].equals("true")));
		
		ontologyURI.setText(viewFields[4]);
		ontologyModelSpec.setSelectedItem(viewFields[5]);
		ontologyTripleStore.setSelectedItem(viewFields[6]);
		ontologyDirectory.setText(viewFields[7]);
		ontologyName.setText(viewFields[8]);
		contextOntologyURI.setText(viewFields[9]);
		contextOntologyLocation.setText(viewFields[10]);
		contextOntologyFormat.setSelectedItem(viewFields[11]);
		
		annotationSourceLocation.setText(viewFields[12]);
		annotationSourceEnclosure.setText(viewFields[13]);
		annotationSourceDelimiter.setText(viewFields[14]);
		rdbtnAnnotationHeaderLine.setSelected(viewFields[15].equals("true"));
	}

	private void onSaveSROSettings() {
		control.onSaveSROSettings(getViewFields());
	}

	private void onStartTraining() {
		control.onStartTraining(getViewFields());
	}
}
