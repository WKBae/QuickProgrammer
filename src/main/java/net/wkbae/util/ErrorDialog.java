package net.wkbae.util;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JTextArea;

import org.slf4j.LoggerFactory;

/**
 * 에러 상태를 표시하는 창입니다.<br>
 * 프로그램에서 발생되는 모든 예외({@link Throwable})들은 최종적으로 이 창을 통해 나타납니다.
 * @author WKBae
 */
public final class ErrorDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 6168726537960345387L;

	private final Icon warningIcon;
	
	private JScrollPane detailPane;
	
	public ErrorDialog(Throwable e) {
		this(null, e);
	}
	
	/**
	 * 에러 창을 생성합니다.<br>
	 * 생성자를 호출하면 자동으로 창을 표시({@link #setVisible(boolean) setVisible(true)})합니다.
	 * @param e
	 */
	public ErrorDialog(Window parent, Throwable e) {
		super(parent, ModalityType.APPLICATION_MODAL);
		
		LoggerFactory.getLogger(parent.getClass()).warn("An error occured and displayed:", e);
		
		setResizable(false);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			LoggerFactory.getLogger(parent.getClass()).warn("Error dialog cannot set Look 'n Feel:", ex);
		}
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{20, 0, 10, 0, 0, 10, 0, 20};
		gridBagLayout.rowHeights = new int[]{20, 0, 0, 0, 0, 20};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		getContentPane().setLayout(gridBagLayout);
		
		warningIcon = UIManager.getIcon("OptionPane.errorIcon");
		
		JLabel icon = new JLabel(warningIcon);
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 1;
		getContentPane().add(icon, gbc_panel);
		
		JLabel titleLabel = new JLabel("오류가 발생했습니다.");
		GridBagConstraints gbc_titleLabel = new GridBagConstraints();
		gbc_titleLabel.insets = new Insets(0, 0, 5, 5);
		gbc_titleLabel.gridx = 3;
		gbc_titleLabel.gridy = 1;
		gbc_titleLabel.gridwidth = 2;
		getContentPane().add(titleLabel, gbc_titleLabel);
		
		JLabel messageLabel = new JLabel(e.getLocalizedMessage());
		GridBagConstraints gbc_messageLabel = new GridBagConstraints();
		gbc_messageLabel.insets = new Insets(0, 0, 5, 5);
		gbc_messageLabel.gridx = 3;
		gbc_messageLabel.gridy = 2;
		getContentPane().add(messageLabel, gbc_messageLabel);
		
		JButton okButton = new JButton("확인");
		okButton.setActionCommand("Ok");
		okButton.addActionListener(this);
		GridBagConstraints gbc_okButton = new GridBagConstraints();
		gbc_okButton.insets = new Insets(0, 0, 5, 5);
		gbc_okButton.gridx = 6;
		gbc_okButton.gridy = 1;
		getContentPane().add(okButton, gbc_okButton);
		
		JButton detailButton = new JButton("자세히...");
		detailButton.setActionCommand("Detail");
		detailButton.addActionListener(this);
		GridBagConstraints gbc_detailButton = new GridBagConstraints();
		gbc_detailButton.insets = new Insets(0, 0, 5, 5);
		gbc_detailButton.gridx = 3;
		gbc_detailButton.gridy = 3;
		getContentPane().add(detailButton, gbc_detailButton);
		
		JTextArea errorDetail = new JTextArea();
		errorDetail.setFont(errorDetail.getFont().deriveFont(11f));
		detailPane = new JScrollPane(errorDetail);
		
		errorDetail.setEditable(false);
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		errorDetail.setText(sw.toString());
		pw.close();
		//sw.close();
		
		GridBagConstraints gbc_detailPane = new GridBagConstraints();
		gbc_detailPane.insets = new Insets(0, 0, 5, 5);
		gbc_detailPane.fill = GridBagConstraints.BOTH;
		gbc_detailPane.gridx = 3;
		gbc_detailPane.gridy = 4;
		gbc_detailPane.gridwidth = 3;
		getContentPane().add(detailPane, gbc_detailPane);
		
		pack();
		
		foldedSize = new Dimension(icon.getWidth() + Math.max(titleLabel.getWidth(), messageLabel.getWidth()) + okButton.getWidth() + 60, 150);
		expandedSize = new Dimension(foldedSize.width + 375, foldedSize.height + 405);//errorDetail.getHeight());
		
		detailPane.setVisible(false);
		
		Dimension paneSize = new Dimension(500, 400);
		detailPane.setMinimumSize(paneSize);
		detailPane.setMaximumSize(paneSize);
		detailPane.setPreferredSize(paneSize);
		detailPane.setSize(paneSize);
		
		setExpanded(false);
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setVisible(true);
	}
	private final Dimension expandedSize;
	
	private final Dimension foldedSize;
	
	/**
	 * "자세히" 버튼을 눌렀을 때 확장되는 기능입니다.
	 * @param expand <code>true</code>라면 확장됩니다.
	 */
	public void setExpanded(boolean expand){
		if(expand){
			setMinimumSize(expandedSize);
			setPreferredSize(expandedSize);
			setMaximumSize(expandedSize);
			setSize(expandedSize);
		}else{
			setMinimumSize(foldedSize);
			setPreferredSize(foldedSize);
			setMaximumSize(foldedSize);
			setSize(foldedSize);
		}
	}
	
	private boolean detailShown = false;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equals("Ok")){
			this.setVisible(false);
			this.dispose();
		}else if(command.equals("Detail")){
			detailShown = !detailShown;
			detailPane.setVisible(detailShown);
			setExpanded(detailShown);
		}
	}
}
