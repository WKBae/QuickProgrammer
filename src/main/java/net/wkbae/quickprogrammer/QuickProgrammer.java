package net.wkbae.quickprogrammer;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import net.wkbae.quickprogrammer.JobManager.JobListener;
import net.wkbae.quickprogrammer.Logger.LogLevel;
import net.wkbae.quickprogrammer.file.PluginManager;
import net.wkbae.util.ErrorDialog;

/**
 * 퀵 프로그래머의 시작점이자 프로그램 창({@link ProgrammingFrame})들의 부모 프레임입니다.<br>
 * 모든 프로그램 창은 이 프레임 위에 놓여집니다.
 * @author WKBae
 */
public final class QuickProgrammer extends JFrame implements WindowListener, ActionListener {
	private static final long serialVersionUID = -6270228961560069887L;

	public static void main(String[] args) {
		Logger.init();
		
		if(GraphicsEnvironment.isHeadless()) { // 디스플레이, 키보드, 마우스가 없는 경우
			Logger.log(LogLevel.ERROR, "이 프로그램을 이용하기 위해서는 디스플레이와 키보드, 마우스가 필요합니다.");
			System.exit(-1);
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new QuickProgrammer(true);
			}
		});
	}

	private JMenuBar menuBar;
	private JDesktopPane desktopPane;
	
	private Stack<ProgrammingFrame> programStack = new Stack<>();
	 
	private BlockTreeFrame blockFrame;
	
	private JMenuItem menuUndo;
	private JMenuItem menuRedo;
	
	private JCheckBoxMenuItem menuBlockList;
	
	private static Integer openedWindows = 0;
	
	/**
	 * QuickProgrammer를 생성합니다.<br>
	 * @param firstLoad 최초로 생성된 프레임을 나타냅니다. 최초 실행 시 나타나는 프레임만 <code>true</code>값을 가져야 합니다.<br>
	 * <code>true</code>라면 초기화(initialization) 작업을 진행합니다.
	 */
	QuickProgrammer(boolean firstLoad) {
		super("Quick Programmer");
		
		synchronized(openedWindows) {
			openedWindows++;
		}
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		if(firstLoad) {
			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					new ErrorDialog(e);
				}
			});
			
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				new ErrorDialog(e);
			}
		}
		
		if(firstLoad) {
			Logger.log(LogLevel.VERBOSE, "Loading Plugins...");
			PluginManager.loadPlugins();
		}

		Dimension size = new Dimension(1000, 700);
		this.setPreferredSize(size);
		this.setSize(size);
		
		Logger.log(LogLevel.VERBOSE, "Creating menu bar...");
		initMenuBar();
		setProgramMenuEnabled(false);
		
		desktopPane = new JDesktopPane();
		
		Logger.log(LogLevel.VERBOSE, "Creating block list...");
		blockFrame = new BlockTreeFrame(this);
		blockFrame.addInternalFrameListener(new BasicFrameListener());
		desktopPane.add(blockFrame);
		blockFrame.setLocation(750, 0);
		
		this.setContentPane(desktopPane);

		this.addWindowListener(this);
		this.pack();
		this.setVisible(true);
	}
	
	public ProgrammingFrame getCurrentProgrammingFrame() {
		if(programStack.isEmpty()) {
			return null;
		}
		return programStack.peek();
	}
	
	public Program getCurrentProgram() {
		if(getCurrentProgrammingFrame() == null) {
			return null;
		}
		return getCurrentProgrammingFrame().getPanel().getProgram();
	}
	
	private int unnamedCount = 1;
	public void createProgrammingFrame() {
		createProgrammingFrame("제목 없음" + ((unnamedCount != 1)? " " + unnamedCount : ""));
		unnamedCount++;
	}
	
	public void createProgrammingFrame(String name) {
		ProgrammingFrame frame = new ProgrammingFrame(name);
		frame.addInternalFrameListener(new ProgrammingFrameListener());
		this.add(frame);
		frame.setVisible(true);
	}
	
	public void createProgrammingFrame(File file) {
		ProgrammingFrame frame;
		try {
			frame = new ProgrammingFrame(file);
		} catch (IOException e) {
			new ErrorDialog(e);
			return;
		}
		frame.addInternalFrameListener(new ProgrammingFrameListener());
		this.add(frame);
		frame.setVisible(true);
	}
	
	private ArrayList<JMenuItem> programMenus = new ArrayList<>();
	private void setProgramMenuEnabled(boolean enabled) {
		for(JMenuItem item : programMenus) {
			item.setEnabled(enabled);
		}
	}
	
	private void initMenuBar(){
		menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("파일(F)");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem menuNewProgram = new JMenuItem("새로 만들기(N)", KeyEvent.VK_N);
		menuNewProgram.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menuNewProgram.setActionCommand("New");
		menuNewProgram.addActionListener(this);
		fileMenu.add(menuNewProgram);
		
		JMenuItem menuOpen = new JMenuItem("열기(O)", KeyEvent.VK_O);
		menuOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		menuOpen.setActionCommand("Open");
		menuOpen.addActionListener(this);
		fileMenu.add(menuOpen);
		
		fileMenu.addSeparator();
		
		JMenuItem menuSave = new JMenuItem("저장(S)", KeyEvent.VK_S);
		menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		menuSave.setActionCommand("Save");
		menuSave.addActionListener(this);
		fileMenu.add(menuSave);
		programMenus.add(menuSave);
		
		JMenuItem menuSaveAs = new JMenuItem("다른 이름으로 저장");
		menuSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		menuSaveAs.setActionCommand("SaveAs");
		menuSaveAs.addActionListener(this);
		fileMenu.add(menuSaveAs);
		programMenus.add(menuSaveAs);
		
		fileMenu.addSeparator();
		
		JMenuItem menuExit = new JMenuItem("종료(X)", KeyEvent.VK_X);
		menuExit.setActionCommand("Exit");
		menuExit.addActionListener(this);
		fileMenu.add(menuExit);
		
		menuBar.add(fileMenu);
		
		JMenu taskMenu = new JMenu("작업(J)");
		taskMenu.setMnemonic(KeyEvent.VK_J);
		programMenus.add(taskMenu);
		
		JMenuItem menuRun = new JMenuItem("실행(R)", KeyEvent.VK_R);
		menuRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		menuRun.setActionCommand("Run");
		menuRun.addActionListener(this);
		taskMenu.add(menuRun);
		
		taskMenu.addSeparator();
		
		menuUndo = new JMenuItem("되돌리기(Z)", KeyEvent.VK_Z);
		menuUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		menuUndo.setActionCommand("Undo");
		menuUndo.addActionListener(this);
		taskMenu.add(menuUndo);
		
		menuRedo = new JMenuItem("되돌리기 취소(Y)", KeyEvent.VK_Y);
		menuRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		menuRedo.setActionCommand("Redo");
		menuRedo.addActionListener(this);
		taskMenu.add(menuRedo);
		
		menuBar.add(taskMenu);
		
		JMenu windowMenu = new JMenu("창(W)");
		windowMenu.setMnemonic(KeyEvent.VK_W);
		//programMenus.add(windowMenu);
		
		JMenuItem menuNewWindow = new JMenuItem("새 창(N)", KeyEvent.VK_N);
		menuNewWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		menuNewWindow.setActionCommand("NewWindow");
		menuNewWindow.addActionListener(this);
		windowMenu.add(menuNewWindow);
		
		windowMenu.addSeparator();
		
		menuBlockList = new JCheckBoxMenuItem("블록 목록(B)");
		menuBlockList.setMnemonic(KeyEvent.VK_B);
		menuBlockList.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		menuBlockList.setActionCommand("BlockList");
		menuBlockList.addActionListener(this);
		menuBlockList.setSelected(true);
		windowMenu.add(menuBlockList);
		
		menuBar.add(windowMenu);
		
		JMenu infoMenu = new JMenu("정보(H)");
		infoMenu.setMnemonic(KeyEvent.VK_H);
		
		JMenuItem menuProgramName = new JMenuItem("Quick Programmer(αlpha)");
		menuProgramName.setEnabled(false);
		infoMenu.add(menuProgramName);
		
		JMenuItem menuAuthorName = new JMenuItem(" by WKBae (http://wkbae.net/)");
		menuAuthorName.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("http://wkbae.net/"));
				} catch (IOException | URISyntaxException e1) {
				}
			}
		});
		infoMenu.add(menuAuthorName);
		
		JMenuItem menuSource = new JMenuItem("오픈소스 (https://github.com/WKBae/QuickProgrammer)");
		menuSource.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("https://github.com/WKBae/QuickProgrammer"));
				} catch (IOException | URISyntaxException e1) {
				}
			}
		});
		infoMenu.add(menuSource);
		
		JMenuItem menuLicense = new JMenuItem("라이센스 정보");
		menuLicense.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new LicenseDialog();
			}
		});
		infoMenu.add(menuLicense);
		
		menuBar.add(infoMenu);
		
		this.setJMenuBar(menuBar);
		//new ErrorDialog(new Exception("TestException"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("New")) {
			createProgrammingFrame();
		} else if(e.getActionCommand().equals("Open")) {
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileExtensionFilter("qps", "퀵 프로그래머 저장 형식 (*.qps)"));
			int ret = fc.showOpenDialog(this);
			if(ret == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if(!file.exists() || !file.isFile()){
					JOptionPane.showMessageDialog(this, "파일이 존재하지 않습니다.", "Quick Programmer", JOptionPane.WARNING_MESSAGE);
					actionPerformed(e);
				}
				createProgrammingFrame(file);
			}
		} else if(e.getActionCommand().equals("Save")) {
			getCurrentProgrammingFrame().save();
		} else if(e.getActionCommand().equals("SaveAs")) {
			getCurrentProgrammingFrame().saveAs();
		} else if(e.getActionCommand().equals("Exit")) {
			closeWindow();
		} else if(e.getActionCommand().equals("Run")) {
			getCurrentProgram().execute();
		} else if(e.getActionCommand().equals("Undo")) {
			getCurrentProgram().getJobManager().undo();
		} else if(e.getActionCommand().equals("Redo")) {
			getCurrentProgram().getJobManager().redo();
		} else if(e.getActionCommand().equals("NewWindow")) {
			new QuickProgrammer(false);
		} else if(e.getActionCommand().equals("BlockList")) {
			blockFrame.setVisible(menuBlockList.isSelected()); // 선택되었으면 true, 그 외 false
		}
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		closeWindow();
	}
	
	@SuppressWarnings("unchecked")
	private void closeWindow() {
		for(ProgrammingFrame innerFrame : (Stack<ProgrammingFrame>)programStack.clone()) {
			desktopPane.getDesktopManager().activateFrame(innerFrame);
			if(!innerFrame.prepareClose()) {
				return;
			}
		}
		this.setVisible(false);
		this.dispose();
		synchronized (openedWindows) {
			if(--openedWindows == 0) {
				System.exit(0);
			}
		}
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}
	
	void programNameChanged(ProgrammingFrame frame) {
		if(frame == getCurrentProgrammingFrame()) {
			setTitle(frame.getDefaultTitle() + " - Quick Programmer");
		}
	}
	
	private void activateProgram(ProgrammingFrame frame) {
		programStack.remove(frame);
		programStack.push(frame);
		
		if(currentJobManager != null) {
			currentJobManager.removeJobListener(currentListener);
		}
		currentListener = new UndoRedoCheckListener();
		currentJobManager = frame.getPanel().getProgram().getJobManager();
		currentJobManager.addJobListener(currentListener);
		checkUndoRedoEnabled();
		
		setProgramMenuEnabled(true);
		setTitle(frame.getDefaultTitle() + " - Quick Programmer");
	}
	
	private final class BasicFrameListener extends InternalFrameAdapter {
		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
			if(e.getInternalFrame() instanceof BlockTreeFrame) {
				menuBlockList.setSelected(false);
			}
		}
	}
	
	private void checkUndoRedoEnabled() {
		Program program = getCurrentProgram();
		if(program != null) {
			menuUndo.setEnabled(program.getJobManager().canUndo());
			menuRedo.setEnabled(program.getJobManager().canRedo());
		}
	}
	
	private JobManager currentJobManager;
	private UndoRedoCheckListener currentListener;
	
	private final class UndoRedoCheckListener implements JobListener {
		@Override
		public void jobDone(Job job) {
			checkUndoRedoEnabled();
		}
		
		@Override
		public void jobUndone(Job job) {
			checkUndoRedoEnabled();
		}
		
	}
	
	private final class ProgrammingFrameListener extends InternalFrameAdapter {

		@Override
		public void internalFrameActivated(InternalFrameEvent e) {
			if(e.getInternalFrame() instanceof ProgrammingFrame) {
				activateProgram((ProgrammingFrame) e.getInternalFrame());
			}
		}
		
		@Override
		public void internalFrameOpened(InternalFrameEvent e) {
			if(e.getInternalFrame() instanceof ProgrammingFrame) {
				activateProgram((ProgrammingFrame) e.getInternalFrame());
			}
		}
		
		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
			if(e.getInternalFrame() instanceof ProgrammingFrame) {
				ProgrammingFrame frame = (ProgrammingFrame) e.getInternalFrame();
				if(frame.prepareClose()) {
					frame.setVisible(false);
					frame.dispose();
					
					programStack.remove(frame);
					if(programStack.isEmpty()) {
						setProgramMenuEnabled(false);
						
						currentJobManager.removeJobListener(currentListener);
						currentJobManager = null;
						currentListener = null;
						
						setTitle("Quick Programmer");
					} else {
						desktopPane.getDesktopManager().activateFrame(programStack.peek());
					}
				}
			}
		}		
	}
}
