package net.wkbae.quickprogrammer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import net.wkbae.quickprogrammer.listener.BlockResizeListener;
import net.wkbae.quickprogrammer.listener.VariableSocketResizeListener;

/**
 * 변수 블록({@link VariableBlock}) 하나를 붙일 수 있는 소켓입니다.<br>
 * 변수 블록은 여러 개를 잇는 것으로 의미를 만들지 못하므로 하나의 변수만 받아들이도록 설계했습니다.<br>
 * 단, 변수 블록에 소켓을 붙이는 방식으로 래퍼(Wrapper)를 만들어 활용할 수 있습니다.
 * @author WKBae
 * @see VariableBlock
 * @deprecated 아직 완성되지 않았습니다.
 */
@Deprecated
public class VariableSocket {
	
	public final static int VARIABLE_SOCKET_MARGIN = 10;
	public final static int VARIABLE_SOCKET_DEPTH = 10;
	public final static int VARIABLE_SOCKET_HEIGHT = 30;
	private final static int VARIABLE_BLOCK_MARGIN = 5;
	public final static int VARIABLE_BLOCK_HEIGHT = 20;
	
	
	private int id;
	
	private AttachInfo lastAttachInfo;
	private DetachInfo lastDetachInfo;
	private AttachJob lastAttachJob = null;
	private DetachJob lastDetachJob = null;
	
	private Program program;
	
	VariableSocket(Program program, int id) {
		this.program = program;
		this.id = id;
		
		recalculateMagnetArea();
	}
	
	public int getSocketId() {
		return id;
	}
	
	private int height = VARIABLE_SOCKET_HEIGHT;
	/**
	 * 변수 블록의 높이를 설정합니다.
	 * @param height 설정할 높이
	 */
	private void setHeight(int height) {
		this.height = height;
		
		recalculateMagnetArea();
		
		for(VariableSocketResizeListener listener : listeners) {
			listener.variableSocketResized(this, height);
		}
	}
	
	/**
	 * 변수 블록의 높이를 구합니다.
	 * @return  변수 블록의 높이
	 */
	public int getHeight() {
		return height;
	}

	private int startX, centerY;
	public void setStartX(int startX) {
		this.startX = startX;
		recalculateMagnetArea();
	}
	
	public void setCenterY(int centerY) {
		this.centerY = centerY;
		recalculateMagnetArea();
	}
	
	public void setLocation(int startX, int centerY) {
		this.startX = startX;
		this.centerY = centerY;
		recalculateMagnetArea();
	}
	
	private MagnetArea magnet;
	private void recalculateMagnetArea() {
		this.magnet = new MagnetArea(0, startX, centerY);
	}
	
	public MagnetArea getMagnetArea() {
		return magnet;
	}
	
	public void reorderBlock() {
		if(attachedBlock != null) {
			setHeight(attachedBlock.getHeight() + VARIABLE_BLOCK_MARGIN * 2);
			attachedBlock.setLocation(startX + VARIABLE_BLOCK_MARGIN, centerY - attachedBlock.getHeight()/2);
		}
	}
	
	private VariableBlock attachedBlock = null;
	public void attachBlock(VariableBlock block) {
		lastAttachInfo = new AttachInfo(block);
		lastAttachJob = new AttachJob(block);
		program.getJobManager().doJob(lastAttachJob);
	}
	
	public void detachBlock(boolean saveJob) {
		if(saveJob) {
			detachBlock();
		} else {
			new DetachJob().execute();
		}
	}
	
	public void detachBlock() {
		lastDetachInfo = new DetachInfo(attachedBlock);
		lastDetachJob = new DetachJob();
		program.getJobManager().doJob(lastDetachJob);
	}
	
	public VariableBlock getAttachedBlock() {
		return attachedBlock;
	}
	
	void undoAttachJob() {
		if(lastAttachInfo != null) {
			this.attachedBlock = null;
			lastAttachInfo.getBlock().removeResizeListener(listener);
			lastAttachInfo.getBlock().setLocation(lastAttachInfo.getOriginalBlockLocation());
			this.setHeight(VARIABLE_SOCKET_HEIGHT);
			
			program.getJobManager().removeJob(lastAttachJob);
			lastAttachInfo = null;
			lastAttachJob = null;
		}
	}
	
	void undoDetachJob() {
		if(lastDetachInfo != null) {
			this.attachedBlock = lastDetachInfo.getBlock();
			
			if(attachedBlock != null) {
				setHeight(attachedBlock.getHeight() + VARIABLE_BLOCK_MARGIN * 2);
				attachedBlock.setLocation(startX + VARIABLE_BLOCK_MARGIN, centerY + attachedBlock.getHeight() / 2);
				
				listener = new VariableBlockResizeListener();
				attachedBlock.addResizeListener(listener);
			}
			
			program.getJobManager().removeJob(lastDetachJob);
			lastAttachInfo = null;
			lastDetachJob = null;
		}
	}
	
