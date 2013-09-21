package net.wkbae.quickprogrammer;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.wkbae.quickprogrammer.JobManager.JobListener;
import net.wkbae.quickprogrammer.ProgrammingFrame.ProgrammingPanel;
import net.wkbae.quickprogrammer.listener.LaneResizeListener;
import net.wkbae.quickprogrammer.runner.ProgramRunner;

/**
 * 현재 프로그램에 대한 모든 정보를 저장하고 있습니다.<br>
 * 모든 프로그램 창은 이 클래스를 하나씩 가지고 있습니다.
 * @author WKBae
 */
public final class Program {
	
	private final ProgrammingPanel panel;
	private Lanes lanes;
	private JobManager jobManager;
	private Variables variables;
	private VariableSockets variableSockets;
	
	private FileChangeListener changeListener;
	
	/**
	 * 나열되지 않은 블록들을 저장합니다.
	 */
	private ArrayList<CodeBlock> unorderedBlock = new ArrayList<CodeBlock>();
	private ArrayList<CodeBlock> allBlocks;
	
	private HashMap<CodeBlock, Float> locationRatioX = new HashMap<CodeBlock, Float>();

	private HashMap<CodeBlock, Float> locationRatioY = new HashMap<CodeBlock, Float>();

	private int previousW;

	private int previousH;

	private int currentW;

	private int currentH;

	/**
	 * 프로그램을 생성합니다.
	 * @param panel
	 */
	Program(ProgrammingPanel panel) {
		this.panel = panel;
		
		initialize();
		this.jobManager = new JobManager(this);
		changeListener = new FileChangeListener();
		jobManager.addJobListener(changeListener);
		
		panel.addComponentListener(new ProgramFrameResizeListener());
	}
	
	/**
	 * 프로그램을 초기화합니다.<br>
	 * 대부분의 경우 실행해서는 안됩니다.
	 */
	void initialize() {
		this.lanes = new Lanes(this);
		this.variables = new Variables();
		this.variableSockets = new VariableSockets(this);
		this.unorderedBlock =  new ArrayList<>();
		this.allBlocks = new ArrayList<>();
		
		lanes.getMainLane().addResizeListener(new MainLaneResizeListener());
	}
	
	/**
	 * 프로그램의 패널을 얻습니다.<br>
	 * 패널은 코드 블록들이 표시되는 장소입니다.
	 * @return 이 프로그램의 패널
	 */
	public ProgrammingPanel getPanel() {
		return panel;
	}
	
	/**
	 * 프로그램의 레인들을 구합니다.<br>
	 * 모든 레인은 이 {@link Lanes} 객체를 이용하여 구합니다.
	 * @return 이 프로그램의 {@link Lanes}
	 */
	public Lanes getLanes() {
		return lanes;
	}
	
	/**
	 * 이 프로그램의 작업 관리자를 구합니다.<br>
	 * 프로그램에서 실행되는 모든 작업을 관리합니다.<br>
	 * 외부에서 접근하면 위험성이 있을 수 있으므로 <code>default</code> 필드로 정의되었습니다.
	 * @return 이 프로그램의 작업 관리자
	 */
	JobManager getJobManager() {
		return jobManager;
	}
	
	/**
	 * 프로그램의 변수들을 구합니다.<br>
	 * 프로그램에서 정의된 모든 변수는 이 {@link Variables} 객체를 이용하여 구합니다. 단, 변수는 프로그램 실행 중({@link BaseBlockModel#execute()})에만 관리해야 합니다.
	 * @return 이 프로그램의 변수들
	 */
	public Variables getVariables() {
		return variables;
	}
	
	/**
	 * 이 프로그램의 변수 소켓들을 구합니다.
	 * @return 이 프로그램의 변수 소켓들
	 */
	public VariableSockets getVariableSockets() {
		return variableSockets;
	}
	
