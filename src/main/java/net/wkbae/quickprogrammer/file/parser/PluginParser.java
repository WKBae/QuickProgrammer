package net.wkbae.quickprogrammer.file.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.wkbae.quickprogrammer.Logger;
import net.wkbae.quickprogrammer.Logger.LogLevel;
import net.wkbae.quickprogrammer.file.parser.Plugin.BlockInfo;
import net.wkbae.util.ErrorDialog;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 플러그인 파일을 분석하여 {@link Plugin}의 형태로 저장하는 클래스.<br>
 * 이 클래스는 한 번에 하나의 플러그인을 분석하며, 플러그인의 활성화 순서 등은 따로 처리해야합니다.
 * @author WKBae
 */
public class PluginParser extends DefaultHandler {
	
	/**
	 * XML 파일을 파싱할 파서
	 */
	private SAXParser parser;
	
	/**
	 * 플러그인의 파일
	 */
	private File file;
	
	/**
	 * 플러그인의 jar 파일
	 */
	private JarFile jar;
	
	/**
	 * 플러그인의 클래스 로더입니다. 플러그인에 포함된 클래스는 {@link Class#forName(String, boolean, ClassLoader)}의 ClassLoader 파라미터에 이 로더를 넣으면 됩니다.
	 */
	private ClassLoader pluginClassLoader;
	
	/**
	 * 이 플러그인에 포함된 블록들의 정보를 저장하는 리스트. 블록의 정보는 {@link BlockInfo} 클래스의 형태로 저장됩니다.
	 */
	private ArrayList<BlockInfo> blocks = new ArrayList<>();
	
