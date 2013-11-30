package net.wkbae.quickprogrammer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import net.wkbae.quickprogrammer.file.parser.ParseException;

/**
 * 변수 소켓을 가진 블록입니다.
 * @author WKBae
 * @deprecated 아직 완성되지 않았습니다.
 */
@Deprecated
public abstract class VariableSocketBlockModel extends BaseBlockModel {

	public VariableSocketBlockModel(Program program) {
		super(program);
	}
	
	private LinkedList<Integer> sockets = new LinkedList<>();
	
	private int socketCount = 0;
	protected final void setSocketsCount(int number) {
		if(number < 0) {
			throw new IllegalArgumentException("소켓의 갯수는 음수가 될 수 없습니다.");
		}
		int diff = number - socketCount;
		if(diff > 0) {
			VariableSockets varSockets = getProgram().getVariableSockets();
			while(--diff >= 0) {
				sockets.addLast(varSockets.createSocket());
				varSockets.getSocket(sockets.getLast()).setLocation(getBlock().getX() - VariableSocket.VARIABLE_SOCKET_DEPTH, getBlock().getY() + getBlock().getHeight()/2);
			}
		} else if(diff < 0) {
			while(++diff <= 0) {
				int id = sockets.removeLast();
				getProgram().getVariableSockets().removeSocket(id);
			}
		}
		socketCount = number;
		getBlock().repaint();
	}
	
	public final VariableSocket getSocket(int index) {
		return getProgram().getVariableSockets().getSocket(sockets.get(index));
	}
	
	protected final LinkedList<Integer> getSockets() {
		return sockets;
	}
	
	@Override
	public void saveState(Map<String, String> attrs) {
		super.saveState(attrs);
		attrs.put("sockets", Arrays.toString(sockets.toArray()).replace("[", "").replace("]", "").replace(" ", ""));
	}
	
	@Override
	public void restoreState(Map<String, String> attrs) throws ParseException {
		super.restoreState(attrs);
		
		VariableSockets varSockets = getProgram().getVariableSockets();
		socketCount = 0;
		for(String idStr : attrs.get("sockets").split(",")) {
			if("".equals(idStr.trim())) {
				continue;
			}
			sockets.addLast(Integer.parseInt(idStr));
			varSockets.getSocket(sockets.getLast()).setLocation(getBlock().getX() - VariableSocket.VARIABLE_SOCKET_DEPTH, getBlock().getY() + getBlock().getHeight()/2);
			socketCount++;
		}
	}
	
	@Override
	protected void paintBackground(Graphics2D g) {
		BufferedImage img = new BufferedImage(getBlock().getWidth(), getBlock().getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D gr = img.createGraphics();
		
		super.paintBackground(gr);
		
		VariableSockets allSockets = getProgram().getVariableSockets();
		int startY = VariableSocket.VARIABLE_SOCKET_MARGIN;
		for(int socketId : sockets) {
			VariableSocket socket = allSockets.getSocket(socketId);
			socket.paintSocket(img, gr, startY);
			startY += socket.getHeight() + VariableSocket.VARIABLE_SOCKET_MARGIN;
		}
		
		gr.dispose();
		g.drawImage(img, 0, 0, null);
	}
}
