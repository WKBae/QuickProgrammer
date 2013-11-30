package net.wkbae.quickprogrammer;

import java.util.TreeMap;

/**
 * 변수 소켓들을 관리하는 클래스입니다.
 * @author WKBae
 * @deprecated 아직 완성되지 않았습니다.
 */
@Deprecated
public class VariableSockets {
	
	private TreeMap<Integer, VariableSocket> sockets;
	private Program program;
	
	VariableSockets(Program program) {
		sockets = new TreeMap<>();
		this.program = program;
	}
	
	public int createSocket(){
		int id;
		for(id = 0; sockets.containsKey(id); id++);
		if(!createSocket(id)) {
			throw new IllegalStateException("변수  소켓을 생성할 수 없습니다.");
		}
		return id;
	}
	
	public boolean createSocket(int socketId){
		if(sockets.containsKey(socketId)){
			return false;
		}
		sockets.put(socketId, new VariableSocket(program, socketId));
		return true;
	}
	
	public void removeSocket(int socketId){
		VariableSocket socket = sockets.remove(socketId);
		socket.detachBlock(false);
	}
	
	public VariableSocket[] getAllSockets(){
		return sockets.values().toArray(new VariableSocket[0]);
	}
	
	void setSockets(TreeMap<Integer, VariableSocket> sockets) {
		this.sockets = sockets;
	}
	
	public VariableSocket getSocket(int socketId) {
		return sockets.get(socketId);
	}
}
