package net.wkbae.quickprogrammer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

import net.wkbae.quickprogrammer.Lane.LaneInfo;
import net.wkbae.quickprogrammer.VariableSocket.SocketInfo;
import net.wkbae.quickprogrammer.file.parser.ParseException;
import net.wkbae.util.ErrorDialog;

/**
 * 실행한 작업들을 나열합니다.<br>
 * undo/redo를 구현하기 위해 사용합니다.<br>
 * 작업을 되돌릴 때에는 이전에 생성한 스냅샷으로 돌아가서, 현재 지점과 스냅샷 사이에 실행된 작업을 최근 하나만 제외하고 다시 실행합니다.
 * @author WKBae
 */
final class JobManager {
	
	private Snapshot firstJob = null;
	private Job currentJob = null;
	private Job lastJob = null;
	private TreeMap<Integer, Snapshot> snapshots = new TreeMap<>();
	private int currentIndex = 0;
	private int lastIndex = 0;
	
	private int savedIndex = 0;
	
	private Program program;
	
	/**
	 * 작업 매니저를 생성합니다.<br>
	 * 이 생성자는 {@link Program}에서만 실행되어야 합니다.
	 * @param program
	 */
	JobManager(Program program){
		this.program = program;
		resetSnapshots();
	}
	
	private int multipleJobCalled = 0;
	private Stack<Job> multipleJobs = null;
	/**
	 * 다중 작업을 시작합니다.<br>
	 * 다중 작업은 여러 개의 작업을 하나로 묶은 작업으로, 되돌릴 때 하나의 작업으로 처리됩니다.<br>
	 * 이 메소드를 실행시키고 {@link #finishMultipleJobs()}를 실행할 때까지 {@link #doJob(Job)}을 통해 실행시킨 모든 작업은 {@link MultipleJobs}로 저장됩니다.<br>
	 * 이 메소드를 호출하면 반드시 {@link #finishMultipleJobs()}도 실행해야 합니다.
	 * @see #finishMultipleJobs()
	 * @see MultipleJobs
	 */
	public void startMultipleJobs(){
		if(ignoreJob > 0) {
			return;
		}
		
		multipleJobCalled++;
		if(multipleJobCalled == 1) {
			multipleJobs = new Stack<Job>();
		}
	}
	
	/**
	 * 작업을 실행합니다.<br>
	 * 작업을 실행하기 위해서는 이 메소드를 사용해야 합니다.
	 * @param job 실행시킬 작업
	 */
	public synchronized void doJob(Job job){
		if(ignoreJob > 0) {
			job.execute();
			
			for(JobListener listener : listeners) {
				listener.jobDone(job);
			}
			return;
		}
		
		if(multipleJobs != null){
			multipleJobs.push(job);
			
			job.execute();
		}else{
			job.setPreviousJob(currentJob);
			currentJob.setNextJob(job);
			currentJob = job;
			lastJob = job;
			
			currentIndex++;
			lastIndex = currentIndex;

			Integer snapIdx;
			while((snapIdx = snapshots.ceilingKey(currentIndex)) != null) {
				snapshots.remove(snapIdx);
			}
			
			job.execute();
			
			for(JobListener listener : listeners) {
				listener.jobDone(job);
			}
		}
	}
	
	/**
	 * 다중 작업을 종료합니다.<br>
	 * {@link #startMultipleJobs()} 메소드를 실행하고 이 메소드를 호출하면 다중 작업이 종료되면서 생성된 {@link MultipleJobs}를 반환합니다.
	 * @return 생성된 {@link MultipleJobs}
	 */
	public MultipleJobs finishMultipleJobs(){
		if(ignoreJob > 0) {
			return null;
		}
		
		if(multipleJobs != null) {
			multipleJobCalled--;
			if(multipleJobCalled == 0) {
				MultipleJobs multiJob = new MultipleJobs(multipleJobs);
				
				currentJob.setNextJob(multiJob);
				multiJob.setPreviousJob(currentJob);
				currentJob = multiJob;
				lastJob = multiJob;
				currentIndex++;
				lastIndex = currentIndex;

				Integer snapIdx;
				while((snapIdx = snapshots.ceilingKey(currentIndex)) != null) {
					snapshots.remove(snapIdx);
				}
				
				multipleJobs = null;
				
				for(JobListener listener : listeners) {
					listener.jobDone(multiJob);
				}
				return multiJob;
			}
		}
		return null;
	}
	
