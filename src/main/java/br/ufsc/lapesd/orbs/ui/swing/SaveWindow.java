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

public class SaveWindow {

	private JFrame frmSaving;
	private JTextField saveLocation;
	private JTextField saveFormat;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SaveWindow window = new SaveWindow();
					window.frmSaving.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SaveWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmSaving = new JFrame();
		frmSaving.setTitle("Saving...");
		frmSaving.setBounds(100, 100, 278, 196);
		frmSaving.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSaving.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
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
		
		JLabel lblSaveLocation = new JLabel("Save Location:");
		frmSaving.getContentPane().add(lblSaveLocation, "2, 4, right, default");
		
		saveLocation = new JTextField();
		frmSaving.getContentPane().add(saveLocation, "4, 4, 3, 1, fill, default");
		saveLocation.setColumns(10);
		
		JLabel lblSaveFormat = new JLabel("Save Format:");
		frmSaving.getContentPane().add(lblSaveFormat, "2, 6, right, default");
		
		saveFormat = new JTextField();
		frmSaving.getContentPane().add(saveFormat, "4, 6, 3, 1, fill, default");
		saveFormat.setColumns(10);
		
		JButton okButton = new JButton("OK");
		frmSaving.getContentPane().add(okButton, "4, 8");
		
		JButton cancelButton = new JButton("Cancel");
		frmSaving.getContentPane().add(cancelButton, "6, 8");
	}

}
