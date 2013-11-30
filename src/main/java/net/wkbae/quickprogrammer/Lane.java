package net.wkbae.quickprogrammer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.wkbae.quickprogrammer.CodeBlock.OrderedInfo;
import net.wkbae.quickprogrammer.ProgrammingFrame.ProgrammingPanel;
import net.wkbae.quickprogrammer.listener.BlockResizeListener;
import net.wkbae.quickprogrammer.listener.LaneResizeListener;
import net.wkbae.quickprogrammer.runner.ProgramThreadGroup;

/**
 * 코드 블록들이 나열되는 장소입니다. 프로그램은 항상 하나의 메인 레인을 가지고 있으며, 프로그램을 실행할 때 메인 레인의 순서대로 실행됩니다.
 * @author WKBae
 */
public final class Lane {
	
	public final static int BLOCK_MARGIN_SIZE = 10;
	
	private int centerX, startY;
	
	private int width, height;
	
	private int id;
	
	private LinkedList<CodeBlock> blocks;
	
	private HashMap<Integer, MagnetArea> magnetAreas;
	
	private ArrayList<LaneResizeListener> resizeListeners;
	
	private HashMap<CodeBlock, InnerBlockResizeListener> blockResizeListeners = new HashMap<>();

	private Program program;
	
	private AttachInfo lastAttachInfo;
	private DetachInfo lastDetachInfo;
	private AttachBlockJob lastAttachJob;
	private DetachBlockJob lastDetachJob;
	
	private boolean visible = true;

	// 변수의 초기화
	{
		magnetAreas = new HashMap<Integer, MagnetArea>();
		resizeListeners = new ArrayList<>();
		
		this.width = 0;
		this.height = BLOCK_MARGIN_SIZE;
	}
	
	/**
	 * 레인을 생성합니다.<br>
	 * 레인의 생성은 반드시 {@link Lanes#createLane()}을 이용해야 합니다.
	 * @see Lanes#createLane()
	 * @see Lanes#createLane(int)
	 */
	Lane(Program program, int id) {
		blocks = new LinkedList<CodeBlock>();
		this.program = program;
		this.id = id;
		
		recalculateMagnetArea();
	}
	
	/**
	 * 블록이 나열되어있는 레인을 생성합니다.<br>
	 * 주어진 리스트에서의 순서에 맞춰서 배열됩니다.
	 * @param blocks 배열될 블록들
	 */
	Lane(Program program, int id, List<CodeBlock> blocks){
		this.blocks = new LinkedList<CodeBlock>(blocks);
		this.program = program;
		this.id = id;
		
		refreshLayout();
	}
	
	/**
	 * 레인의 ID를 구합니다.<br>
	 * 레인의 상태를 저장할 때는 항상 Lane 객체가 아닌 ID로 저장해야 합니다.<br>
	 * 레인 객체는 {@link Lanes#getLane(int)}을 이용해 구할 수 있습니다.
	 * @return 이 레인의 ID
	 * @see Lanes#getLane(int)
	 */
	public int getLaneId() {
		return id;
	}
	
	/**
	 * 레인의 X축(가로) 위치를 변경합니다.<br>
	 * 블록들은 이 좌표를 중심으로 나열됩니다.<br>
	 * 이 메소드를 실행하고 반드시 {@link #refreshLayout()}를 실행해야 블록이 올바르게 나열됩니다.
	 * @param centerX 레인의 중심 X좌표
	 */
	public void setCenterX(int centerX){
		this.centerX = centerX;
	}
	
	/**
	 * 레인의 Y축(세로) 위치를 변경합니다.<br>
	 * 블록들은 이 좌표를 시작으로 나열됩니다.<br>
	 * 이 메소드를 실행하고 반드시 {@link #refreshLayout()}를 실행해야 블록이 올바르게 나열됩니다.
	 * @param startY 레인의 시작점 Y좌표
	 */
	public void setStartY(int startY){
		this.startY = startY;
	}
	
