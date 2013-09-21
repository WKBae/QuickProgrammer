package net.wkbae.quickprogrammer;

/**
 * 마그넷 영역을 나타내는 클래스입니다.<br>
 * 마그넷 영역은 코드 블록을 근처로 드래그 했을 때 자동으로 붙게되는 영역입니다.
 * @author WKBae
 */
public class MagnetArea {
	
	/**
	 * 마그넷 영역의 넓이<br>
	 * 한 중심점 (x, y)가 있으면 (x - MAGNET_SIZE , y - MAGNET_SIZE)부터 (x + MAGNET_SIZE , y + MAGNET_SIZE)가 마그넷 영역입니다.
	 */
	public final static int MAGNET_SIZE = 20;
	
	/** 레인에서 마그넷 영역의 위치, {@link Lane#attachBlock(int, CodeBlock)} 메소드에서 사용할 수 있습니다. */
	public final int index;
	/** 마그넷 영역의 좌측 상단의 X좌표 */
	public final int x;
	/** 마그넷 영역의 좌측 상단의 Y좌표 */
	public final int y;
	/** 마그넷 영역의 너비 */
	public final int w;
	/** 마그넷 영역의 높이 */
	public final int h;
	
	/**
	 * 새로운 마그넷 영역을 생성합니다.
	 * @param lane 마그넷 영역이 포함된 레인
	 * @param x 마그넷 영역의 좌측 상단의 X좌표
	 * @param y 마그넷 영역의 좌측 상단의 Y좌표
	 * @param width 마그넷 영역의 너비
	 * @param height 마그넷 영역의 높이
	 */
	MagnetArea(int index, int x, int y, int width, int height){
		this.index = index;
		this.x = x;
		this.y = y;
		this.w = width;
		this.h = height;
	}
	
	MagnetArea(int index, int centerX, int centerY){
		this.index = index;
		this.x = centerX - MAGNET_SIZE;
		this.y = centerY - MAGNET_SIZE;
		this.w = MAGNET_SIZE * 2;
		this.h = MAGNET_SIZE * 2;
	}
	/**
	 * 기존의 마그넷 영역을 복사합니다.
	 * @param area 값을 복사할 영역
	 */
	MagnetArea(MagnetArea area){
		this.index = area.index;
		this.x = area.x;
		this.y = area.y;
		this.w = area.w;
		this.h = area.h;
	}
	
	/**
	 * 좌표가 마그넷 영역에 있는지 확인합니다.<br>
	 * 블록의 좌표를 계산할 때에는 블록 위쪽 중앙 부분의 좌표({@link CodeBlock#getX()} + ({@link CodeBlock#getWidth()} / 2), {@link CodeBlock#getY()})로 계산해야합니다.
	 * @param x 계산할 X좌표
	 * @param y 계산할 Y좌표
	 * @return 좌표가 영역 내에 있으면 <code>true</code>
	 */
	public boolean contains(int x, int y){
		return contains(x, y, true);
	}
	
	/**
	 * 좌표가 마그넷 영역에 있는지 확인합니다.<br>
	 * 블록의 좌표를 계산할 때에는 블록 위쪽 중앙 부분의 좌표({@link CodeBlock#getX()} + ({@link CodeBlock#getWidth()} / 2), {@link CodeBlock#getY()})로 계산해야합니다.<br>
	 * 
	 * @param x 계산할 X좌표
	 * @param y 계산할 Y좌표
	 * @param calculateX X좌표를 계산할 지 여부
	 * @return 좌표가 영역 내에 있으면 <code>true</code>
	 */
	public boolean contains(int x, int y, boolean calculateX){
		if(y >= this.y && y <= this.y + h){
			if(!calculateX || (x >= this.x && x <= this.x + w)){
				return true;
			}
		}
		return false;
	}
}