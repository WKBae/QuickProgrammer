package net.wkbae.quickprogrammer;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * 변수들을 저장하는 클래스입니다.<br>
 * 변수는 프로그램({@link Program}) 단위로 저장되며, {@link Program#getVariables()}를 통해 얻어올 수 있습니다.
 * @author WKBae
 */
@SuppressWarnings("rawtypes")
public final class Variables {
	
	private ArrayList<HashMap<String, Variable>> variables = new ArrayList<>(); // list: declared stack number(lane depth), map: name - variable
	
	/**
	 * 프로그램에서 사용될 변수를 초기화합니다.
	 * @param id 프로그램의 ID({@link Program}에서 생성)
	 * @see Program
	 */
	Variables() {
		
	}
	
	private int depth = -1;
	
	void laneStarted() {
		depth++;
		variables.add(depth, new HashMap<String, Variable>());
	}
	
	void laneFinished() {
		variables.remove(depth);
		depth--;
	}
	
	/**
	 * 생성되어있는 변수를 구합니다.<br>
	 * 만약 같은 이름의 변수가 있다면 깊이가 가장 깊은(가장 나중에 만들어진) 변수를 반환합니다.<br>
	 * 변수가 없다면 유저 오류를 발생시킵니다.
	 * @param name 변수의 이름
	 * @return 찾은 변수, 없다면 <code>null</code>을 반환합니다.
	 */
	public Variable getVariable(String name) {
		for(int i = depth; i >= 1; i--) {
			Variable v = variables.get(i).get(name);
			if(v != null) return v;
		}
		throw new RuntimeException("변수 \"" + name + "\"이 존재하지 않습니다.");
	}
	
	/**
	 * <p>
	 * 새로운 변수를 생성합니다. 변수는 각각의 타입에 맞는 초기값을 가지고 있습니다.<br>
	 * 변수가 이미 존재하면 유저 오류를 발생시킵니다.
	 * </p><p>
	 * <table border="1">
	 * <tr><th>변수 타입</th><th>초기값</th></tr>
	 * <tr><td>{@link VariableType#STRING}</td><td><code>""</code></td></tr>
	 * <tr><td>{@link VariableType#INTEGER}</td><td><code>0</code></td></tr>
	 * <tr><td>{@link VariableType#DOUBLE}</td><td><code>0.0</code></td></tr>
	 * <tr><td>{@link VariableType#BOOLEAN}</td><td><code>false</code></td></tr>
	 * <tr><td>{@link VariableType#OBJECT}</td><td><code>null</code></td></tr>
	 * </table>
	 * </p>
	 * @param name 변수의 이름
	 * @param type 변수의 종류
	 * @see VariableType
	 */
	public Variable createVariable(String name, VariableType type){
		if(depth == -1) {
			throw new IllegalStateException("변수 생성은 프로그램 실행중에 되어야 합니다.");
		}
		if(variables.get(depth).containsKey(name)){
			throw new RuntimeException(name + "은 이미 있는 변수명입니다.");
		}
		Variable var = new Variable(type);
		variables.get(depth).put(name, var);
		return var;
	}
}