	/**
	 * 플러그인 파서를 생성합니다. 이 과정에서 XML 파서를 생성하며, 이 파서는 여러번 사용({@link #parsePlugin(File)} 여러번 호출)이 가능합니다.
	 * @throws SAXException
	 */
	public PluginParser() throws SAXException{
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			parser = factory.newSAXParser();
		} catch (ParserConfigurationException e) {
			new ErrorDialog(e);
		}
	}
	
	/**
	 * 플러그인을 분석합니다.<br>
	 * 이 메소드는 서로 다른 파일로 여러번 호출할 수 있습니다.
	 * @param pluginFile 분석할 플러그인(.jar) 파일
	 * @return 분석된 플러그인, 플러그인에 포함된 블록, 초기화 클래스, 파서, 그리고 커스텀 태그가 포함되어있습니다.
	 * @throws IOException 파서가 플러그인 설명 파일을 읽지 못했을 때 발생
	 * @throws SAXException 플러그인 설명 파일에서 문제점이 발생된 경우, {@link IllegalPluginFormatException}, {@link IllegalPluginDescriptionException}이 포함됩니다.
	 */
	public Plugin parsePlugin(File pluginFile) throws IOException, SAXException {
		this.jar = new JarFile(pluginFile);
		this.file = pluginFile;
		pluginClassLoader = new URLClassLoader(new URL[]{pluginFile.toURI().toURL()}); // 플러그인 jar 파일의 경로로 클래스 로더를 생성합니다.
		parser.parse(getDescription(jar), this);
		return currentPlugin;
	}
	
	/**
	 * 플러그인 파일에 포함된 설명 파일의 이름입니다.
	 */
	private final static String DESCRIPTION_FILE = "plugin.xml";
	
	/**
	 * 플러그인 파일에서 설명 파일을 구합니다.<br>
	 * jar 파일은 압축 형식으로, 압축파일의 루트에 포함된 {@value #DESCRIPTION_FILE} 파일을 찾습니다.
	 * @param jar 플러그인의 {@link JarFile}
	 * @return 설명 파일의 입력 스트림
	 * @throws IllegalPluginFormatException 파일을 찾을 수 없는 경우에 발생합니다.
	 * @throws IOException 파일을 읽을 수 없는 경우에 발생합니다.
	 */
	private static InputStream getDescription(JarFile jar) throws IllegalPluginFormatException, IOException {
		JarEntry entry = jar.getJarEntry(DESCRIPTION_FILE);
		if(entry == null) {
			throw new IllegalPluginFormatException(DESCRIPTION_FILE + " 파일을 찾을 수 없습니다.");
		}
		return jar.getInputStream(entry);
	}
	
	/**
	 * 문서에서 현재의 위치를 나타내는 변수입니다.<br>
	 * 예외가 발생할 때 이 위치를 사용합니다.
	 */
	private Locator currentLocator;
	@Override
	public void setDocumentLocator(Locator locator) {
		currentLocator = locator;
	}
	
	/**
	 * <code>&lt;Plugin&gt;</code> 태그가 열렸는지 여부
	 */
	private boolean pluginOpened = false;
	
	/**
	 * <code>&lt;Plugin&gt;</code>을 제외한 나머지 태그(<code>&lt;Block&gt;</code>, <code>&lt;Initializer&gt;</code>)가 열려있는지의 여부
	 */
	private boolean tagOpened = false;
	
	/**
	 * <code>&lt;Plugin&gt;</code> 태그가 닫혔는지 여부, 플러그인 태그가 두 번 이상 열렸는지를 알아내기 위해서 있습니다.
	 */
	private boolean pluginClosed = false;
	
	@Override
	public void startDocument() throws SAXException {
		// 변수들을 초기화
		currentPlugin = null;
		currentLocator = null;
		pluginOpened = false;
		tagOpened = false;
		pluginClosed = false;
		
		blocks.clear();
	}
	
	/**
	 * 현재 열려있는(데이터를 넣을) {@link Plugin} 객체입니다. <code>&lt;Plugin&gt;</code> 태그가 열리면 객체를 생성합니다.
	 */
	private Plugin currentPlugin;
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		if(tagOpened){
			throw new IllegalPluginDescriptionException("태그가 다른 태그의 내부에 있을 수 없습니다.", currentLocator);
		}
		
		if(qName.equalsIgnoreCase("Plugin")){ // <Plugin> 태그
			if(pluginClosed){
				throw new IllegalPluginDescriptionException("Plugin 태그가 여러번 열렸습니다.", currentLocator);
			}
			if(pluginOpened){
				throw new IllegalPluginDescriptionException("Plugin 태그의 내부에 다시 Plugin 태그가 열렸습니다.", currentLocator);
			}
			
		// 플러그인의 이름("name" 속성)을 구합니다.
			String name = attrs.getValue("name");
			if(name == null) {
				throw new IllegalPluginDescriptionException(jar.getName() + " 파일의 플러그인의 이름이 지정되지 않았습니다.", currentLocator);
			} else {
				name = name.trim();
				if(name.length() == 0) {
					throw new IllegalPluginDescriptionException(jar.getName() + " 파일의 플러그인의 이름이 비어있습니다.", currentLocator);
				}
			}
		// 플러그인의 식별자("identifier" 속성)를 구합니다.
			String identifier = attrs.getValue("identifier");
			if(identifier == null) {
				throw new IllegalPluginDescriptionException(jar.getName() + " 파일의 플러그인의 식별자가 지정되지 않았습니다.", currentLocator);
			} else{
				identifier = identifier.trim();
				if(identifier.length() == 0) {
					throw new IllegalPluginDescriptionException(jar.getName() + " 파일의 플러그인의 식별자가 비어있습니다.", currentLocator);
				}
			}
		// 플러그인의 버전("version" 속성)을 구합니다. 속성이 존재하지 않더라도 진행합니다.
			String version = attrs.getValue("version");
		// 플러그인의 아이콘("icon" 속성)을 구합니다. 속성이 존재하지 않더라도 진행합니다.
			String icon = attrs.getValue("icon");

			ImageIcon ico = null;
			if(icon != null) {
				try {
					JarEntry iconEntry = jar.getJarEntry(icon);
					if(iconEntry != null) {
						InputStream is = jar.getInputStream(iconEntry);
						ico = new ImageIcon(ImageIO.read(is));
					}
				} catch (IOException e) {
					Logger.log(LogLevel.WARNING, new IllegalPluginFormatException("\"" + name + " 플러그인에서 아이콘 \"" + icon + "\"을 찾을 수 없습니다.",  e));
				}
			}
			
		// 플러그인이 의존하는 다른 플러그인들을 구합니다. 의존은 "depends" 속성에 필요한 플러그인의 식별자를 넣습니다. 2개 이상일 경우 ","로 구분합니다.
			String dependent = attrs.getValue("depends");
			String[] dependency = null;
			if(dependent != null) {
			// ","로 구분된 문자열을 문자열의 배열로 나눕니다.
				String[] tmpDepend = dependent.split(",");
				ArrayList<String> dependencies = new ArrayList<String>();
				for(int i = 0; i < tmpDepend.length; i++) {
				// 문자열이 null이거나 ""인 경우 넘어감
					if(tmpDepend[i] != null && tmpDepend[i].trim().length() > 0) {
					// "A, B" -> "A", " B"
					// 띄어쓰기가 포험될 수 있으므로 trim
						dependencies.add(tmpDepend[i].trim());
					}
				}
				dependency = dependencies.toArray(new String[dependencies.size()]);
			}
		// 플러그인 객체를 만듭니다.
			currentPlugin = new Plugin(file, name, identifier, ico, version, dependency);
		// 플러그인의 클래스 로더를 설정합니다.
			currentPlugin.setPluginLoader(pluginClassLoader);
			pluginOpened = true;
		} else {
			// 플러그인 태그가 아니고, 플러그인 태그가 열리지 않은 경우 예외를 발생시킵니다.
			if(!pluginOpened){
				throw new IllegalPluginDescriptionException("Plugin 태그가 열리지 않았습니다.", currentLocator);
			}
			
			if(qName.equalsIgnoreCase("Block")){ // <Block> 태그
			// 블록의 클래스, 이름, 이미지, 그룹을 구합니다.
				String cls = attrs.getValue("class");
				String name = attrs.getValue("name");
				String icon = attrs.getValue("icon");
			// 필수 속성(클래스, 라벨) 중 하나가 존재하지 않으면 예외를 발생시킵니다.
				if(cls == null) {
					throw new IllegalPluginDescriptionException("Block 태그에 \"class\" 속성이 존재하지 않습니다.", currentLocator);
				} else if(name == null){
					throw new IllegalPluginDescriptionException("Block 태그에 \"name\" 속성이 존재하지 않습니다.", currentLocator);
				}
				
				ImageIcon ico = null;
				if(icon != null) {
					try {
						JarEntry iconEntry = jar.getJarEntry(icon);
						if(iconEntry != null) {
							InputStream is = jar.getInputStream(iconEntry);
							ico = new ImageIcon(ImageIO.read(is));
						}
					} catch (IOException e) {
						Logger.log(LogLevel.INFO, new IllegalPluginFormatException("\"" + name + " 플러그인에서 아이콘 \"" + icon + "\"을 찾을 수 없습니다.",  e));
					}
				}
				
				try {
				// 블록 정보를 만듭니다.
					BlockInfo block = currentPlugin.new BlockInfo(currentPlugin, cls, name, ico);
				// 블록의 클래스를 가져옵니다.(클래스를 확인 - 클래스가 없으면 ClassNotFoundException, 코드 블록이 아니라면 ClassCastException 발생)
					block.getBlockClass();
				// 블록 리스트에 만든 블록 정보를 저장합니다.
					blocks.add(block);
				} catch (ClassCastException e) { // 코드 블록으로 캐스트할 수 없음 - 코드 블록이 아님
					throw new IllegalPluginFormatException("\"" + cls + "\" 클래스는 코드 블록이 아닙니다.", e);
				} catch (ClassNotFoundException e) { // 클래스를 찾을 수 없음
					throw new IllegalPluginFormatException("\"" + cls + "\" 클래스(코드 블록)를 찾을 수 없습니다.", e);
				}
				
				tagOpened = true;
				
			} else {
				throw new IllegalPluginFormatException("알 수 없는 태그입니다: " + qName);
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equalsIgnoreCase("Plugin")) { // 플러그인 태그가 닫힘
			pluginOpened = false;
		// 플러그인 태그가 닫혔음을 표시합니다 - 다시 플러그인 태그가 열릴 경우 예외를 발생시킴
			pluginClosed = true;
		} else { // <Plugin> 이외의 태그가 닫힘
			tagOpened = false;
			
			if(!qName.equalsIgnoreCase("Block")) { // 블록 태그가 아닌 경우
				throw new IllegalPluginDescriptionException("알 수 없는 태그입니다: " + qName, currentLocator);
			}
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
	// 플러그인에 블록 배열을 대입합니다.
		currentPlugin.setBlocks(blocks.toArray(new BlockInfo[blocks.size()]));
	}
}