package net.wkbae.quickprogrammer;

/**
 * 프로그램을 실행할 작업 클래스입니다.<br>
 * 되돌릴 수 있는 모든 작업은 이 클래스를 확장하여 {@link JobManager#doJob(Job)}을 이용해 실행해야 합니다.<br>
 * 작업들은 연결 리스트(Linked List)의 형태로 보관됩니다.
 * @author WKBae
 * @see JobManager
 */
abstract class Job {
	
	private Job nextJob;
	private Job prevJob;
	
	/**
	 * 이 작업의 다음 작업을 설정합니다.
	 * @param nextJob 설정할 다음 작업
	 */
	final void setNextJob(Job nextJob) {
		this.nextJob = nextJob;
	}
	/**
	 * 이 작업의 이전 작업을 설정합니다.
	 * @param prevJob 설정할 이전 작업
	 */
	final void setPreviousJob(Job prevJob) {
		this.prevJob = prevJob;
	}
	
	/**
	 * 이 작업의 다음 작업을 구합니다.<br>
	 * 설정된 작업이 없다면 <code>null</code>을 반환합니다.
	 * @return 다음 작업, 없다면 <code>null</code>
	 */
	final Job getNextJob() {
		return nextJob;
	}
	
	/**
	 * 이 작업의 이전 작업을 구합니다.<br>
	 * 설정된 작업이 없다면 <code>null</code>을 반환합니다.
	 * @return 이전 작업, 없다면 <code>null</code>
	 */
	final Job getPreviousJob() {
		return prevJob;
	}
	
	/**
	 * 작업을 실행합니다.<br>
	 * 이 메소드는 사용해서는 안됩니다. {@link JobManager#doJob(Job)}을 이용해 이 작업을 전달하면 작업 매니저가 이 메소드를 호출합니다.
	 */
	protected abstract void execute();
}