	private int ignoreJob = 0;
	/**
	 * 이 메소드 이후로 실행된 작업들을 무시합니다.<br>
	 * 반드시 제한된 조건 내에서만 실행되어야 합니다.
	 */
	private void startIgnoreJob() {
		ignoreJob++;
	}
	/**
	 * 이 메소드까지 작업들을 무시합니다.
	 */
	private void finishIgnoreJob() {
		ignoreJob--;
	}
	
	/**
	 * 마지막 작업을 되돌립니다.<br>
	 * 최근의 스냅샷으로 복귀시킨 뒤, 사이에 실행되었던 작업을 하나만 제외하고 모두 다시 실행합니다.
	 */
	public synchronized void undo() {
		if(canUndo()) {
			Entry<Integer, Snapshot> lastSnapEntry = snapshots.lowerEntry(currentIndex);
			
			int diff = currentIndex - 1 - lastSnapEntry.getKey();
			if(snapshots.containsKey(currentIndex)) diff--; // 0--1-2 => 0--1 (마지막이 스냅샷이면 하나 더 제거)
			
			startIgnoreJob();
			Job job = lastSnapEntry.getValue();
			
			for(int i = 0; i <= diff; i++) {
				job.execute();
				job = job.getNextJob();
			}
			currentJob = job.getPreviousJob();
			currentIndex--;
			finishIgnoreJob();
			
			for(JobListener listener : listeners) {
				listener.jobUndone(job);
			}
		}
	}
	
	/**
	 * 되돌린 작업을 다시 실행합니다.
	 */
	public synchronized void redo() {
		if(canRedo()) {
			currentJob = currentJob.getNextJob();
			currentJob.execute();
			currentIndex++;
			
			for(JobListener listener : listeners) {
				listener.jobDone(currentJob);
			}
		}
	}
	
	/**
	 * 되돌릴 수 있는지 확인합니다.
	 * @return 현재 작업의 위치가 저장된 위치보다 크다면 <code>true</code>를 반환합니다.
	 */
	public boolean canUndo() {
		return currentIndex > 0 && currentJob.getPreviousJob() != null;
	}
	
	/**
	 * 되돌린 작업을 다시 실행할 수 있는지(다시 실행할 작업이 있는지) 확인합니다.
	 * @return 현재 작업의 위치가 마지막 위치보다 작다면 <code>true</code>를 반환합니다.
	 */
	public boolean canRedo() {
		return currentIndex < lastIndex && currentJob.getNextJob() != null;
	}
	
	/**
	 * 작업을 제거합니다.<br>
	 * 필요할 때에만 사용되어야 합니다.
	 * @param job 제거할 작업
	 */
	public void removeJob(Job job){
		if(job.getPreviousJob() != null) {
			int matchIndex = lastIndex;
			for(Job j = lastJob; j != firstJob; j = j.getPreviousJob()) {
				matchIndex--;
				if(j == job) break;
			}
			if(matchIndex > 0) {
				job.getPreviousJob().setNextJob(job.getNextJob());
				if(job.getNextJob() != null) {
					job.getNextJob().setPreviousJob(job.getPreviousJob());
				}
			}
		} else if(multipleJobs != null) {
			if(multipleJobs.remove(job)) {
			}
		}
	}
	
	/**
	 * 상태를 초기화합니다.<br>
	 * 제일 처음 스냅샷(아무 것도 없는 상태)로 되돌아갑니다.
	 */
	void clearJobs() {
		currentJob = firstJob;
		lastJob = firstJob;
		currentIndex = 0;
		lastIndex = 0;
		snapshots.clear();
		snapshots.put(0, firstJob);
	}
	
	/**
	 * 모든 상태를 초기화합니다.<br>
	 * 첫 번째 스냅샷도 다시 구합니다. 즉, 최대 이 메소드를 호출한 상태까지만 돌아갑니다.
	 */
	void resetSnapshots() {
		firstJob = new Snapshot();
		currentJob = firstJob;
		lastJob = firstJob;
		currentIndex = 0;
		lastIndex = 0;
		savedIndex = 0;
		snapshots.clear();
		snapshots.put(0, firstJob);
	}
	
	/**
	 * 저장된 지점을 표시합니다.
	 * @see #isChangedFromSave()
	 */
	void markSaved() {
		this.savedIndex = currentIndex;
	}
	
	/**
	 * 현재 상태가 저장된 위치인지 확인합니다.
	 * @return 저장된 위치와 같다면 <code>true</code>를 반환합니다.
	 */
	boolean isChangedFromSave() {
		return savedIndex != currentIndex;
	}
	
