package net.wkbae.quickprogrammer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * 콘솔에 현재 상황을 출력하는 클래스입니다.<br>
 * 다음 버전에서  slf4j로 변경됩니다.
 * @author WKBae
 */
@Deprecated
public class Logger {
	/**
	 * 로그의 수준을 나타내는 Enum입니다.
	 * @author WKBae
	 */
	public static enum LogLevel {
		VERBOSE(0), INFO(1), WARNING(2), ERROR(3);
		
		private int level;
		LogLevel(int level) {
			this.level = level;
		}
		
		public int getLevelNumber() {
			return level;
		}
		
	}
	
	private static LogLevel level;
	private static PrintStream output;
	
	/**
	 * Logger를 초기화합니다. 지정된 Level 이상의 상태만 output으로 출력합니다.
	 * @param level 이 레벨 이상의 로그만 출력됩니다.
	 * @param output 로그를 출력할 스트림입니다.
	 */
	public static void init(LogLevel level, PrintStream output) {
		Logger.level = level;
		Logger.output = output;
		
	}
	
	/**
	 * Logger를 초기화합니다. 지정된 Level 이상의 상태만 {@link System#out}으로 출력합니다.
	 * @param level 이 레벨 이상의 로그만 출력됩니다.
	 */
	public static void init(LogLevel level) {
		init(level, System.out);
	}
	
	/**
	 * Logger를 초기화합니다. WARNING 이상의 상태만 {@link System#out}으로 출력합니다.
	 */
	public static void init() {
		init(LogLevel.WARNING, System.out);
	}
	
	/**
	 * 메시지를 출력합니다.<br>
	 * 지정된 레벨 이상의 메세지만, 앞에 레벨이 붙어서("[WARNING] message") 출력됩니다.
	 * @param level 출력할 로그의 레벨
	 * @param message 출력할 메시지
	 */
	public static void log(LogLevel level, String message) {
		if(level.getLevelNumber() >= Logger.level.getLevelNumber()) {
			message = message.replaceAll("\n", "\n[" + level.toString() + "] ");
			output.println("[" + level.toString() + "] " + message);
		}
	}
	
	/**
	 * 예외를 출력합니다.<br>
	 * {@link Throwable#printStackTrace(PrintStream)}를 이용해 출력합니다.
	 * @param level 출력할 로그의 레벨
	 * @param e 출력할 예외
	 */
	public static void log(LogLevel level, Throwable e) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		
		e.printStackTrace(ps);
		
		ps.close();
		String msg = baos.toString();
		log(level, msg);
	}
}
