package net.wkbae.quickprogrammer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import net.wkbae.quickprogrammer.file.parser.ParseException;
import net.wkbae.quickprogrammer.listener.LaneResizeListener;

/**
 * 다른 블록들을 담을 수 있는, {@link Lane 레인}을 가지고 있는 코드 블록입니다.<br>
 * {@link #getInnerLane()} 메소드를 활용하여  다양한 기능을 구현할 수 있습니다.
 * @author WKBae
 * @see BaseBlockModel
 */
public abstract class ContainerBlockModel extends BaseBlockModel {

	private final static int BORDER_SIZE = 7;
	private final static int HORIZONTAL_MARGIN_SIZE = 5;
	private final static int VERTICAL_MARGIN_SIZE = 10;
	private final Rectangle baseSize = new Rectangle(150, 50);
	
	private int innerLane;
	
	private MouseReleaseListener releaseListener;
	
	public ContainerBlockModel(Program program) {
		super(program);
		
		releaseListener = new MouseReleaseListener();
		getBlock().addMouseListener(releaseListener);
		
		setSize(baseSize.width, baseSize.height + Lane.BLOCK_MARGIN_SIZE + VERTICAL_MARGIN_SIZE + BORDER_SIZE);
	}
	
	/**
	 * 블록의 기본 크기(다른 블록을 놓기 위한 공간을 제외한 크기)를 설정합니다.
	 * @param width 설정할 블록의 기본 너비
	 * @param height 설정할 블록의 기본 높이
	 */
	public final void setBaseSize(int width, int height) {
		baseSize.setSize(width, height);
		setSize(baseSize.width, baseSize.height + Lane.BLOCK_MARGIN_SIZE + VERTICAL_MARGIN_SIZE + BORDER_SIZE);
	}
	
	/**
	 * 블록의 기본 크기(다른 블록을 놓기 위한 공간을 제외한 크기)를 구합니다.
	 * @return 코드 블록의 기본 크기
	 */
	protected Rectangle getBaseSize() {
		return baseSize;
	}
	
	/**
	 * 코드 블록을 생성하고 화면에 표시하기 직전에 호출됩니다.<br>
	 * 이 과정에서 레인을 생성하므로 반드시 <code>super.initialize()</code>를 실행해야 합니다. 단, 결과값은 항상 <code>true</code>를 반환합니다.<br>
	 * 만약 저장된 파일을 열었다면 이 메소드는 호출되지 않고 {@link #restoreState(Map)}가 호출됩니다.
	 */
	@Override
	public boolean initialize() {
		innerLane = getProgram().getLanes().createLane();
		getInnerLane().addResizeListener(new ContainerLaneResizeListener());
		getInnerLane().setLocation(getBlock().getX() + getBlock().getWidth() / 2, getBlock().getY());
		return true;
	}
	
	/**
	 * 내부 레인을 설정합니다.<br>
	 * 이 메소드를 직접 호출하면 안됩니다.
	 * @param id 설정할 레인 id
	 */
	void setInnerLane(int id) {
		innerLane = id;
	}
	
	/**
	 * 코드 블록을 저장할 때 호출합니다.<br>
	 * 기본적으로 여러 값들을 저장하므로, 반드시 <code>super.saveToFile(attrs)</code>를 실행해야 합니다.<br>
	 * 사용되는 키는 <code>"plugin", "class", "x", "y", "laneId"</code>입니다.<br>
	 * 저장된 값은 유저가 보거나 변경할 수도 있습니다.
	 */
	@Override
	public void saveState(Map<String, String> attrs) {
		super.saveState(attrs);
		attrs.put("laneId", getInnerLaneID()+"");
	}
	
	@Override
	public void restoreState(final Map<String, String> attrs) throws ParseException {
		int laneId = Integer.parseInt(attrs.get("laneId"));
		
		if(getProgram().getLanes().getLane(laneId) == null) {
			throw new ParseException(laneId + "번 레인을 찾을 수 없습니다.");
		}
		innerLane = laneId;
		
		Lane lane = getInnerLane();
		lane.addResizeListener(new ContainerLaneResizeListener());
		
		lane.setLocation(this.getBlock().getX() + ContainerBlockModel.this.getBlock().getWidth()/2, this.getBlock().getY() + baseSize.height);
		lane.refreshLayout();
		
		super.restoreState(attrs);
		
	}
	