	/**
	 * 현재 상태의 스냅샷을 생성합니다.<br>
	 * 블록 생성({@link BaseBlockModel#initialize()}), 데이터 입력({@link BaseBlockModel#onDoubleClick()})이 일어났을 때 저장합니다.
	 */
	void createSnapshot() {
		if(ignoreJob > 0) {
			return;
		}
		
		Snapshot snapshot = new Snapshot();
		currentJob.setNextJob(snapshot);
		snapshot.setPreviousJob(currentJob);
		
		currentJob = snapshot;
		lastJob = snapshot;
		currentIndex++;
		lastIndex = currentIndex;
		
		snapshots.put(currentIndex, snapshot);
		
		Integer snapIdx;
		while((snapIdx = snapshots.ceilingKey(currentIndex + 1)) != null) {
			snapshots.remove(snapIdx);
		}
		
	}
	
	private ArrayList<JobListener> listeners = new ArrayList<>();
	/**
	 * 작업 리스너를 추가합니다.<br>
	 * 리스너는 작업 실행/실행 취소시 호출됩니다.
	 * @param listener 추가할 리스너
	 */
	public void addJobListener(JobListener listener) {
		listeners.add(listener);
	}
	/**
	 * 작업 리스너를 제거합니다.
	 * @param listener 제거할 리스너
	 */
	public void removeJobListener(JobListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * 작업 리스너입니다.<br>
	 * 리스너는 작업 실행, 실행 취소 시 호출됩니다.
	 * @author WKBae
	 * @see JobManager#addJobListener(JobListener)
	 * @see JobManager#removeJobListener(JobListener)
	 */
	public static interface JobListener {
		/**
		 * 작업을 실행하거나 되돌리기 취소(Redo)시 호출됩니다.
		 * @param job 실행된 작업
		 */
		public void jobDone(Job job);
		/**
		 * 작업을 실행 취소할 때 호출됩니다.
		 * @param job 되돌린(취소된) 작업
		 */
		public void jobUndone(Job job);
	}
	
	/**
	 * 스냅샷입니다. 프로그램의 모든 상태를 저장합니다.
	 * @author WKBae
	 * @see JobManager#createSnapshot()
	 */
	private class Snapshot extends Job {
		
		private HashMap<CodeBlock, HashMap<String, String>> blockData = new HashMap<>();
		private ArrayList<LaneInfo> laneInfos = new ArrayList<>();
		private ArrayList<SocketInfo> socketInfos = new ArrayList<>();
		
		/**
		 * 스냅샷을 생성합니다.<br>
		 * 이 생성자를 호출한 시점에서 모든 정보를 저장합니다.
		 */
		public Snapshot() {
			for(Lane lane : program.getLanes().getAllLanes()) {
				laneInfos.add(lane.getLaneInfo());
			}
			
			for(VariableSocket socket : program.getVariableSockets().getAllSockets()) {
				socketInfos.add(socket.getSocketInfo());
			}
			
			for(CodeBlock block : program.getAllBlocks()) {
				HashMap<String, String> data = new HashMap<>();
				block.getModel().saveState(data);
				blockData.put(block, data);
			}
		}
		
		/**
		 * 현재 상태를 스냅샷으로 복귀시킵니다.<br>
		 * 모든 {@link Lane 레인}, {@link VariableSocket 소켓}, {@link CodeBlock 블록}들을 복구시킵니다.
		 */
		@Override
		protected void execute() {
			startIgnoreJob();
			
			program.getPanel().removeAll();
			program.initialize();
			
			TreeMap<Integer, Lane> lanes = new TreeMap<>();
			for(LaneInfo info : laneInfos) {
				Lane lane = new Lane(program, info.laneId, info.blocks);
				lane.setLocation(info.centerX, info.startY);
				lanes.put(info.laneId, lane);
			}
			program.getLanes().setLanes(lanes);
			program.resetMainLaneLocation();
			
			TreeMap<Integer, VariableSocket> sockets = new TreeMap<>();
			for(SocketInfo info : socketInfos) {
				VariableSocket socket = new VariableSocket(program, info.socketId);
				socket.setLocation(info.startX, info.centerY);
				socket.attachBlock(info.block);
				sockets.put(info.socketId, socket);
			}
			program.getVariableSockets().setSockets(sockets);
			
			for(Entry<CodeBlock, HashMap<String, String>> entry : blockData.entrySet()) {
				program.addBlock(entry.getKey(), false);
				try {
					entry.getKey().getModel().restoreState(entry.getValue());
				} catch (ParseException e) {
					new ErrorDialog(e);
				}
			}
			program.getPanel().repaint();
			
			finishIgnoreJob();
		}
	}
}
