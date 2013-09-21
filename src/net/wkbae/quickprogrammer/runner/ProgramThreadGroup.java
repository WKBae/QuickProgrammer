package net.wkbae.quickprogrammer.runner;

import net.wkbae.quickprogrammer.Program;

/**
 * 작성된 프로그램을 실행하는 스레드입니다.<br>
 * 프로그램은 항상 이 스레드 위에서 작동하므로 {@link Thread#getThreadGroup()}을 {@link ProgramThreadGroup}으로 캐스팅할 수 있습니다.
 * @author WKBae
 */
public class ProgramThreadGroup extends ThreadGroup {
	
	private static int counter = 1;
	
	private final Program program;
	
	/**
	 * 프로그램이 실행되는 스레드 그룹을 생성합니다.
	 * @param program 실행될 {@link Program 프로그램}
	 */
	ProgramThreadGroup(Program program) {
		super(String.valueOf(counter++));
		this.program = program;
	}
	
	public Program getProgram() {
		return program;
	}
}
