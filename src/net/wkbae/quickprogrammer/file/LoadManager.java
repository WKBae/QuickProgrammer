package net.wkbae.quickprogrammer.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import net.wkbae.quickprogrammer.BaseBlockModel;
import net.wkbae.quickprogrammer.CodeBlock;
import net.wkbae.quickprogrammer.Lane;
import net.wkbae.quickprogrammer.Program;
import net.wkbae.quickprogrammer.VariableBlock;
import net.wkbae.quickprogrammer.VariableSocket;
import net.wkbae.quickprogrammer.file.parser.LoadException;
import net.wkbae.quickprogrammer.file.parser.Plugin.BlockInfo;

public class LoadManager {

	private Program program;
	private File file;
	
	/**
	 * 불러오기 매니저를 생성합니다.<br>
	 * {@link #load()}를 호출해 파일을 불러옵니다.
	 * @param program 저장할 프로그램
	 * @param file 프로그램을 저장할 파일
	 */
	public LoadManager(Program program, File file) {
		this.program = program;
		this.file = file;
	}
	
	/**
	 * 불러오기 매니저를 생성합니다.<br>
	 * {@link #load()}를 호출해 파일을 불러옵니다.
	 * @param program 저장할 프로그램
	 * @param file 프로그램을 저장할 파일
	 */
	public LoadManager(Program program, String file) {
		this.program = program;
		this.file = new File(file);
	}
	
	@SuppressWarnings("unchecked")
	public void load() throws IOException {
		if(file.exists() && file.isFile()) {
			FileReader fr = new FileReader(file);
			
			char[] buffer = new char[3];
			fr.read(buffer);
			if(!(buffer[0] == 'Q' && buffer[1] == 'P' && buffer[2] == 'S')) {
				fr.close();
				throw new LoadException("퀵 프로그래머 저장 파일이 아닙니다.");
			}
			
			BufferedReader br = new BufferedReader(fr);
			
			try {
				JSONObject obj = (JSONObject) JSONValue.parseWithException(br);
				
				
				JSONObject lanes = (JSONObject) obj.get("Lanes");
				HashMap<Integer, LinkedHashMap<CodeBlock, HashMap<String, String>>> orderedBlocks = new HashMap<>();
				if(lanes != null) {
					for(Entry<String, JSONArray> entry : (Set<Entry<String, JSONArray>>) lanes.entrySet()) {
						int id = Integer.parseInt(entry.getKey());
						if(id != 0 && !program.getLanes().createLane(id)) {
							throw new LoadException(id + "번 레인을 생성할 수 없습니다.");
						}
						
						LinkedHashMap<CodeBlock, HashMap<String, String>> map = new LinkedHashMap<>();
						for(Object blk : entry.getValue()) {
							JSONObject attr = (JSONObject) blk;
							CodeBlock block = parseBlock(attr);
							map.put(block, attr);
						}
						orderedBlocks.put(id, map);
					}
				}
				
				
				JSONObject sockets = (JSONObject) obj.get("Sockets");
				HashMap<Integer, SimpleEntry<CodeBlock, HashMap<String, String>>> attachedBlocks = new HashMap<>();
				if(sockets != null) {
					for(Entry<String, JSONObject> entry : (Set<Entry<String, JSONObject>>) sockets.entrySet()) {
						int id = Integer.parseInt(entry.getKey());
						if(!program.getVariableSockets().createSocket(id)) {
							throw new LoadException(id + "번 소켓을 생성할 수 없습니다.");
						}
						
						JSONObject attr = entry.getValue();
						CodeBlock block = parseBlock(attr);
						SimpleEntry<CodeBlock, HashMap<String, String>> map = new SimpleEntry<CodeBlock, HashMap<String, String>>(block, attr);
						attachedBlocks.put(id, map);
					}
				}

				for(Integer id : orderedBlocks.keySet()) {
					Lane lane = program.getLanes().getLane(id);
					LinkedHashMap<CodeBlock, HashMap<String, String>> blocks =  orderedBlocks.get(id);
					for(CodeBlock block : blocks.keySet()) {
						program.addBlock(block, false);
						block.getModel().restoreState(blocks.get(block));
						
						lane.attachBlock(-1, block);
					}
				}

				for(Integer id : attachedBlocks.keySet()) {
					VariableSocket socket = program.getVariableSockets().getSocket(id);
					SimpleEntry<CodeBlock, HashMap<String, String>> blockEntry =  attachedBlocks.get(id);
					if(blockEntry.getKey() != null) {
						program.addBlock(blockEntry.getKey(), false);
						blockEntry.getKey().getModel().restoreState(blockEntry.getValue());
						
						socket.attachBlock((VariableBlock) blockEntry.getKey());
					}
				}
				
				JSONArray blocks = (JSONArray) obj.get("Blocks");
				if(blocks != null) {
					for(Object blk : blocks) {
						JSONObject attr = (JSONObject) blk;
						CodeBlock block = parseBlock(attr);
						
						block.getModel().restoreState((HashMap<String, String>) attr);
					}
				}
			} catch(ClassCastException | NumberFormatException e) {
				throw new LoadException("잘못된 형식의 파일입니다.", e);
			} catch(Exception e) {
				throw new LoadException("파일을 불러올 수 없습니다.", e);
			} finally {
				br.close();
				fr.close();
			}
		} else {
			throw new LoadException(file + " 파일을 열 수 없습니다.");
		}
	}
	
	private CodeBlock parseBlock(JSONObject obj) throws LoadException {
		if(obj == null) {
			return null;
		}
		BlockInfo info = PluginManager.getBlockInfo((String) obj.get("class"));
		if(info == null) throw new LoadException("저장 파일에서 " + obj.get("identifier") + " 플러그인의 " + obj.get("class") + " 블록을 찾을 수 없습니다.");
		
		Class<? extends BaseBlockModel> blockClass;
		try {
			blockClass = info.getBlockClass();
		} catch (ClassNotFoundException e) {
			throw new LoadException("저장 파일에서 " + obj.get("identifier") + " 플러그인의 " + obj.get("class") + " 블록을 찾을 수 없습니다.", e);
		}
		
		try {
			Constructor<? extends BaseBlockModel> con = blockClass.getDeclaredConstructor(Program.class);
			BaseBlockModel model = con.newInstance(program);
			return model.getBlock();
		} catch (NoSuchMethodException e) {
			IllegalClassFormatException ex = new IllegalClassFormatException("생성자를 찾을 수 없습니다. 생성자의 인수는 Program 클래스 하나만 있어야 합니다.");
			ex.setStackTrace(e.getStackTrace());
			throw new LoadException("블록을 생성할 수 없습니다.", ex);
		} catch (Throwable e) {
			throw new LoadException("블록을 생성할 수 없습니다.", e);
		}
	}
}
