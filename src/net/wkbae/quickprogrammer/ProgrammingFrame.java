package net.wkbae.quickprogrammer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.Transient;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.wkbae.quickprogrammer.file.LoadManager;
import net.wkbae.quickprogrammer.file.SaveManager;
import net.wkbae.util.ErrorDialog;

/**
 * 프로그램을 제작할 창의 프레임입니다.<br>
 * 여기에 포함된 {@link ProgrammingPanel}가 코드 블록을 배치하는 곳입니다.
 * @author WKBae
 */
public final class ProgrammingFrame extends JInternalFrame {
	private static final long serialVersionUID = 24648605289575049L;

	private File openedFile = null;
	
	private String title;
	
	/**
	 * 프로그램 창을 지정된 이름으로 생성합니다.
	 * @param name 창의 이름
	 */
	ProgrammingFrame(String name) {
		super(name, true, true, true, true);
		
		this.title = name;
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		this.setSize(600, 400);
		
		this.panel = new ProgrammingPanel(this);
		
		this.setFrameIcon(null);
		
		JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.getViewport().addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				panel.viewportMoved(((JViewport)e.getSource()).getViewRect());
			}
		});
		this.setContentPane(scrollPane);
		
	}
	
	/**
	 * 프로그램 창을 지정된 파일을 불러와서 생성합니다.
	 * @param file 불러올 파일
	 * @throws IOException 파일을 읽는 데 실패한 경우
	 */
	ProgrammingFrame(File file) throws IOException {
		this(file.getName());
		
		this.openedFile = file;
		load(file);
	}
	
	private ProgrammingPanel panel;
	
	/**
	 * 프로그램의 패널을 구합니다.
	 * @return 이 창의 패널
	 */
	public ProgrammingPanel getPanel() {
		return panel;
	}
	
	/**
	 * 이 창의 기본 이름을 구합니다.<br>
	 * 파일이 변경되었을 때 이 이름에 "* "을 붙입니다.
	 * @return 창의 이름
	 */
	public String getDefaultTitle() {
		return title;
	}
	
	/**
	 * 파일을 저장합니다. 열린 파일이 없다면 파일 선택 창을 생성합니다.
	 * @return 저장을 성공했다면 <code>true</code>를 반환합니다.
	 */
	public boolean save() {
		if(openedFile == null){
			return saveAs();
		}
		try {
			new SaveManager(getPanel().getProgram(), openedFile).save();
			getPanel().getProgram().setSaved();
			return true;
		} catch (IOException e) {
			new ErrorDialog(new IOException("파일을 저장할 수 없습니다.", e));
		}
		return false;
	}
	
	/**
	 * 열린 파일을 선택하는 창을 엽니다.
	 * @return 저장이 되었다면 <code>true</code>를 반환합니다.
	 */
	public boolean saveAs() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileExtensionFilter("qps", "퀵 프로그래머 저장 형식 (*.qps)"));
		int ret = fc.showSaveDialog(this);
		if(ret == JFileChooser.APPROVE_OPTION) {
			if(fc.getFileFilter() instanceof FileExtensionFilter) {
				File file = fc.getSelectedFile();
				if(file.exists()) {
					int res = JOptionPane.showConfirmDialog(this, "이미 파일이 존재합니다. 덮어쓰시겠습니까?", "Quick Programmer", JOptionPane.YES_NO_OPTION);
					if(res == JOptionPane.NO_OPTION) {
						saveAs();
					} else {
						openedFile = file;
					}
				} else {
					openedFile = new File(fc.getSelectedFile() + ((FileExtensionFilter)fc.getFileFilter()).getExtension());
				}
			} else {
				openedFile = fc.getSelectedFile();
			}
			this.title = openedFile.getName();
			this.setTitle(getDefaultTitle());
			
			return save();
		} else {
			// 파일 창에서 취소를 누른 경우
			return false;
		}
	}
	
	/**
	 * 파일을 불러옵니다.
	 * @param file 불러올 파일
	 * @throws IOException 파일을 불러오는 데 실패한 경우
	 */
	private void load(File file) throws IOException {
		new LoadManager(getPanel().getProgram(), file).load();
		getPanel().getProgram().getJobManager().clearJobs();
		getPanel().getProgram().getJobManager().resetSnapshots();
		//getPanel().getProgram().setSaved();
	}
	
	/**
	 * 창을 닫을 준비를 합니다.<br>
	 * 파일을 저장할 지 여부를 묻습니다.
	 * @return 닫을 수 있다면 <code>true</code>
	 */
	public boolean prepareClose() {
		if(getPanel().getProgram().isChangedFromSave()) {
			int selection = JOptionPane.showConfirmDialog(this, "파일이 변경되었습니다. 저장하시겠습니까?", "Quick Programmer", JOptionPane.YES_NO_CANCEL_OPTION);
			switch(selection) {
			case JOptionPane.YES_OPTION:
				return save();
			case JOptionPane.NO_OPTION:
				return true;
				
			case JOptionPane.CANCEL_OPTION:
			case JOptionPane.CLOSED_OPTION:
				return false;
				
			default:
				return true;
			}
		} else { // 파일이 바뀌지 않음
			return true;
		}
	}
	
	/* 휴지통 이미지들 */
	private static Image TRASHCAN_OPENED;

	private static Image TRASHCAN_CLOSED;

	private final static Rectangle TRASHCAN_SIZE = new Rectangle(46, 80);

	private final static Rectangle TRASHCAN_CURSOR_SIZE = new Rectangle(11, 20);

	private static Image TRASHCAN_CURSOR;

	private final static int TRASHCAN_MARGIN = 10;
	static {
		try {
			InputStream open = Program.class.getResourceAsStream("/image/trashcan_opened.png");
			if(open != null) {
				TRASHCAN_OPENED = ImageIO.read(open);
			} else {
				TRASHCAN_OPENED = new BufferedImage(TRASHCAN_SIZE.width, TRASHCAN_SIZE.height, BufferedImage.TYPE_INT_ARGB);
			}
			
			InputStream close = Program.class.getResourceAsStream("/image/trashcan_closed.png");
			if(close != null) {
				TRASHCAN_CLOSED = ImageIO.read(close);
			} else {
				TRASHCAN_CLOSED = new BufferedImage(TRASHCAN_SIZE.width, TRASHCAN_SIZE.height, BufferedImage.TYPE_INT_ARGB);
			}
			
			TRASHCAN_CURSOR = TRASHCAN_OPENED.getScaledInstance(TRASHCAN_CURSOR_SIZE.width, TRASHCAN_CURSOR_SIZE.height, Image.SCALE_SMOOTH);
		} catch (IOException e) {
			throw new IllegalStateException("이미지 파일을 읽을 수 없습니다.", e);
		}
	}
	
	/**
	 * 프로그램이 표시되는 패널입니다.<br>
	 * 이 패널은 프로그래밍 창({@link ProgrammingFrame}) 내부에 위치해 유저들이 블록들을 배치시킬 수 있도록 합니다.
	 * @author WKBae
	 *
	 */
	public final class ProgrammingPanel extends JPanel implements MouseListener, MouseMotionListener {
		private static final long serialVersionUID = -4216384521158726111L;
		
		private final static int SCROLL_MARGIN = 50;

		private ProgrammingFrame frame;
		private Program program;
		
		private ProgrammingPanel(ProgrammingFrame frame) {
			this.setBackground(Color.WHITE);
			this.setLayout(null);
			
			this.frame = frame;
			this.program = new Program(this);
			
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
		}
		
		
		@Override
		@Transient
		public Dimension getPreferredSize() {
			Lane mainLane = getProgram().getLanes().getMainLane();
			return new Dimension(mainLane.getWidth() + SCROLL_MARGIN, mainLane.getHeight() + SCROLL_MARGIN);
		}
		
		/**
		 * 이 패널이 포함된 프레임을 구합니다.
		 * @return 이 패널의 프레임
		 */
		public ProgrammingFrame getFrame() {
			return frame;
		}
		
		/**
		 * 이 패널의 프로그램을 구합니다.
		 * @return 이 패널의 프로그램
		 */
		public Program getProgram() {
			return program;
		}
		
		private HashMap<Object, ArrayList<Point[]>> lines = new HashMap<>();
		/**
		 * 패널에 점선을 그립니다. 레인을 그릴 때 사용됩니다.
		 * @param key 점선을 그리는 객체
		 * @param start 선의 시작점
		 * @param end 선의 끝점
		 */
		void addLine(Object key, Point start, Point end) {
			ArrayList<Point[]> pointList = lines.get(key);
			if(pointList == null) {
				pointList = new ArrayList<>();
				lines.put(key, pointList);
			}
			pointList.add(new Point[]{start, end});
		}
		
		/**
		 * 모든 선을 제거합니다.
		 */
		void clearAllLines() {
			lines.clear();
		}
		
		/**
		 * 지정된 객체의 선을 제거합니다.
		 * @param key 제거할 선의 객체
		 */
		void clearLines(Object key) {
			ArrayList<Point[]> pointList = lines.get(key);
			if(pointList != null) {
				pointList.clear();
			}
		}
		
		/**
		 * 전체 목록에서 지정한 객체를 제거합니다.
		 * @param key 제거할 객체
		 */
		void removeLines(Object key) {
			lines.remove(key);
			repaint();
		}
		
		private Stroke dottedStroke = new BasicStroke(1, 0, 0, 1, new float[]{2, 2}, 1);
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			((Graphics2D)g).setStroke(dottedStroke);
			for(ArrayList<Point[]> pointList : lines.values()){
				for(Point[] points : pointList) {
					if(points.length != 2){
						continue;
					}
					
					g.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
				}
			}
			
			g.drawImage(((Image)(trashcanOpened? TRASHCAN_OPENED : TRASHCAN_CLOSED)).getScaledInstance(TRASHCAN_SIZE.width, TRASHCAN_SIZE.height, Image.SCALE_SMOOTH),
					trashcanArea.x, trashcanArea.y, trashcanArea.width, trashcanArea.height, null);
		}
		
		private Rectangle trashcanArea = new Rectangle(0, 0, 0, 0);
		
		private void viewportMoved(Rectangle visibleArea) {
			trashcanArea.setBounds(visibleArea.x + visibleArea.width - TRASHCAN_SIZE.width - TRASHCAN_MARGIN,
					visibleArea.y + visibleArea.height - TRASHCAN_SIZE.height - TRASHCAN_MARGIN,
					TRASHCAN_SIZE.width, TRASHCAN_SIZE.height);
			this.repaint();
		}
		
		Rectangle getTrashcanArea() {
			return trashcanArea;
		}
		
		private boolean trashcanOpened = false;
		void setTrashcanOpened(boolean opened) {
			trashcanOpened = opened;
			this.repaint(trashcanArea);
		}
		
		private boolean eraseMode = false;
		
		boolean isEraseMode() {
			return eraseMode;
		}
		
		private void setEraseMode(boolean eraseMode) {
			this.eraseMode = eraseMode;
			if(eraseMode) {
				BufferedImage cursorImg = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
				cursorImg.getGraphics().drawImage(TRASHCAN_CURSOR, 0, 0, TRASHCAN_CURSOR_SIZE.width, TRASHCAN_CURSOR_SIZE.height, null);
				
				Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "Erase Mode");
				
				setCursor(blankCursor);
			} else {
				setCursor(Cursor.getDefaultCursor());
			}
			repaint();
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1) {
				if(trashcanArea.contains(e.getPoint())) {
					setEraseMode(!eraseMode); // Toggle
				}
			} else if(e.getButton() == MouseEvent.BUTTON3) {
				if(eraseMode) {
					setEraseMode(false);
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {
			if(eraseMode && e.getButton() == MouseEvent.BUTTON3) {
				setEraseMode(false);
			}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			mouseMoved(e);
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			validate();
			
			if(trashcanArea.contains(e.getPoint())) {
				setTrashcanOpened(true);
			} else {
				setTrashcanOpened(false);
			}
		}
	}
}
