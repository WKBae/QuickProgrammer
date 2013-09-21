package net.wkbae.quickprogrammer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import net.wkbae.quickprogrammer.Logger.LogLevel;
import net.wkbae.quickprogrammer.ProgrammingFrame.ProgrammingPanel;
import net.wkbae.quickprogrammer.file.PluginManager;
import net.wkbae.quickprogrammer.file.parser.ParseException;
import net.wkbae.quickprogrammer.listener.BlockMoveListener;
import net.wkbae.quickprogrammer.listener.BlockResizeListener;
import net.wkbae.util.ErrorDialog;

/**
 * <p>
 * 코드 블록의 데이터를 처리하는 클래스입니다.<br>
 * 화면상의 코드 블록과 일대일로 대응됩니다.<br>
 * 플러그인을 통해 추가될 수 있으며, 모든 블록은 반드시 이 클래스를 상속받아야 합니다.<br>
 * 생성자는 반드시 {@link Program} 인자가 있어야 하며, 인자로 받은 {@link Program}을 <code>super(program);</code>으로 상위 클래스에게 전달해야합니다.
 * </p>
 * <p> 
 * 프로그램을 파일로 저장할 때 {@link #saveState(Map)}이 호출되며, 여기에서 저장된 데이터는 나중에 불려질 때 {@link #restoreState(Map)}를 통해 전달됩니다.
 * </p>
 * @author WKBae
 */
public abstract class BaseBlockModel implements BlockMoveListener, BlockResizeListener{
	
	
	private Program program;
	private CodeBlock block;
	
	private ProgrammingPanel panel;
	
	private String name = null;
	
	/**
	 * 블록 모델을 생성합니다.<br>
	 * 생성자를 호출하면 자동으로 {@link CodeBlock}을 생성합니다.
	 * @param program
	 */
	public BaseBlockModel(Program program){
		assert(program != null);
		
		this.panel = program.getPanel();
		this.program = program;
		this.block = createCodeBlock();
		
		setSize(150, 50);
		
		getBlock().addMoveListener(this);
		getBlock().addResizeListener(this);
		setDraggable(true);
	}
	
	/**
	 * 코드 블록을 생성합니다.<br>
	 * {@link VariableBlock}을 사용해야 하는 {@link VariableBlockModel}가 상속받아야 하므로 default로 되었습니다.
	 * @return 생성된 코드 블록
	 */
	CodeBlock createCodeBlock() {
		return new CodeBlock(program, this);
	}
	
	/**
	 * 프로그램 전체를 관리하는 {@link Program} 객체를 얻습니다.
	 * @return 이 블록이 포함된 프로그램의 {@link Program}
	 */
	public final Program getProgram() {
		return program;
	}
	
	/**
	 * 프로그램을 표시하는 {@link ProgrammingPanel} 객체를 얻습니다.<br>
	 * @deprecated 단독 실행(Standalone)을 한다면 <code>null</code>을 반환할 수 있습니다.
	 * @return {@link ProgrammingPanel}
	 */
	@Deprecated
	public final ProgrammingPanel getPanel() {
		return panel;
	}
	
	/**
	 * 이 코드 블록의 뷰(View)를 구합니다.<br>
	 * 이 객체가 블록의 표시와 관련된 작업을 합니다.
	 * @return 이 블록이 가지는 {@link CodeBlock}
	 */
	public final CodeBlock getBlock() {
		return block;
	}
	
	/**
	 * 코드 블록에 표시되는 이름을 얻습니다.<br>
	 * 이름은 플러그인의 plugin.xml에서 블록의 이름(name) 속성으로 설정할 수 있습니다.
	 * @return 코드 블록의 이름
	 */
	public final String getName() {
		if(name == null) {
			this.name = PluginManager.getBlockInfo(this.getClass().getName()).name;
		}
		return name;
	}
	
	/**
	 * 이 블록을 드래그할 수 있을 지 설정합니다.<br>
	 * 이 메소드는 {@link CodeBlock#setDraggable(boolean)} 메소드를 호출합니다.
	 * @param draggable 드래그할 수 있다면 <code>true</code>
	 */
	public final void setDraggable(boolean draggable) {
		block.setDraggable(draggable);
	}
	
