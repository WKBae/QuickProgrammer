package net.wkbae.quickprogrammer.listener;

import net.wkbae.quickprogrammer.Lane;

/**
 * {@link Lane 레인}의 크기가 변경될 때 호출되는 리스너입니다. {@link Lane#addResizeListener(LaneResizeListener)}를 이용해 추가합니다.<br>
 * 레인의 크기가 변경되는 상황은 블록 추가/제거와 내부 블록의 크기 변경 등이 있습니다.
 * @author WKBae
 *
 */
public interface LaneResizeListener {
	public void laneResized(Lane lane, int width, int height);
}