	/**
	 * 레인의 위치를 설정합니다.<br>
	 * 이 메소드를 호출하면 자동으로 {@link #refreshLayout()}를 실행합니다.
	 * @param centerX 레인의 중심 X좌표
	 * @param startY 레인의 시작점 Y좌표
	 */
	public void setLocation(int centerX, int startY) {
		this.centerX = centerX;
		this.startY = startY;
		refreshLayout();
	}

	/**
	 * 레인 중심의 X좌표를 구합니다.
	 * @return 레인 중앙의 X좌표
	 */
	public int getCenterX(){
		return centerX;
	}

	/**
	 * 레인의 시작점의 Y좌표를 구합니다.
	 * @return 레인의 시작점 Y좌표
	 */
	public int getStartY(){
		return startY;
	}

	/**
	 * 레인의 너비를 구합니다.<br>
	 * 레인의 너비는 레인에 포함된 블록 중 가장 넓은 블록의 너비와 같습니다.
	 * @return 레인의 너비
	 */
	public int getWidth(){
		return width;
	}

	/**
	 * 레인의 높이를 구합니다.<br>
	 * 레인의 높이는 레인에 포함된 모든 블록들의 높이의 합과 블록간 간격의 합과 같습니다.
	 * @return 레인의 높이
	 */
	public int getHeight(){
		return height;
	}

	/**
	 * 레인을 보이게 할 지 설정합니다.<br>
	 * 보이지 않도록 설정하면 모든 내부 블록들도 {@link CodeBlock#setVisible(boolean) CodeBlock.setVisible(false)}를 이용해 숨겨집니다.
	 * @param visible 레인이 보일 지 여부. 감추려면 <code>false</code>
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		refreshLayout();
	}

	/**
	 * 이 레인이 보이는 지 여부를 구합니다.
	 * @return 레인이 보인다면 <code>true</code>를 반환합니다.
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * 블록을 레인에 배열합니다.<br>
	 * 블록이 붙을 위치를 지정하며, 해당 위치에 있던 블록 및 그 뒤에 있던 블록들은 한 칸씩 밀려납니다.
	 * @param order 블록이 나열될 위치, -1이거나 모든 나열된 블록의 수보다 크다면 맨 뒤에 추가합니다.
	 * @param block 나열될 블록
	 */
	public void attachBlock(int order, CodeBlock block){
		lastAttachInfo = new AttachInfo(block);
		lastAttachJob = new AttachBlockJob(block, order);
		program.getJobManager().doJob(lastAttachJob);
	}
	
	/**
	 * 블록을 레인에서 제거합니다.
	 * @param block 제거할 블록
	 */
	public void detachBlock(CodeBlock block){
		lastDetachInfo = new DetachInfo(block);
		lastDetachJob = new DetachBlockJob(block);
		program.getJobManager().doJob(lastDetachJob);
	}
	
	/**
	 * 블록이 레인에 붙은 것을 취소합니다.<br>
	 * 마지막으로 붙었던 블록은 떨어지며, 레인은 해당 블록이 붙기 직전의 상태로 돌아갑니다.<br>
	 * 레인에 붙은 블록 중 마지막 하나의 작업만을 기억하므로 두 번 이상 취소되지 않습니다.
	 */
	public void undoAttachJob(){
		if(lastAttachInfo != null){
			this.blocks = lastAttachInfo.getOriginalBlocks();
			
			program.setOrdered(lastAttachInfo.getBlock(), false);
			lastAttachInfo.getBlock().removeOrderedInfo();
			lastAttachInfo.getBlock().setLocation(lastAttachInfo.getOriginalBlockLocation());
			
			refreshLayout();
			
			program.getJobManager().removeJob(lastAttachJob);
			lastAttachInfo = null;
			lastAttachJob = null;
		}
	}
	
