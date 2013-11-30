package net.wkbae.quickprogrammer.listener;

import net.wkbae.quickprogrammer.BaseBlockModel;
import net.wkbae.quickprogrammer.CodeBlock;

/**
 * 블록의 크기가 변경되는 것을 감지하는 리스너입니다. {@link CodeBlock#addResizeListener(BlockResizeListener)}를 이용해 추가합니다.
 * @author WKBae
 */
public interface BlockResizeListener {
	public void onBlockResize(BaseBlockModel block, int width, int height);
}