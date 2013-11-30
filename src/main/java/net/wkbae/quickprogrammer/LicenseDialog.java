package net.wkbae.quickprogrammer;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * 프로그램의 라이센스 정보를 표시하는 창입니다.
 * @author WKBae
 */
public class LicenseDialog extends JDialog implements ListSelectionListener {
	private static final long serialVersionUID = 757526753667654847L;
	
	private JTextArea licenseArea;
	public LicenseDialog() {
		super();
		this.setTitle("라이센스 정보");
		
		Vector<String> data = new Vector<>();
		data.add("Quick Programmer");
		data.add("json-simple");
		
		JList<String> list = new JList<String>(data);
		list.addListSelectionListener(this);
		getContentPane().add(list, BorderLayout.WEST);
		
		licenseArea = new JTextArea();
		licenseArea.setLineWrap(true);
		getContentPane().add(new JScrollPane(licenseArea), BorderLayout.CENTER);
		
		this.setMinimumSize(new Dimension(700, 600));
		this.setVisible(true);
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		//System.out.println(e);
		if(!e.getValueIsAdjusting()) {
			InputStream is = this.getClass().getResourceAsStream("/license/" + ((JList<String>)e.getSource()).getSelectedValue() + ".txt");
		//System.out.println(is);
			if(is != null) {
				Scanner s = new Scanner(is).useDelimiter("\\A");
				licenseArea.setText(s.hasNext()? s.next() : "");
				licenseArea.setCaretPosition(0);
				s.close();
				try {
					is.close();
				} catch (IOException e1) {}
			}
		}
	}
}
