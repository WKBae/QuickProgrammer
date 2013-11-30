package net.wkbae.quickprogrammer;

import java.util.Map;

import net.wkbae.quickprogrammer.file.parser.ParseException;

/**
 * 변수 블록의 데이터를 처리하는 클래스입니다.<br>
 * 이 클래스는 {@link BaseBlockModel}을 확장하고 있지만, {@link #execute()}가 아닌 {@link #evaluate()} 메소드만 이용합니다.<br>
 * 바뀔 가능성이 매우 크므로 이 클래스를 이용하는 것은 권장되지 않습니다.
 * @author WKBae
 * @deprecated 아직 준비되지 않았습니다.
 */
@Deprecated
public abstract class VariableBlockModel extends BaseBlockModel {

	public VariableBlockModel(Program program) {
		super(program);
		
		setSize(100, VariableSocket.VARIABLE_BLOCK_HEIGHT);
	}
	
	@Override
	CodeBlock createCodeBlock() {
		return new VariableBlock(getProgram(), this);
	}
	
	@Override
	public void saveState(Map<String, String> attrs) {
		super.saveState(attrs);
	}
	
	@Override
	public void restoreState(Map<String, String> attrs) throws ParseException {
		super.restoreState(attrs);
	}
	
	/**
	 * 변수 블록에서는 쓰이지 않습니다. 대신 {@link #evaluate()}를 이용하세요.
	 */
	@Override
	public final void execute() {}
	
	/**
	 * 변수 블록의 값을 받아올 때 사용합니다.<br>
	 * 기능적으로 {@link #execute()}와 유사하지만, 값을 반환해야 하므로 이 메소드를 이용해야 합니다.
	 * @return 반환할 값
	 */
	public abstract Object evaluate();
	
}
