package net.wkbae.quickprogrammer.listener;

import net.wkbae.quickprogrammer.BaseBlockModel;
import net.wkbae.quickprogrammer.CodeBlock;

/**
 * 블록의 이동을 감지하는 리스너입니다. {@link CodeBlock#addMoveListener(BlockMoveListener)}를 이용해 추가합니다.
 * @author WKBae
 * @see BaseBlockModel
 * @see CodeBlock
 */
public interface BlockMoveListener {
	public void onBlockMove(BaseBlockModel block, int x, int y);
}
