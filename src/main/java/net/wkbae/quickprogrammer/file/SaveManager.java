package net.wkbae.quickprogrammer.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.wkbae.quickprogrammer.BaseBlockModel;
import net.wkbae.quickprogrammer.CodeBlock;
import net.wkbae.quickprogrammer.Lane;
import net.wkbae.quickprogrammer.Program;
import net.wkbae.quickprogrammer.VariableSocket;

public class SaveManager {
	
	private Program program;
	private File file;
	
	/**
	 * 저장 매니저를 생성합니다.<br>
	 * {@link #save()}를 호출하면 파일으로 저장합니다.
	 * @param program 저장할 프로그램
	 * @param file 프로그램을 저장할 파일
	 */
	public SaveManager(Program program, File file) {
		this.program = program;
		this.file = file;
	}
	
	/**
	 * 저장 매니저를 생성합니다.<br>
	 * {@link #save()}를 호출하면 파일으로 저장합니다.
	 * @param program 저장할 프로그램
	 * @param file 프로그램을 저장할 파일
	 */
	public SaveManager(Program program, String file) {
		this.program = program;
		this.file = new File(file);
	}
	
	/**
	 * 프로그램을 파일로 저장합니다.<br>
	 * 이 작업은 같은 이름의 파일을 덮어씁니다. 실행하기 전에 파일을 덮어쓸지 확인하세요.
	 * @throws IOException 파일을 출력할 수 없는 경우 발생합니다.
	 */
	@SuppressWarnings("unchecked")
	public void save() throws IOException {
		
		JSONObject wholeObj = new JSONObject();
		
		JSONObject laneObj = new JSONObject();
		for(Lane lane : program.getLanes().getAllLanes()) {
			JSONArray array = new JSONArray();
			for(CodeBlock block : lane.getBlocks()) {
				array.add(jsonBlock(block));
			}
			laneObj.put(lane.getLaneId(), array);
		}
		wholeObj.put("Lanes", laneObj);
		
		JSONObject socketObj = new JSONObject();
		for(VariableSocket socket : program.getVariableSockets().getAllSockets()) {
			socketObj.put(socket.getSocketId(), jsonBlock(socket.getAttachedBlock()));
		}
		wholeObj.put("Sockets", socketObj);
		
		JSONArray blockArray = new JSONArray();
		for(CodeBlock block : program.getUnorderedBlocks()) {
			blockArray.add(jsonBlock(block));
		}
		wholeObj.put("Blocks", blockArray);
		
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("QPS"); // QuickProgrammerSave
		
		wholeObj.writeJSONString(bw);
		
		bw.close();
		fw.close();
	}
	
	/**
	 * 코드 블록을 {@link JSONObject}로 변환합니다.<br>
	 * {@link BaseBlockModel#saveState(Map)}가 사용됩니다.
	 * @param block 변환할 코드 블록
	 * @return 변환된 JSON 객체
	 */
	private JSONObject jsonBlock(CodeBlock block) {
		if(block == null) {
			return null;
		}
		Map<String, String> attrs = new HashMap<>();
		block.getModel().saveState(attrs);
		return new JSONObject(attrs);
	}
	
}
