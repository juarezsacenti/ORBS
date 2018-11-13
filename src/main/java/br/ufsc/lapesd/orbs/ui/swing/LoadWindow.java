package br.ufsc.lapesd.orbs.ui.swing;

import java.awt.EventQueue;

import javax.swing.JFrame;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

public class LoadWindow {

	private JFrame frmLoading;
	private JTextField loadLocation;
	private JTextField loadFormat;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoadWindow window = new LoadWindow();
					window.frmLoading.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LoadWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmLoading = new JFrame();
		frmLoading.setTitle("Loading...");
		frmLoading.setBounds(100, 100, 278, 196);
		frmLoading.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLoading.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
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
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblLoadLocation = new JLabel("Load Location:");
		frmLoading.getContentPane().add(lblLoadLocation, "2, 4, right, default");
		
		loadLocation = new JTextField();
		frmLoading.getContentPane().add(loadLocation, "4, 4, 3, 1, fill, default");
		loadLocation.setColumns(10);
		
		JLabel lblLoadFormat = new JLabel("Load Format:");
		frmLoading.getContentPane().add(lblLoadFormat, "2, 6, right, default");
		
		loadFormat = new JTextField();
		frmLoading.getContentPane().add(loadFormat, "4, 6, 3, 1, fill, default");
		loadFormat.setColumns(10);
		
		JButton okButton = new JButton("OK");
		frmLoading.getContentPane().add(okButton, "4, 8");
		
		JButton cancelButton = new JButton("Cancel");
		frmLoading.getContentPane().add(cancelButton, "6, 8");
	}

}