	/**
	 * 코드 블록의 크기를 설정합니다.<br>
	 * 이 메소드는 {@link CodeBlock#setSize(Dimension)}, {@link CodeBlock#setPreferredSize(Dimension)} 메소드를 호출합니다.
	 * @param width 설정할 너비
	 * @param height 설정할 높이
	 */
	public final void setSize(int width, int height) {
		Dimension size = new Dimension(width, height);
		block.setMinimumSize(size);
		block.setMaximumSize(size);
		block.setPreferredSize(size);
		block.setSize(size);
	}
	
	/**
	 * 코드 블록의 위치를 설정합니다.<br>
	 * 이 메소드는 {@link CodeBlock#setLocation(int, int)} 메소드를 호출합니다.
	 * @param x 블록의 x좌표
	 * @param y 블록의 y좌표
	 */
	public final void setLocation(int x, int y) {
		block.setLocation(x, y);
	}
	
	
/* * * * * * * Abstract Methods * * * * * * */
	
	/**
	 * <p>
	 * 코드 블록을 그릴 때 호출됩니다.<br>
	 * 스윙 컴포넌트를 그릴 때 사용하는 방식과 동일합니다.
	 * </p>
	 * <p>
	 * 기본으로 {@link #paintBackground(Graphics2D)}를 이용하여 배경을 그린 뒤, 그 위에 이름 문자열을 적습니다.<br>
	 * 이 메소드를 상속받는다면 제일 먼저 {@link #paintBackground(Graphics2D)}를 호출해야합니다.
	 * </p>
	 * @param g 컴포넌트를 그릴 그래픽 객체
	 */
	protected void paintComponent(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		paintBackground(g);
		
		g.setColor(Color.BLACK);
		Rectangle2D labelSize = g.getFontMetrics().getStringBounds(getName(), g);
		g.drawString(getName(), (int)(block.getWidth()/2.0 - labelSize.getCenterX()), (int)(block.getHeight()/2.0 - labelSize.getCenterY()));
		
	}
	
	/**
	 * 코드 블록의 배경을 그릴 때 호출됩니다.<br>
	 * 블록 모양의 통일성을 위해, 이 메소드를 상속하는 것은 권장되지 않습니다.
	 * @param g 배경을 그릴 그래픽 객체
	 */
	protected void paintBackground(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		LinearGradientPaint gradient = new LinearGradientPaint(0, 0, 0, getBlock().getHeight(), new float[]{0, 0.618f, 1}, new Color[]{Color.WHITE, getColor(), getColor().brighter()});
		g.setPaint(gradient);
		g.fillRoundRect(0, 0, getBlock().getWidth(), getBlock().getHeight(), 10, 10);
		
		g.setPaint(null);
		g.setStroke(new BasicStroke());
		g.setColor(Color.GRAY);
		g.drawRoundRect(0, 0, getBlock().getWidth()-1, getBlock().getHeight()-1, 10, 10);
	}
	
	private Color color = null;
	