	public SocketInfo getSocketInfo() {
		return new SocketInfo(id, startX, centerY, attachedBlock);
	}
	
	public boolean isAttachable(VariableBlock block) {
		if(attachedBlock != null) return false; // 이미 변수 블록이 붙어있는 경우 X
		return magnet.contains(block.getX(), block.getY());
	}
	
	public void paintSocket(Image backgroundAsImage, Graphics2D imageG, int startY) {
		int width = backgroundAsImage.getWidth(null);
		int height = backgroundAsImage.getHeight(null);
		BufferedImage img = imageG.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		Graphics2D gr = img.createGraphics();
		
		gr.setComposite(AlphaComposite.Clear);
		gr.fillRect(0, 0, width, height);
		
		gr.setComposite(AlphaComposite.Src);
		gr.setColor(Color.WHITE);
		gr.fillRect(0, 0, width, height);
		
		gr.setComposite(AlphaComposite.Clear);
			
		gr.fillRoundRect(width - VARIABLE_SOCKET_DEPTH, startY, VARIABLE_SOCKET_DEPTH*2, getHeight(), 10, 10);
		
		
		gr.setComposite(AlphaComposite.SrcAtop);
		gr.drawImage(backgroundAsImage, 0, 0, null);
		
		gr.dispose();
		
		imageG.setComposite(AlphaComposite.Clear);
		imageG.fillRect(0, 0, width, height);
		
		imageG.setComposite(AlphaComposite.Src);
		imageG.drawImage(img, 0, 0, null);
		
		imageG.setColor(Color.GRAY);
		imageG.setStroke(new BasicStroke());
		
		imageG.drawRoundRect(width - VARIABLE_SOCKET_DEPTH, startY, VARIABLE_SOCKET_DEPTH*2, getHeight(), 10, 10);
		
	}
	
	public Object evaluate() {
		return attachedBlock.getModel().evaluate();
	}
	
	private ArrayList<VariableSocketResizeListener> listeners = new ArrayList<>();
	
	public void addSocketResizeListener(VariableSocketResizeListener listener) {
		listeners.add(listener);
	}
	
	public void removeSocketResizeListener(VariableSocketResizeListener listener) {
		listeners.remove(listener);
	}
	
	private VariableBlockResizeListener listener;
	
	public static class SocketInfo {
		private SocketInfo(int socketId, int startX, int centerY, VariableBlock block) {
			this.socketId = socketId;
			this.startX = startX;
			this.centerY = centerY;
			this.block = block;
		}
		public final int socketId;
		public final int startX, centerY;
		public final VariableBlock block;
	}
	
	private class AttachInfo {
		private VariableBlock block;
		private Point originalBlockLoc;
		public AttachInfo(VariableBlock block) {
			this.block = block;
			this.originalBlockLoc = block.getLocation();
		}
		
		public VariableBlock getBlock() {
			return block;
		}
		
		public Point getOriginalBlockLocation() {
			return originalBlockLoc;
		}
	}
	
	private class DetachInfo {
		private VariableBlock block;
		public DetachInfo(VariableBlock block) {
			this.block = block;
		}
		
		public VariableBlock getBlock() {
			return block;
		}
	}
	
	private class AttachJob extends Job {
		private VariableBlock block;
		
		private AttachJob(VariableBlock block) {
			this.block = block;
		}
		
		@Override
		protected void execute() {
			attachedBlock = block;
			reorderBlock();
			listener = new VariableBlockResizeListener();
			block.addResizeListener(listener);
		}
	}
	
	private class DetachJob extends Job {
		
		@Override
		protected void execute() {
			VariableBlock block = attachedBlock;
			attachedBlock = null;
			
			block.removeResizeListener(listener);
			setHeight(VARIABLE_SOCKET_HEIGHT);
		}
		
		/*@Override
		public void undo() {
			if(block != null) {
				attachedBlock = block;
				setHeight(block.getHeight() + VARIABLE_BLOCK_MARGIN * 2);
				block.setLocation(startX + VARIABLE_BLOCK_MARGIN, centerY + block.getHeight() / 2);
				listener = new VariableBlockResizeListener();
				block.addResizeListener(listener);
			}
		}*/
	}
	
	private class VariableBlockResizeListener implements BlockResizeListener {
		@Override
		public void onBlockResize(BaseBlockModel block, int width, int height) {
			reorderBlock();
		}
	}
}