	/**
	 * 이 블록의 내부에 있는 레인의 ID를 구합니다.
	 * @return 블록 내부 레인의 ID
	 */
	public final int getInnerLaneID() {
		return innerLane;
	}
	
	/**
	 * 이 블록의 내부에 있는 레인을 구합니다.
	 * @return 블록 내부의 레인
	 */
	public final Lane getInnerLane() {
		return getProgram().getLanes().getLane(innerLane);
	}
	
	@Override
	public void onBlockRemove() {
		getProgram().getLanes().removeLane(getInnerLaneID());
	}
	
	@Override
	protected void paintBackground(Graphics2D g) {
	/* https://weblogs.java.net/blog/campbell/archive/2006/07/java_2d_tricker.html */
		int width = getBlock().getWidth();
		int height = getBlock().getHeight();
		BufferedImage img = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		Graphics2D gr = img.createGraphics();
		
		gr.setComposite(AlphaComposite.Clear);
		gr.fillRect(0, 0, width, height);
		
		gr.setComposite(AlphaComposite.Src);
		gr.setColor(Color.WHITE);
		gr.fillRect(0, 0, width, height);
		
		gr.setComposite(AlphaComposite.Clear);
		gr.fillRoundRect(BORDER_SIZE, baseSize.height, getBlock().getWidth() - BORDER_SIZE * 2, getBlock().getHeight() - baseSize.height - BORDER_SIZE, 10, 10);
		
		gr.setComposite(AlphaComposite.SrcAtop);
		super.paintBackground(gr);
		
		gr.dispose();
		g.drawImage(img, 0, 0, null);
		
		g.setColor(Color.GRAY);
		g.setStroke(new BasicStroke());
		g.drawRoundRect(BORDER_SIZE, baseSize.height, getBlock().getWidth() - BORDER_SIZE * 2, getBlock().getHeight() - baseSize.height - BORDER_SIZE, 10, 10);
		
	}
	
	@Override
	protected void paintComponent(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		paintBackground(g);
		
		g.setColor(Color.BLACK);
		Rectangle2D labelSize = g.getFontMetrics().getStringBounds(getName(), g);
		g.drawString(getName(), (int)(getBlock().getWidth()/2.0 - labelSize.getCenterX()), (int)(baseSize.height/2.0 - labelSize.getCenterY()));
		
	}
	
	@Override
	public void onBlockMove(BaseBlockModel block, int x, int y) {
		super.onBlockMove(block, x, y);
		Lane lane = getInnerLane();
		lane.setLocation(ContainerBlockModel.this.getBlock().getX() + ContainerBlockModel.this.getBlock().getWidth()/2,
				ContainerBlockModel.this.getBlock().getY() + baseSize.height);
	}
	
	/**
	 * 이 컨테이너 내부에 있는 레인의 크기가 변경되는 것을 감지하는 리스너입니다.<br>
	 * 레인의 크기가 변경되면 이 컨테이너의 크기도 바꾸기 위해 사용합니다.
	 * @author WKBae
	 */
	private class ContainerLaneResizeListener implements LaneResizeListener {
		@Override
		public void laneResized(Lane lane, int width, int height) {
			ContainerBlockModel.this.getBlock().setSize(Math.max(width + (HORIZONTAL_MARGIN_SIZE + BORDER_SIZE) * 2, baseSize.width), baseSize.height + height + BORDER_SIZE + VERTICAL_MARGIN_SIZE);
			ContainerBlockModel.this.setLocation(lane.getCenterX() - ContainerBlockModel.this.getBlock().getWidth() / 2, ContainerBlockModel.this.getBlock().getY());
			lane.refreshLayout();
		}
	}
	
	/**
	 * 마우스를 놓았을 때 작동하는 리스너입니다.<br>
	 * 이 블록을 제일 아래로 배치해 내부에 있는 블록들이 클릭될 수 있도록 합니다.
	 * @author WKBae
	 */
	private class MouseReleaseListener extends MouseAdapter {
		@Override
		public void mouseReleased(MouseEvent e) {
			try {
				if(getPanel().getComponentCount() > 0) {
					getPanel().setComponentZOrder(getBlock(), getPanel().getComponentCount()-1);
				} else {
					getPanel().setComponentZOrder(getBlock(), 0);
				}
			} catch(IllegalArgumentException ex) {} // 휴지통으로 드래그하여 블록이 제거된 경우 발생되는 예외
		}
	}
}