	/**
	 * 코드 블록 추가합니다.<br>
	 * 매개 변수 initialize가 true이고 {@link BaseBlockModel#initialize()}에서 false가 반환할 경우 블록이 추가되지 않습니다.
	 * @param block 추가할 블록
	 * @param initialize {@link BaseBlockModel#initialize()}를 실행할 지 여부, 저장된 파일을 불러올 때에만 false로 하는 것이 좋습니다.<br>
	 * <table border="1">
	 * <tr><th><code>initialize</code></th><th>{@link BaseBlockModel#initialize()}의 반환값</th><th>추가 여부</th></tr>
	 * <tr><td rowspan="2"><code>true</code></td><td><code>true</code></td><td>추가함</td></tr>
	 * <tr><td><code>false</code></td><td>추가 안함</td></tr>
	 * <tr><td><code>false</code></td><td>실행 안함</td><td>추가함</td></tr>
	 * </table>
	 */
	public boolean addBlock(CodeBlock block, boolean initialize){
		if(initialize && !block.getModel().initialize()){
			return false;
		}
		
		getJobManager().startMultipleJobs();
		getJobManager().doJob(new AddBlockJob(block));
		getJobManager().finishMultipleJobs();
		
		return true;
	}
	
	/**
	 * 코드 블록을 추가합니다.<br>
	 * 추가할 때, {@link BaseBlockModel#initialize()}를 실행합니다.<br>
	 * 이 메소드는 <code>addBlock(block, true)</code>와 동일합니다.
	 * @param block 추가할 블록
	 */
	public boolean addBlock(CodeBlock block){
		return addBlock(block, true);
	}
	
	/**
	 * 코드 블록을 제거합니다.
	 * @param block 제거할 블록
	 */
	public void removeBlock(CodeBlock block){
		getJobManager().startMultipleJobs();
		getJobManager().doJob(new RemoveBlockJob(block));
		getJobManager().finishMultipleJobs();
	}

	/**
	 * 나열되지 않은 블록들을 받는 메소드입니다.<br>
	 * 반환값은 변경할 수 없는 리스트로, 읽는 것만 가능합니다.
	 * @return {@link Collections#unmodifiableList(List)}를 이용하여 바꿀 수 없도록 처리된 블록 리스트
	 */
	public List<CodeBlock> getUnorderedBlocks() {
		return Collections.unmodifiableList(unorderedBlock);
	}
	
	/**
	 * 프로그램을 실행시킵니다.<br>
	 * 이 메소드는 {@link ProgramRunner#createRunner(Program) ProgramRunner.createRunner(this)}{@link ProgramRunner#run() .run()}을 통해 실행됩니다.
	 */
	public void execute() {
		ProgramRunner.createRunner(this).run();
	}

	/**
	 * 블록이 나열되지 않은 상태인지 설정합니다.
	 * @param block 설정할 블록
	 * @param ordered <code>true</code>라면 나열되지 않은 상태
	 */
	void setOrdered(CodeBlock block, boolean ordered) {
		if(ordered) {
			unorderedBlock.remove(block);
		} else {
			unorderedBlock.add(block);
		}
	}
	
	/**
	 * 패널에 표현될 블록의 위치 비율을 제거합니다.(블록의 이동 등으로 비율이 바뀐 경우)<br>
	 * 제거된 위치 비는 다음 패널 리사이즈 시에 다시 계산됩니다.
	 */
	void resetLocationRatio(CodeBlock block) {
		locationRatioX.remove(block);
	}
	
	/**
	 * 나열되지 않은 블록들을 재배치하는 메소드입니다.<br>
	 * 블록들은 원래 놓여있던 위치에 비례해서 이동됩니다.
	 */
	private void reallocateUnorderedBlocks(){
		for(CodeBlock block : unorderedBlock){
			
			if(!locationRatioX.containsKey(block) || !locationRatioY.containsKey(block)){
				locationRatioX.put(block, block.getX() / (float)previousW);
				locationRatioY.put(block, block.getY() / (float)previousH);
			}
			int x = Math.round(locationRatioX.get(block) * currentW);
			int y = Math.round(locationRatioY.get(block) * currentH);
			
			block.setLocation(x, y);
		}
	}
	
	/**
	 * 프로그램이 바뀐 지점을 리셋합니다.<br>
	 * 저장된 것처럼 표시되므로 필요할 때에만 호출해야 합니다.
	 */
	void setSaved() {
		getJobManager().markSaved();
	}
	
	/**
	 * 현재 상태가 프로그램이 저장된 지점에서 바뀌었는지 확인합니다.
	 * @return 프로그램이 바뀌었다면 <code>true</code>를 반환합니다.
	 */
	public boolean isChangedFromSave() {
		return getJobManager().isChangedFromSave();
	}

