package net.wkbae.quickprogrammer.runner;

import javax.swing.SwingUtilities;

import net.wkbae.quickprogrammer.Program;
import net.wkbae.quickprogrammer.ProgrammingFrame;
import net.wkbae.util.ErrorDialog;

/**
 * 프로그램을 실행하는 클래스입니다. {@link #run()}을 호출하면 새로운 스레드에서 실행됩니다.
 * @author WKBae
 *
 */
public class ProgramRunner {
	
	/**
	 * 새로운 프로그램 스레드를 생성합니다.<br>
	 * 메인 레인에 배치된 순서대로 실행합니다. 프로그램 스레드가 생성되더라도 {@link ProgrammingFrame.ProgrammingPanel}의 내용이 바뀌면 바뀐 내용대로 실행합니다.
	 * @return 생성된 프로그램 스레드를 반환합니다. {@link ProgramThreadGroup#start()} 메소드로 실행시킬 수 있습니다.
	 */
	public static ProgramRunner createRunner(Program program) {
		return new ProgramRunner(program);
	}
	
	private final Program program;
	private final ProgramThreadGroup group;
	private ProgramRunner(Program program) {
		this.program = program;
		this.group = new ProgramThreadGroup(program);
	}
	
	public void run() {
		new ProgramThread(program, group).start();
	}
	
	private class ProgramThread extends Thread {
		
		private final Program program;
		
		ProgramThread(Program program, ProgramThreadGroup group) {
			super(group, "Main");
			this.program = program;
		}
		
		private boolean started = false;
		@Override
		public void run() {
			if(!(getThreadGroup() instanceof ProgramThreadGroup)) {
				throw new IllegalThreadStateException("ProgramThread.run() 메소드를 직접 실행할 수 없습니다.");
			}
			
			if(started) {
				throw new IllegalThreadStateException("프로그램 스레드가 이미 실행되었습니다.");
			}
			started = true;
			
			try {
				program.getLanes().getMainLane().execute();
			} catch (final Exception e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						new ErrorDialog(new Exception("프로그램을 실행하는 중 오류가 발생했습니다.", e));
					}
				});
			}
		}
	}
}
