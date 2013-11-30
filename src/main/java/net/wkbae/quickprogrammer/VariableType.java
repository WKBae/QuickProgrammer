package net.wkbae.quickprogrammer;

/**
 * 변수의 자료형을 표현합니다.
 * @author WKBae
 */
public enum VariableType {
	/**
	 * 문자열 변수가 가지는 타입입니다.
	 */
	STRING(String.class),
	/**
	 * 정수형 변수가 가지는 타입입니다.
	 */
	INTEGER(Integer.class),
	/**
	 * 실수형 변수가 가지는 타입입니다.
	 */
	DOUBLE(Double.class),
	/**
	 * 불리언 변수가 가지는 타입입니다.
	 */
	BOOLEAN(Boolean.class),
	/**
	 * 그 이외의 변수가 가지는 타입입니다.
	 */
	OBJECT(Object.class);
	
	private Class<?> type;
	VariableType(Class<?> typeClass){
		type = typeClass;
	}
	
	/**
	 * 자료형의 클래스를 얻을 때 사용하는 메소드입니다.
	 * @return 자료형이 나타내는 클래스를 반환합니다.
	 */
	public Class<?> getTypeClass(){
		return type;
	}
	
	/**
	 * 어떠한 객체가 이 자료형인지 판단합니다.<br>
	 * <code>null</code>에 대해서는 항상 <code>false</code>를 반환합니다.
	 * @param obj 비교할 객체
	 * @return 주어진 객체가 이 자료형과 같을 경우 <code>true</code>
	 */
	public boolean isTypeOf(Object obj){
		return obj != null && type.isInstance(obj);
	}
	
	/**
	 * 주어진 객체의 지료형을 문자열로 반환합니다.
	 * @param obj 자료형을 얻을 객체
	 * @return 객체의 자료형<br>
	 * <table border=1>
	 * <tr><th>객체의 자료형</th><th>반환값</th></tr>
	 * <tr><td>{@link String}</td><td>"<code>String</code>"</td></tr>
	 * <tr><td>{@link Integer}</td><td>"<code>Integer</code>"</td></tr>
	 * <tr><td>{@link Double}</td><td>"<code>Double</code>"</td></tr>
	 * <tr><td>{@link Boolean}</td><td>"<code>Boolean</code>"</td></tr>
	 * <tr><td>{@link Object}</td><td>"<code>Object</code>"</td></tr>
	 * <tr><td><code>null</code></td><td>"<code>Null</code>"</td></tr>
	 * </table>
	 */
	public static String typeStringOf(Object obj){
		if(obj == null){
			return "Null";
		}else if(STRING.isTypeOf(obj)){
			return "String";
		}else if(INTEGER.isTypeOf(obj)){
			return "Integer";
		}else if(DOUBLE.isTypeOf(obj)){
			return "Double";
		}else if(BOOLEAN.isTypeOf(obj)){
			return "Boolean";
		}else if(OBJECT.isTypeOf(obj)){
			return "Object";
		}
		return "Unknown";
	}
	
	/**
	 * 자료형에 맞는 초기값을 반환합니다.
	 * @return 자료형의 초기값<br>
	 * <table border="1">
	 * <tr><th>변수 타입</th><th>초기값</th></tr>
	 * <tr><td>{@link VariableType#STRING}</td><td><code>""</code></td></tr>
	 * <tr><td>{@link VariableType#INTEGER}</td><td><code>0</code></td></tr>
	 * <tr><td>{@link VariableType#DOUBLE}</td><td><code>0.0</code></td></tr>
	 * <tr><td>{@link VariableType#BOOLEAN}</td><td><code>false</code></td></tr>
	 * <tr><td>{@link VariableType#OBJECT}</td><td><code>null</code></td></tr>
	 * </table>
	 */
	public Object getInitialValue(){
		switch(this){
		case STRING:
			return "";
		case INTEGER:
			return 0;
		case DOUBLE:
			return 0.0;
		case BOOLEAN:
			return true;
		case OBJECT:
			return null;
		default:
			return null;
		}
	}
	
	/**
	 * 자료형을 문자열로 얻을 때 사용합니다.<br>
	 * 이 메소드에서는 클래스 형태로 표현합니다.(Integer -> "Integer" 등)
	 * @return 변수의 자료형
	 */
	public String toString(){
		switch(this){
		case STRING:
			return "String";
		case INTEGER:
			return "Integer";
		case DOUBLE:
			return "Double";
		case BOOLEAN:
			return "Boolean";
		case OBJECT:
			return "Object";
		default:
			return null;
		}
	}
	
	public static VariableType getTypeFromName(String typeName) {
		switch(typeName) {
		case "String":
			return STRING;
		case "Integer":
			return INTEGER;
		case "Double":
			return DOUBLE;
		case "Boolean":
			return BOOLEAN;
		case "Object":
			return OBJECT;
		default:
			return null;
		}
	}
}