	/**
	 * 블록의 색상을 구합니다.<br>
	 * 이 메소드를 상속받아서 원하는 색상으로 만들 수 있습니다.<br>
	 * 기본적으로, 색상은 패키지명+클래스명을 MD5로 인코딩한 16진수 문자열 32자리를 다음 과정을 통해 만들어냅니다.<br>
	 * <ol>
	 *  <li>16진수 문자열 32자리 중 뒤쪽 8자리를 {@link Long#parseLong(String, int) Long.parseLong(str, 16)}을 이용해 숫자로 만듭니다.</li>
	 *  <li>만든 숫자를 {@link Random#Random(long)}의 시드로 사용합니다.</li>
	 *  <li>{@link Random#nextFloat()}로 소수 3개를 만듭니다.</li>
	 *  <li>랜덤으로 만들어낸 숫자 3개를 각각 <code>0.0~1.0</code>, <code>0.4~0.7</code>, <code>0.8~1.0</code> 범위에 맞게 조정합니다.</li>
	 *  <li>범위가 정해진 숫자를 각각 {@link Color#getHSBColor(float, float, float)}의 H, S, B에 넣어서 색을 만들어냅니다.</li>
	 * </ol>
	 */
	public Color getColor() {
		if(color == null) {
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				// 패키지+클래스명을 MD5로 인코딩
				byte[] md5 = md.digest(this.getClass().getName().getBytes());
				// MD5 바이트 배열 => 16진수 문자열 32자리
				String hex = DatatypeConverter.printHexBinary(md5);
				
				Random rand = new Random(Long.parseLong(hex.substring(24), 16));
				color = Color.getHSBColor(rand.nextFloat(), rand.nextFloat()*0.3f + 0.4f, rand.nextFloat()*0.2f + 0.8f);
			} catch (NoSuchAlgorithmException e) {
				new ErrorDialog(e);
			}
		}
		return color;
	}
	
	/**
	 * 코드 블록을 생성하고 화면에 표시하기 전에 호출됩니다.<br>
	 * 기본적으로 <code>true</code>만 반환하므로 <code>super.initialize()</code>를 호출하지 않아도 됩니다.<br>
	 * 만약 저장된 파일을 열었다면 이 메소드는 호출되지 않고 {@link #restoreState(Map)}가 호출됩니다.
	 * @return 코드 블록을 생성할 지 여부, <code>false</code>를 반환할 경우 블록을 생성하지 않습니다.
	 */
	public boolean initialize() {
		return true;
	}
	
	/**
	 * 코드 블록을 실행할 때 호출됩니다.<br>
	 * 함수나 반복문 등을 통해서 여러 번 실행될 수 있으므로 전역변수는 사용하지 않는 것이 좋습니다.
	 */
	public abstract void execute();
	
	/**
	 * 코드 블록을 더블클릭할 때 호출합니다.<br>
	 * 블록의 설정 창을 여는 것이 좋습니다.
	 */
	public void onDoubleClick() {}
	
	/**
	 * 코드 블록이 움직였을 때({@link CodeBlock#setLocation(int, int)}) 호출됩니다.
	 * @param x 코드 블록이 움직인 x좌표
	 * @param y 코드 블록이 움직인 y좌표
	 */
	@Override
	public void onBlockMove(BaseBlockModel block, int x, int y) {}
	
	/**
	 * 코드 블록의 크기가 변경되었을 때({@link CodeBlock#setSize(int, int)}) 호출됩니다.
	 * @param width 변경된 코드 블록의 넓이
	 * @param height 변경된 코드 블록의 높이
	 */
	@Override
	public void onBlockResize(BaseBlockModel block, int width, int height) {}
	
	/**
	 * 코드 블록이 삭제될 때 호출됩니다.<br>
	 * 블록이 가지고 있던 자원(레인, 소켓, 리스너 등)을 해제해야 합니다.<br>
	 */
	public void onBlockRemove() {}
	
	/**
	 * 코드 블록의 상태를 저장할 때 호출합니다.<br>
	 * 파일로 저장하거나, 블록을 삭제할 때 등 여러 상황에서 호출됩니다.<br>
	 * 기본적으로 여러 값들을 저장하므로, 반드시 <code>super.saveState(attrs)</code>를 실행해야 합니다.<br>
	 * 사용되는 키는 <code>"plugin", "class", "x", "y"</code>입니다.<br>
	 * 저장된 값은 유저가 보거나 변경할 수도 있습니다.
	 * @param attrs 속성들이 저장될 변수
	 * @see #restoreState(Map)
	 */
	public void saveState(final Map<String, String> attrs) {
		attrs.put("plugin", PluginManager.getBlockInfo(this.getClass().getName()).plugin.getIdentifier());
		attrs.put("class", this.getClass().getCanonicalName());
		if(getBlock().getOrderedInfo() == null) {
			Point loc = block.getLocation();
			attrs.put("x", String.valueOf(loc.x));
			attrs.put("y", String.valueOf(loc.y));
		}
	}
	
	/**
	 * 코드 블록의 상태를 되돌릴 때 호출됩니다.<br>
	 * 파일을 불러올 때나, 삭제된 블록을 복구할 때 등 여러 상황에서 포출됩니다.<br>
	 * {@link #saveState(Map)}에서 저장한 내용이 전달됩니다.<br>
	 * 이 메소드를 상속할 경우 <code>super.restoreState(attrs)</code>를 호출해야 합니다.
	 * @param attrs 태그의 속성에 저장된 값({@link #saveState(Map)}에서 저장된 값)
	 * @throws ParseException 저장된 데이터를 분석하는 중 예외가 발생할 경우 {@link ParseException}을 사용합니다.
	 */
	public void restoreState(final Map<String, String> attrs) throws ParseException {
		String xStr = attrs.get("x");
		if(xStr != null) {
			try {
				int x = Integer.parseInt(xStr);
				String yStr = attrs.get("y");
				if(yStr != null) {
					int y = Integer.parseInt(yStr);
					setLocation(x, y);
				}
			} catch (NumberFormatException e) {
				Logger.log(LogLevel.WARNING, e);
				setLocation(0, 0);
			}
		} else {
			setLocation(0, 0);
		}
	}
}