	/**
	 * 블록이 레인에서 떨어진 것을 취소합니다.<br>
	 * 마지막으로 떨어졌던 블록이 다시 붙으며, 레인은 해당 블록이 떨어지기 직전의 상태로 돌아갑니다.<br>
	 * 레인에서 떨어진 블록 중 마지막 하나의 작업만을 기억하므로 두 번 이상 취소되지 않습니다.
	 */
	public void undoDetachJob(){
		if(lastDetachInfo != null){
			this.blocks = lastDetachInfo.getOriginalBlocks();
			
			program.setOrdered(lastDetachInfo.getBlock(), true);
			InnerBlockResizeListener listener = new InnerBlockResizeListener();
			blockResizeListeners.put(lastDetachInfo.getBlock(), listener);
			lastDetachInfo.getBlock().addResizeListener(listener);
			
			refreshLayout();
			
			program.getJobManager().removeJob(lastDetachJob);
			lastDetachInfo = null;
			lastDetachJob = null;
		}
	}
	
	/**
	 * 블록이 나열된 위치를 반환합니다.
	 * @param block 위치를 구할 블록
	 * @return 블록의 위치, 나열되지 않은 블록이라면 -1
	 */
	public int getOrderedIndex(CodeBlock block){
		return blocks.indexOf(block);
	}
	
	/**
	 * 블록이 마그넷으로 붙을 수 있을 영역을 구합니다.
	 * @param block 확인할 블록
	 * @return 블록이 붙을 수 있는 영역을 반환합니다. 만약 해당하는 영역이 없다면 <code>null</code>을 반환합니다.
	 */
	public MagnetArea getAttachableMagnetArea(CodeBlock block){
		// x좌표가 마그넷 영역에 포함되는 지 확인(레인은 세로로 일자이므로 x좌표가 맞지 않으면 이 레인의 모든 마그넷 영역에서 겹치지 않음)
		int blockCenterX = block.getX() + (block.getWidth()/2);
		if(checkXCoordForMagnet(blockCenterX)){
			// 레인에 존재하는 마그넷 영역들을 비교하여 포함되는 영역에 붙임 
			for(MagnetArea area : magnetAreas.values()){
				if(area == null){
					continue;
				}
				if(area.contains(blockCenterX, block.getY(), false)){
					return area;
				}
			}
		}
		return null;
	}

	/**
	 * 마그넷 효과로 레인에 붙을 영역을 얻습니다.
	 * @return 마그넷 영역, {@link Collections#unmodifiableCollection(Collection)}으로 처리되었으므로 변경할 수 없습니다.
	 */
	public Collection<MagnetArea> getMagnetAreas(){
		return Collections.unmodifiableCollection(magnetAreas.values());
	}

