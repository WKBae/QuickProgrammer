package net.wkbae.quickprogrammer;

import java.util.Map;
import java.util.TreeMap;

/**
 * 프로그램의 레인들을 관리하는 클래스입니다.
 * @author WKBae
 */
public final class Lanes {
	
	private TreeMap<Integer, Lane> lanes;
	
	private Program program;
	
	/**
	 * 레인을 생성합니다.<br>
	 * 자동으로 메인 레인(ID 0번)을 생성합니다.
	 * @param program
	 */
	Lanes(Program program){
		lanes = new TreeMap<Integer, Lane>();
		
		this.program = program;
		
		Lane mainLane = new Lane(program, 0);
		mainLane.setCenterX(program.getPanel().getWidth() / 2);
		mainLane.setStartY(0);
		lanes.put(0, mainLane);
	}
	
	/**
	 * 레인들을 설정합니다.
	 * @param lanes 설정할 레인들
	 */
	void setLanes(Map<Integer, Lane> lanes) {
		program.getPanel().clearAllLines();
		this.lanes = new TreeMap<>(lanes);
	}
	
	/**
	 * 레인을 생성합니다.<br>
	 * ID는 자동으로 생성합니다.
	 * @return 생성된 레인의 ID
	 */
	public int createLane(){
		int id;
		for(id = 0; lanes.containsKey(id); id++);
		if(!createLane(id)) {
			throw new IllegalStateException("레인을 생성할 수 없습니다.");
		}
		return id;
	}
	
	/**
	 * 레인을 생성합니다.<br>
	 * 설정한 ID로 생성합니다.
	 * @param laneId 생성할 레인의 ID
	 * @return 생성되었다면 <code>true</code>, 이미 존재하면 <code>false</code>
	 */
	public boolean createLane(int laneId){
		if(lanes.containsKey(laneId)){
			return false;
		}
		program.getJobManager().doJob(new CreateLaneJob(laneId));
		//lanes.put(laneId, new Lane(program, laneId));
		return true;
	}
	
	/**
	 * 레인을 제거합니다.
	 * @param laneId 제거할 레인의 ID
	 */
	public void removeLane(int laneId){
		if(laneId == 0){
			throw new IllegalArgumentException("메인 레인은 지울 수 없습니다.");
		}
		program.getJobManager().doJob(new RemoveLaneJob(laneId));
	}
	
	/**
	 * 모든 레인을 구합니다.
	 * @return 모든 레인들
	 */
	public Lane[] getAllLanes(){
		return lanes.values().toArray(new Lane[0]);
	}
	
	/**
	 * 메인 레인(ID 0번)을 구합니다.
	 * @return 메인 레인
	 */
	public Lane getMainLane(){
		return getLane(0);
	}
	
	/**
	 * 주어진 ID의 레인을 구합니다.
	 * @param laneId 구할 레인의 ID
	 * @return 레인, 없다면 <code>null</code>
	 */
	public Lane getLane(int laneId){
		return lanes.get(laneId);
	}
	
	/**
	 * 레인을 생성하는 작업입니다.
	 * @author WKBae
	 *
	 */
	private class CreateLaneJob extends Job {
		private int laneId;
		
		public CreateLaneJob(int laneId) {
			this.laneId = laneId;
		}
		@Override
		protected void execute() {
			lanes.put(laneId, new Lane(program, laneId));
		}
	}
	
	/**
	 * 레인을 제거하는 작업입니다.
	 * @author WKBae
	 *
	 */
	private class RemoveLaneJob extends Job {
		private int laneId;
		private Lane lane;
		
		public RemoveLaneJob(int laneId) {
			this.laneId = laneId;
		}
		@Override
		protected void execute() {
			lane = lanes.remove(laneId);
			if(lane != null) {
				program.getPanel().removeLines(lane);
			}
		}
	}
}
