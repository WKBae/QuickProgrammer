package net.wkbae.quickprogrammer;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * 변수 블록입니다.<br>
 * 코드 블록과 비슷하지만, 변수 소켓({@link VariableSocket})에 하나만 연결됩니다.
 * @deprecated 아직 준비되지 않은 기능입니다.
 * @author WKBae
 *
 */
@Deprecated
public class VariableBlock extends CodeBlock {
	private static final long serialVersionUID = 8652899134099016824L;
	
	private Dragger dragger = new Dragger();
	
	private VariableBlockModel model;
	
	VariableBlock(Program program, VariableBlockModel model) {
		super(program, model);
		this.model = model;
	}
	
	@Override
	public VariableBlockModel getModel() {
		return model;
	}
	
	@Override
	public void setDraggable(boolean drag) {
		if(drag) {
			this.addMouseListener(dragger);
			this.addMouseMotionListener(dragger);
		} else {
			this.removeMouseListener(dragger);
			this.removeMouseMotionListener(dragger);
		}
	}
	
	/**
	 * 이 블록이 붙은 소켓을 구합니다.
	 * @return 변수의 소켓
	 */
	public VariableSocket getAttachedSocket() {
		if(attachedSocket == null) {
			return null;
		} else {
			return program.getVariableSockets().getSocket(attachedSocket);
		}
	}
	
	/**
	 * 변수 블록이 붙어있는 소켓 ID를 반환합니다.<br>
	 * 만약 블록이 소켓에 붙어있지 않다면 <code>null</code>을 반환합니다.
	 * @return 현재 부착된 소켓의 ID, 부착되지 않았다면 <code>null</code>
	 */
	public Integer getAttachedSocketId() {
		return attachedSocket;
	}
	
	private Integer attachedSocket = null;
	
	/**
	 * 변수 블록의 드래그를 처리하는 클래스입니다
	 * @author WKBae
	 *
	 */
	private class Dragger implements MouseListener, MouseMotionListener {

		private int screenX, screenY;
		private int currentX, currentY;
		
		private VariableSocket attachedNow, detachedNow;
		
		private BlockMoveJob lastMoveJob = null;
		
		@Override
		public void mousePressed(MouseEvent e) {
			panel.setComponentZOrder(VariableBlock.this, 0);
			panel.repaint();
			
			screenX = e.getXOnScreen();
			screenY = e.getYOnScreen();
			
			currentX = getX();
			currentY = getY();
			
			attachedNow = null;
			detachedNow = null;
		}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			int differenceX = e.getXOnScreen() - screenX;
			int differenceY = e.getYOnScreen() - screenY;
			
			int xToBe = currentX + differenceX;
			int yToBe = currentY + differenceY;
			
			if(attachedSocket != null) {
				VariableSocket socket = program.getVariableSockets().getSocket(attachedSocket);
				if(!socket.getMagnetArea().contains(xToBe, yToBe+getHeight()/2)) {
					if(attachedNow != null) {
						socket.undoAttachJob();
					} else {
						detachedNow = socket;
						socket.detachBlock();
					}
					attachedSocket = null;

					if(lastMoveJob != null){
						program.getJobManager().removeJob(lastMoveJob);
					}
					lastMoveJob = new BlockMoveJob(currentX, currentY, xToBe, yToBe);
					program.getJobManager().doJob(lastMoveJob);
					program.resetLocationRatio(VariableBlock.this);
				} else {
					return;
				}
			} else {
				boolean attached = false;
				for(VariableSocket socket : program.getVariableSockets().getAllSockets()) {
					if(socket.getMagnetArea().contains(getX(), getY()+getHeight()/2)) {
						if(socket == detachedNow) {
							detachedNow.undoDetachJob();
							detachedNow = null;
						} else {
							socket.attachBlock(VariableBlock.this);
							attachedNow = socket;
						}
						
						attachedSocket = socket.getSocketId();
						attached = true;
						break;
					}
				}
				if(!attached) {
					if(lastMoveJob != null){
						program.getJobManager().removeJob(lastMoveJob);
					}
					lastMoveJob = new BlockMoveJob(currentX, currentY, xToBe, yToBe);
					program.getJobManager().doJob(lastMoveJob);
					program.resetLocationRatio(VariableBlock.this);
				}	
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {}

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}
		
	}
	
	/**
	 * 블록을 움직이는 작업입니다.
	 * @author WKBae
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
			VariableBlock.this.setLocation(toX, toY);
		}
		
		public Point getOriginalLocation() {
			return new Point(fromX, fromY);
		}
	}
}
