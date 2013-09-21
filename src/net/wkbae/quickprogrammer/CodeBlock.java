package net.wkbae.quickprogrammer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JComponent;

import net.wkbae.quickprogrammer.ProgrammingFrame.ProgrammingPanel;
import net.wkbae.quickprogrammer.listener.BlockMoveListener;
import net.wkbae.quickprogrammer.listener.BlockResizeListener;

/**
 * 화면상에 표시되는 코드 블록을 나타내는 클래스입니다.<br>
 * 플러그인으로 코드 블록을 만들기 위해서는 이 클래스가 아닌 {@link BaseBlockModel}을 구현해야 합니다.
 * @author WKBae
 *
 */
public class CodeBlock extends JComponent {
	private static final long serialVersionUID = -5501622150990187258L;
	
	protected final Program program;
	protected final ProgrammingPanel panel;
	
	private final BaseBlockModel model;
	
	private OrderedInfo orderedInfo = null;
	private Dragger dragger = new Dragger();

	/**
	 * <p>
	 * 코드 블록을 생성합니다.<br>
	 * {@link BaseBlockModel#BaseBlockModel()}에서 호출합니다.
	 * </p><p>
	 * 한 모델에 대해서 하나의 블록만이 생성되어야 합니다.
	 * </p>
	 * @param model 코드 블록의 모델
	 */
	CodeBlock(Program program, BaseBlockModel model) {
		super();
		
		this.program = program;
		this.panel = program.getPanel();
		
		this.model = model;
		
		this.addMouseListener(new DoubleClickListener());
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(panel.isEraseMode()) {
					CodeBlock.this.program.removeBlock(CodeBlock.this);
				}
			}
		});
	}
	
	/**
	 * 이 코드 블록의 모델(Model)을 얻습니다.
	 * @return 코드 블록의 모델({@link BaseBlockModel})
	 */
	public BaseBlockModel getModel(){
		return model;
	}
	
	/**
	 * 블록의 위치를 지정합니다.<br>
	 * 위치가 변경되면 블록 이동 리스너({@link BlockMoveListener})들을 실행시킵니다.
	 * @see BaseBlockModel#setLocation(int, int)
	 * @see BlockMoveListener
	 */
	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		program.resetLocationRatio(this);
		
		for(BlockMoveListener listener : moveListeners) {
			if(listener != null) listener.onBlockMove(getModel(), x, y);
		}
	}
	
	/**
	 * 블록의 크기를 지정합니다.<br>
	 * 크기가 변경되면 블록 크기 변경 리스너({@link BlockResizeListener})들을 실행시킵니다.
	 * @see BaseBlockModel#setSize(int, int)
	 * @see BlockResizeListener
	 */
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);

		for(BlockResizeListener listener : resizeListeners) {
			if(listener != null) listener.onBlockResize(getModel(), width, height);
		}
	}
	
	/**
	 * 코드 블록을 드래그 할 수 있는지를 설정합니다.
	 * @param drag 블록을 드래그 할 수 있는지 여부, 블록을 드래그 할 수 있으면 <code>true</code>
	 */
	public void setDraggable(boolean drag){
		if(drag){
			this.addMouseListener(dragger);
			this.addMouseMotionListener(dragger);
		} else {
			this.removeMouseListener(dragger);
			this.removeMouseMotionListener(dragger);
		}
	}
	
	private ArrayList<BlockResizeListener> resizeListeners = new ArrayList<>();
	
	/**
	 * 블록 크기 변경 리스너를 추가합니다.
	 * @param listener 추가할 리스너
	 */
	public final void addResizeListener(BlockResizeListener listener) {
		resizeListeners.add(listener);
	}
	
	/**
	 * 블록 크기 변경 리스너를 제거합니다.
	 * @param listener 제거할 리스너
	 * @return 리스너를 포함하고 있었다면 <code>true</code>를 반환합니다.
	 */
	public final boolean removeResizeListener(BlockResizeListener listener) {
		return resizeListeners.remove(listener);
	}

	private ArrayList<BlockMoveListener> moveListeners = new ArrayList<>();
	
	/**
	 * 블록 이동 리스너를 추가합니다.
	 * @param listener 추가할 리스너
	 */
	public final void addMoveListener(BlockMoveListener listener) {
		moveListeners.add(listener);
 	}
	
	/**
	 * 블록 이동 리스너를 제거합니다.
	 * @param listener 제거할 리스너
	 * @return 리스너를 포함하고 있었다면 <code>true</code>를 반환합니다.
	 */
	public final boolean removeMoveListener(BlockMoveListener listener) {
		return moveListeners.remove(listener);
	}
	
	/**
	 * 이 코드 블록을 그립니다.<br>
	 * 이 메소드는 모델({@link BaseBlockModel})의 {@link BaseBlockModel#paintComponent(Graphics2D)} 메소드를 호출합니다.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		model.paintComponent((Graphics2D)g);
	}
	
	/**
	 * 블록이 레인에 나열된 상태 정보를 얻습니다.<br>
	 * 레인에 나열되어있지 않다면 <code>null</code>을 반환합니다.
	 * @return 블록 나열 상태 정보, 없다면 <code>null</code>을 반환합니다.
	 */
	public OrderedInfo getOrderedInfo() {
		return orderedInfo;
	}
	
	/**
	 * 블록이 나열된 정보를 설정합니다.<br>
	 * 이 메소드는 임의로 호출하면 안됩니다.
	 * @param info
	 */
	void setOrderedInfo(OrderedInfo info) {
		this.orderedInfo = info;
	}
	
	/**
	 * 블록이 나열된 정보를 제거합니다. 즉, 블록을 나열되지 않은 것으로 만듭니다.<br>
	 * 이 메소드는 임의로 호출하면 안됩니다.
	 */
	void removeOrderedInfo() {
		orderedInfo = null;
		dragger.laneAttachedInThisClick = null;
		dragger.laneDetachedInThisClick = null;
		
		if(dragger.lastMoveJob != null) {
			this.setLocation(dragger.lastMoveJob.getOriginalLocation());
			program.getJobManager().removeJob(dragger.lastMoveJob);
			dragger.lastMoveJob = null;
		}
	}
	
	/**
	 * <p>
	 * 코드 블록을 드래그 할 때 움직이는 것을 구현한 클래스입니다.<br>
	 * 클릭할 당시 마우스의 위치를 구한 뒤, 드래그한 이후 마우스의 위치와의 차이를 블록의 좌표에 더합니다.
	 * </p><p>
	 * 레인의 마그넷 영역에 붙을 수 있다면 자동으로 레인에 붙습니다.
	 * </p>
	 * @author WKBae
	 */
	private class Dragger implements MouseListener, MouseMotionListener {
		
		private int screenX, screenY; // 마우스가 클릭될 때, 마우스의 위치
		private int currentX, currentY; // 마우스가 클릭될 때의 위치
		
		private Lane laneAttachedInThisClick, laneDetachedInThisClick;
		private int originalOrder;
		
		private BlockMoveJob lastMoveJob = null;
		
		@Override
		public void mousePressed(MouseEvent e) {
			panel.setComponentZOrder(CodeBlock.this, 0);
			panel.repaint();
			
			screenX = e.getXOnScreen();
			screenY = e.getYOnScreen();
			
			currentX = getX();
			currentY = getY();
			
			laneAttachedInThisClick = null;
			laneDetachedInThisClick = null;
			getModel().getProgram().getJobManager().startMultipleJobs();
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			Point loc = e.getPoint();
			loc.translate(getX(), getY());
			if(panel.getTrashcanArea().contains(loc)) {
				program.removeBlock(CodeBlock.this);
			}
			
			laneAttachedInThisClick = null;
			laneDetachedInThisClick = null;
			lastMoveJob = null;
			getModel().getProgram().getJobManager().finishMultipleJobs();
		}
		@Override
		public void mouseExited(MouseEvent e) {}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseClicked(MouseEvent e) {}
		@Override
		public void mouseMoved(MouseEvent e) {}
		@Override
		public void mouseDragged(MouseEvent e) {
			
			int differenceX = e.getXOnScreen() - screenX;
			int differenceY = e.getYOnScreen() - screenY;
			
			int xToBe = currentX + differenceX;
			int yToBe = currentY + differenceY;
			
			if(orderedInfo != null){ // 붙어있는 레인이 비어있지 않다면(레인에 붙어있다면)
				if(!orderedInfo.attachedArea.contains(xToBe+getWidth()/2, yToBe)){// 마그넷 영역 바깥으로 나간다면
					if(laneAttachedInThisClick != null){// 지금 붙은 레인이 비어있지 않다면(이번 마우스 클릭에 레인에 붙었다면)
						orderedInfo.attachedLane.undoAttachJob(); // 붙은 것을 되돌림
						laneAttachedInThisClick = null; // 지금 붙은 레인(이번 마우스 클릭에 붙었던 레인)을 비움
					}else{ // 이번 클릭 이전에 붙었다면(클릭하기 전부터 붙어있었다면)
						laneDetachedInThisClick = orderedInfo.attachedLane; // 떼어낸 레인에 붙어있던 레인을 넣음
						originalOrder = orderedInfo.attachedLane.getOrderedIndex(CodeBlock.this); // 나열되어있던 위치에 나열된 위지를 넣음
						orderedInfo.attachedLane.detachBlock(CodeBlock.this); // 블록을 떼어냄
					}
					orderedInfo = null;
					
					if(lastMoveJob != null){
						program.getJobManager().removeJob(lastMoveJob);
					}
					lastMoveJob = new BlockMoveJob(currentX, currentY, xToBe, yToBe);
					program.getJobManager().doJob(lastMoveJob);
					program.resetLocationRatio(CodeBlock.this);
				}
			} else {
				boolean attached = false;
				for(Lane lane : program.getLanes().getAllLanes()){
					MagnetArea area = lane.getAttachableMagnetArea(CodeBlock.this);
					if(area != null){
						if(lane == laneDetachedInThisClick && area.index == originalOrder){
							laneDetachedInThisClick.undoDetachJob();
							laneDetachedInThisClick = null;
							originalOrder = -1;
						}else{//
							lane.attachBlock(area.index, CodeBlock.this);
							laneAttachedInThisClick = lane;
						}
						
						orderedInfo = new OrderedInfo(lane, area);
						attached = true;
						break;
					}
				}
				// 배치되지 않음
				if(!attached) {
					if(lastMoveJob != null){
						program.getJobManager().removeJob(lastMoveJob);
					}
					lastMoveJob = new BlockMoveJob(currentX, currentY, xToBe, yToBe);
					program.getJobManager().doJob(lastMoveJob);
					program.resetLocationRatio(CodeBlock.this);
				}
			}
		}
	}
	
	/**
	 * 코드 블록이 레인에 붙어있는 정보를 나타냅니다.<br>
	 * 연결되어있는 레인, 붙어있는 마그넷 영역, 그리고 레인에 연결되었을 때의 위치 정보를 가지고 있습니다.
	 * @author WKBae
	 * @see CodeBlock#getOrderedInfo()
	 * @see CodeBlock#setOrderedInfo(OrderedInfo)
	 */
	public static class OrderedInfo {
		/**
		 * 코드 블록이 연결되어있는 레인
		 */
		public final Lane attachedLane;
		/**
		 * 코드 블록이 붙어있는 마그넷 영역
		 * @see MagnetArea
		 */
		public final MagnetArea attachedArea;

		OrderedInfo(Lane attachedLane, MagnetArea attachedArea) {
			this.attachedLane = attachedLane;
			this.attachedArea = attachedArea;
		}
	}
	
	/**
	 * 코드 블록을 움직이는 작업 클래스입니다.<br>
	 * {@link CodeBlock#setLocation(int, int)}을 호출합니다.
	 * @author WKBae
	 *
	 */
	private class BlockMoveJob extends Job {
		private final int fromX, fromY, toX, toY;
		private BlockMoveJob(int fromX, int fromY, int toX, int toY){
			this.fromX = fromX;
			this.fromY = fromY;
			this.toX = toX;
			this.toY = toY;
		}
		@Override
		protected void execute() {
			CodeBlock.this.setLocation(toX, toY);
		}
		
		public Point getOriginalLocation() {
			return new Point(fromX, fromY);
		}
	}
	
	/**
	 * 블록을 더블클릭한 것을 감지하는 리스너입니다.<br>
	 * 더블클릭되었다면 {@link BaseBlockModel#onDoubleClick()} 메소드를 호출합니다.
	 * @author WKBae
	 * @see BaseBlockModel#onDoubleClick()
	 */
	private class DoubleClickListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1){
				model.onDoubleClick();
				program.getJobManager().createSnapshot();
				e.consume();
			}
		}
	}
}