	@SuppressWarnings("unchecked")
	List<CodeBlock> getAllBlocks() {
		return (List<CodeBlock>) allBlocks.clone();
	}
	
	void resetMainLaneLocation() {
		getLanes().getMainLane().setCenterX(currentW/2);
		getLanes().getMainLane().refreshLayout();
		
	}
	
	/**
	 * 프로그램이 변경된 것을 감지하는 리스너입니다.<br>
	 * 저장된 상태의 기준점이 있으며, 작업({@link Job})이 실행되거나 되돌려질 때 기준점으로부터의 거리를 저장합니다. 거리가 0이라면 저장된 것으로 간주됩니다.
	 * @author WKBae
	 */
	private class FileChangeListener implements JobListener {
		
		@Override
		public void jobDone(Job job) {
			ProgrammingFrame frame = getPanel().getFrame();
			if(getJobManager().isChangedFromSave()) {
				if(!frame.getTitle().startsWith("* ")) {
					frame.setTitle("* " + frame.getDefaultTitle());
				}
			} else {
				frame.setTitle(frame.getDefaultTitle());
			}
		}
		
		@Override
		public void jobUndone(Job job) {
			ProgrammingFrame frame = getPanel().getFrame();
			if(getJobManager().isChangedFromSave()) {
				if(!frame.getTitle().startsWith("* ")) {
					frame.setTitle("* " + frame.getDefaultTitle());
				}
			} else {
				frame.setTitle(frame.getDefaultTitle());
			}
		}
	}
	
	/**
	 * 메인 레인의 크기가 바뀌는것을 감지하는 리스너입니다.<br>
	 * 프로그램 패널의 크기를 이 메인 레인의 크기로 맞춥니다.
	 * @author WKBae
	 */
	private class MainLaneResizeListener implements LaneResizeListener {
		@Override
		public void laneResized(Lane lane, int width, int height) {
			getPanel().getFrame().revalidate();
		}
	}
	
	/**
	 * 창의 크기가 변경되는 것을 감지하는 리스너입니다.<br>
	 * 메인 레인의 위치를 창의 중간으로 옮기고, 나열되지 않은 블록들을 다시 배치합니다.({@link Program#reallocateUnorderedBlocks()})
	 * @author WKBae
	 * @see Program#reallocateUnorderedBlocks()
	 */
	private class ProgramFrameResizeListener extends ComponentAdapter {
		@Override
		public void componentResized(ComponentEvent e) {
			previousW = currentW;
			previousH = currentH;
			currentW = e.getComponent().getWidth();
			currentH = e.getComponent().getHeight();
			
			getLanes().getMainLane().setCenterX(currentW/2);
			
			reallocateUnorderedBlocks();
			
			for(Lane lane : getLanes().getAllLanes()){
				lane.refreshLayout();
			}
		}
	}
	
	/**
	 * 블록을 추가하는 작업입니다.
	 * @author WKBae
	 *
	 */
	private class AddBlockJob extends Job {
		
		private CodeBlock block;
		
		public AddBlockJob(CodeBlock block) {
			this.block = block;
		}
		
		@Override
		protected void execute() {
			panel.add(block, -1);
			
			Rectangle rct = panel.getVisibleRect();
			Point loc = rct.getLocation();
			loc.translate(block.getWidth(), block.getHeight());
			if(panel.getComponentAt(loc.x - 1, loc.y - 1) != null) {
				while(panel.getComponentAt(loc.x - 1, loc.y - 1) != panel) {
					loc.translate(5, 5);
				}
			}
			block.setLocation(loc.x - block.getWidth(), loc.y - block.getHeight());
			
			allBlocks.add(block);
			unorderedBlock.add(block);
			
			block.setVisible(true);
			
			panel.repaint();
		}
	}
	
	/**
	 * 블록을 제거하는 작업입니다.
	 * @author WKBae
	 *
	 */
	private class RemoveBlockJob extends Job {
		
		private CodeBlock block;
		
		public RemoveBlockJob(CodeBlock block) {
			this.block = block;
		}
		
		@Override
		protected void execute() {
			block.getModel().onBlockRemove();
			
			unorderedBlock.remove(block);
			allBlocks.remove(block);
			panel.remove(block);
			panel.repaint();
		}
	}
}
