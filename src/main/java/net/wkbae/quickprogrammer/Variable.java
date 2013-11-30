package net.wkbae.quickprogrammer;

/**
 * 프로그램에서 사용되는 변수입니다.<br>
 * 변수는 {@link VariableType} 형식의 자료형을 가집니다.
 * @author WKBae
 * @see VariableType
 */
public final class Variable<T> {

	private VariableType type;
	private T content;
	
	/**
	 * 새로운 변수를 생성합니다.<br>
	 * 이 생성자는 {@link Variables#createVariable(String, VariableType)} 클래스를 통해서만 호출되어야 합니다.
	 * @param programId 변수가 생성되는 프로그램의 ID
	 * @param type 변수의 자료형
	 */
	@SuppressWarnings("unchecked")
	Variable(VariableType type){
		this.type = type;
		this.content = (T) type.getInitialValue();
	}
	
	/**
	 * 변수의 값을 설정합니다.<br>
	 * 변수에 대입될 값은 반드시 변수의 형식으로 변환될 수 있어야 합니다.
	 * @param content 변수에 설정할 값. 반드시 변수의 형식으로 변환되어야 하며, 그렇지 않을 경우 프로그램 오류를 일으킵니다.
	 */
	public void setValue(T content){
		if(this.type.isTypeOf(content)){
			this.content = content;
		}else{
			throw new RuntimeException(this.type + " 타입의 변수에 " + VariableType.typeStringOf(content) + " 타입을 대입했습니다.");
		}
	}
	
	/**
	 * 변수의 종류를 구합니다.
	 * @return 변수의 종류
	 */
	public VariableType getType(){
		return type;
	}
	
	/**
	 * 변수의 값을 구합니다.
	 * @return 변수의 값
	 */
	public T getValue(){
		return content;
	}
}