	/**
	 * 레인의 표시를 갱신합니다.<br>
	 * 레인의 위치가 변했거나 내부에 있는 블록의 크기가 바뀌는 등 다시 표시해야할 때 사용합니다.
	 * @return 레인의 크기가 변경되었다면 <code>true</code>
	 */
	public final boolean refreshLayout() {
		if(reorderBlocks()) {
			runResizeListeners();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 레인 크기 변경 리스너를 추가합니다.<br>
	 * 레인의 크기가 변경될 때 호출합니다.
	 * @param listener 추가할 리스너
	 */
	public void addResizeListener(LaneResizeListener listener) {
		resizeListeners.add(listener);
	}

	/**
	 * 레인 크기 변경 리스너를 제거합니다.
	 * @param listener 제거할 리스너
	 */
	public void removeResizeListener(LaneResizeListener listener) {
		resizeListeners.remove(listener);
	}

	/**
	 * 레인 크기 변경 리스너들을 실행시킵니다.<br>
	 * 실행하기 전, {@link #reorderBlocks()}를 실행시킵니다.
	 */
	private void runResizeListeners() {
		reorderBlocks();
		for(LaneResizeListener listener : resizeListeners) {
			if(listener != null) listener.laneResized(Lane.this, width, height);
		}
	}

	/**
	 * 이 레인을 실행합니다.<br>
	 * 레인에 나열된 블록들을 차례로 실행({@link BaseBlockModel#execute()})합니다.<br>
	 * 시작할 때 {@link Variables#laneStarted()}를 호출하고, 종료할 때 {@link Variables#laneFinished()}를 호출합니다.
	 */
	public void execute(){
		if(!(Thread.currentThread().getThreadGroup() instanceof ProgramThreadGroup)) {
			throw new IllegalThreadStateException("블록은 ProgramThreadGroup 내에서만 실행할 수 있습니다.");
		}
		
		program.getVariables().laneStarted();
		
		for(CodeBlock block : blocks){
			block.getModel().execute();
		}
	
		program.getVariables().laneFinished();
	}

	/**
	 * 레인 위의 모든 블록을 구합니다.<br>
	 * 리스트를 변경할 수 있으나 변경하지 않는 것이 좋습니다.
	 * @return 나열된 코드 블록 리스트
	 */
	public LinkedList<CodeBlock> getBlocks(){
		return blocks;
	}

	/**
	 * 마그넷 영역을 다시 계산합니다.<br>
	 * 이 메소드는 {@link #recalculateMagnetArea(int) recalculateMagnetArea(0)}와 동일합니다.
	 */
	void recalculateMagnetArea(){
		recalculateMagnetArea(0);
	}
	
	/**
	 * 마그넷 영역을 다시 계산합니다.<br>
	 * 주어진 위치로부터 계산합니다.
	 * @param from 다시 계산할 첫번째 위치, 계산에 포함됩니다.
	 */
	void recalculateMagnetArea(int from){
		if(from < 0) {
			throw new IllegalArgumentException("블록의 위치는 음수가 될 수 없습니다.");
		}
		if(visible) {
			if(from == 0) {
				magnetAreas.clear();
				magnetAreas.put(0, new MagnetArea(0, centerX, startY));
			} else {
				for(int i = magnetAreas.size()-1; i >= blocks.size(); i--){
					magnetAreas.remove(i);
				}
			}
			
			int start = from;
			
			for(int i = start; i < blocks.size(); i++){
				CodeBlock block = blocks.get(i);
				MagnetArea area = new MagnetArea(i+1, centerX, block.getY() + block.getHeight());
				magnetAreas.put(i+1, area);
				block.setOrderedInfo(new OrderedInfo(this, magnetAreas.get(i)));
			}
		} else {
			magnetAreas.clear();
		}
	}
	
	/**
	 * 레인의 너비를 다시 계산합니다.
	 * @param runListeners 등록된 {@link LaneResizeListener}들을 실행할 지 여부
	 * @return 너비가 바뀌었다면 <code>true</code>
	 * @see #getWidth()
	 * @see #refreshLayout()
	 */
	boolean recalculateWidth(boolean runListeners) {
		if(visible) {
			int lastWidth = this.width;
			
			int maxWidth = 0;
			for(CodeBlock blk : blocks){
				int w = blk.getWidth();
				if(w > maxWidth){
					maxWidth = w;
				}
			}
			this.width = maxWidth;
			
			//너비가 변경된 경우
			if(this.width != lastWidth) {
				if(runListeners) {
					runResizeListeners();
				}
				return true;
			} else {
				return false;
			}
		} else {
			if(this.width != 0) {
				reorderBlocks();
				
				if(runListeners) {
					runResizeListeners();
				}
				return true;
			} else {
				return false;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	LaneInfo getLaneInfo() {
		return new LaneInfo(id, centerX, startY, (LinkedList<CodeBlock>)blocks.clone());
	}

	/**
	 * 제시한 X좌표가 이 레인에 붙을 수 있는지 구합니다.<br>
	 * 레인은 항상 세로로 있으므로 X좌표를 이용해 일차적으로 붙을 수 있는지 확인할 수 있습니다.
	 * @param x 확인할 X좌표
	 * @return X좌표가 이 레인 위에 있는 마그넷  영역에 붙을 가능성이 있다면 <code>true</code>를 반환합니다.
	 */
	private boolean checkXCoordForMagnet(int x){
		return (x >= centerX - MagnetArea.MAGNET_SIZE) && (x <= centerX + MagnetArea.MAGNET_SIZE);
	}

	/**
	 * 특정 위치에 있는 블록을 레인에서 제거합니다.
	 * @param index 제거할 블록의 위치
	 * @param runListeners 크기 변경 리스너를 실행할 지 여부. <code>true</code>면 리스너를 실행합니다.
	 */
	private void detachBlockAt(int index, boolean runListeners) {
		CodeBlock block = blocks.remove(index);
		program.setOrdered(block, false);
		recalculateMagnetArea(index > 0? index-1 : 0);
		
		boolean wChanged = recalculateWidth(false);
		this.height = height - block.getHeight();
		if(runListeners && (wChanged || block.getHeight() != 0)) {
			runResizeListeners();
		}
	}

	/**
	 * 레인에 놓인 블록들을 재배치합니다.<br>
	 * 블록을 배치하면서 레인의 너비와 높이도 다시 계산합니다. 단, 크기 변경 리스너는 호출하지 않습니다.
	 * @return 레인의 크기가 변경되었다면 <code>true</code>를 반환합니다.
	 */
	protected boolean reorderBlocks(){
		if(visible) {
			int nextY = startY + BLOCK_MARGIN_SIZE;
			
			ProgrammingPanel panel = program.getPanel();
			
			panel.clearLines(this);
			panel.addLine(this, new Point(getCenterX(), getStartY()), new Point(getCenterX(), getStartY() + BLOCK_MARGIN_SIZE));
			
			int lastWidth = width, lastHeight = height;
			
			int maxWidth = 0;
			for(CodeBlock block : blocks){
				Point loc = new Point(centerX - block.getWidth()/2, nextY);
				
				block.setLocation(loc);
				nextY += block.getHeight() + BLOCK_MARGIN_SIZE;
				panel.addLine(this, new Point(centerX, nextY - BLOCK_MARGIN_SIZE), new Point(centerX, nextY));
				
				if(block.getWidth() > maxWidth) {
					maxWidth = block.getWidth();
				}
			}
			
			this.width = maxWidth;
			this.height = nextY - startY;
			
			recalculateMagnetArea();
			
			panel.repaint();
			
			if(lastHeight != height || lastWidth != width) {
				return true;
			} else {
				return false;
			}
		} else {
			for(CodeBlock block : blocks) {
				block.setVisible(false);
			}
			
			if(this.height != 0 || this.width != 0) {
				this.height = 0;
				this.width = 0;
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * 레인의 높이를 다시 계산합니다.
	 * @param runListeners 등록된 {@link LaneResizeListener}들을 실행할 지 여부
	 * @return 너비가 바뀌었다면 <code>true</code>
	 * @see #getHeight()
	 * @see #refreshLayout()
	 */
	protected boolean recalculateHeight(boolean runListeners) {
		if(visible) {
			int lastHeight = this.height;
			
			this.height = BLOCK_MARGIN_SIZE;
			for(CodeBlock block : blocks){
				this.height += block.getHeight() + BLOCK_MARGIN_SIZE;
			}
			
			if(this.height != lastHeight) {
				if(runListeners) {
					runResizeListeners();
				}
				return true;
			} else {
				return false;
			}
		} else {
			if(this.height != 0) {
				reorderBlocks();
				
				if(runListeners) {
					runResizeListeners();
				}
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * 내부에 있는 블록의 크기가 변경되는 것을 감지하는 리스너입니다.<br>
	 * 내부 블록의 크기에 따라 레인의 배치도 변경됩니다.
	 * @author WKBae
	 *
	 */
	private class InnerBlockResizeListener implements BlockResizeListener {
		
		@Override
		public void onBlockResize(BaseBlockModel block, int width, int height) {
			refreshLayout();
		}
	}
	
	/**
	 * 레인의 정보입니다.<br>
	 * 레인을 복구시킬 때 사용합니다.
	 * @author WKBae
	 *
	 */
	public static class LaneInfo {
		private LaneInfo(int laneId, int centerX, int startY, LinkedList<CodeBlock> blocks) {
			this.laneId = laneId;
			this.centerX = centerX;
			this.startY = startY;
			this.blocks = blocks;
		}
		public final int laneId;
		public final int centerX, startY;
		public final LinkedList<CodeBlock> blocks;
	}
	
	/**
	 * 레인에 블록을 붙인 정보입니다.<br>
	 * 붙인 블록을 되돌릴 때 사용합니다.
	 * @author WKBae
	 *
	 */
	private class AttachInfo {
		private LinkedList<CodeBlock> originalBlocks;
		private Point originalBlockLoc;
		private CodeBlock block;
		
		@SuppressWarnings("unchecked")
		public AttachInfo(CodeBlock block) {
			originalBlocks = (LinkedList<CodeBlock>) getBlocks().clone();
			this.block = block;
			originalBlockLoc = block.getLocation();
		}
		
		public LinkedList<CodeBlock> getOriginalBlocks() {
			return originalBlocks;
		}
		public CodeBlock getBlock() {
			return block;
		}
		public Point getOriginalBlockLocation() {
			return originalBlockLoc;
		}
	}
	
	/**
	 * 레인에 블록을 떼어낸 정보입니다.<br>
	 * 떼어낸 블록을 되돌릴 때 사용합니다.
	 * @author WKBae
	 *
	 */
	private class DetachInfo {
		private LinkedList<CodeBlock> originalBlocks;
		private CodeBlock block;
		
		@SuppressWarnings("unchecked")
		public DetachInfo(CodeBlock block) {
			originalBlocks = (LinkedList<CodeBlock>) getBlocks().clone();
			this.block = block;
		}
		
		public LinkedList<CodeBlock> getOriginalBlocks() {
			return originalBlocks;
		}
		public CodeBlock getBlock() {
			return block;
		}
	}
	
	/**
	 * 블록을 붙이는 작업입니다.
	 * @author WKBae
	 *
	 */
	private class AttachBlockJob extends Job {
		
		private CodeBlock block;
		private int order;
		
		private AttachBlockJob(CodeBlock block, int order) {
			this.block = block;
			this.order = order;
		}
		
		@Override
		protected void execute() {
			if(order >= blocks.size() || order == -1){ // 배치할 위치가 배열되어있는 블록의 수보다 크거나 -1인 경우 
				blocks.add(block);
				reorderBlocks();
				recalculateMagnetArea(magnetAreas.size()-1);
			}else{
				blocks.add(order, block);
				reorderBlocks();
				recalculateMagnetArea(order);
			}
			//block.setOrderedInfo(new OrderedInfo(Lane.this, Lane.this.magnetAreas.get(order)));
			
			if(blocks.size() != 1){ // 제일 위쪽의 블록이 아니면
				height += BLOCK_MARGIN_SIZE;
			}
			height += block.getHeight();
			if(width < block.getWidth()){
				width = block.getWidth();
			}
			
			program.setOrdered(block, true);
			InnerBlockResizeListener listener = new InnerBlockResizeListener();
			blockResizeListeners.put(block, listener);
			block.addResizeListener(listener);
			
			runResizeListeners();
		}
	}
	
	/**
	 * 블록을 때어내는 작업입니다.
	 * @author WKBae
	 *
	 */
	private class DetachBlockJob extends Job {
		
		private CodeBlock block;
		private DetachBlockJob(CodeBlock block){
			this.block = block;
		}
		
		@Override
		protected void execute() {
			int location = blocks.indexOf(block);
			if(location != -1){
				detachBlockAt(location, false);
				
				program.setOrdered(block, false);
				block.removeResizeListener(blockResizeListeners.remove(block));
				
				reorderBlocks();
				
				runResizeListeners();
			}
		}
	}
	
}
