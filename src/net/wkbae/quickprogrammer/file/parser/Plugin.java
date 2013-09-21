package net.wkbae.quickprogrammer.file.parser;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.wkbae.quickprogrammer.BaseBlockModel;
import net.wkbae.quickprogrammer.VariableBlockModel;

/**
 * 불러온 플러그인 하나를 나타냅니다.<br>
 * 플러그인은 서로 비교할 수 있으며({@link Comparable}), 플러그인의 이름으로 비교합니다.
 * @author WKBae
 */
public class Plugin implements Comparable<Plugin>{
	
	private ClassLoader pluginLoader;
	
	private File file;
	private String name;
	private String identifier;
	private Icon icon;
	private String version;
	
	private String[] dependency;
	
	private BlockInfo[] blocks;
	
	/**
	 * 플러그인 클래스를 생성합니다.
	 * @param file 플러그인의 파일명
	 * @param name 플러그인의 이름
	 * @param identifier 플러그인의 식별자
	 * @param icon 플러그인의 아이콘
	 * @param version 플러그인의 버전
	 * @param dependency 플러그인의 의존성, 플러그인을 사용하는 데 필요한 다른 플러그인의 식별자들
	 */
	public Plugin(File file, String name, String identifier, Icon icon, String version, String[] dependency) {
		assert(name != null && name.length() != 0);
		assert(identifier != null && identifier.length() != 0);
		
		this.file = file;
		this.name = name;
		this.identifier = identifier;
		this.icon = icon;
		this.version = version;
		this.dependency = (dependency == null)? new String[0] : dependency;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public Icon getIcon() {
		return icon;
	}

	public String getVersion() {
		return version;
	}
	
	public String[] getDependency() {
		return dependency;
	}
	
	public BlockInfo[] getBlocks() {
		return blocks;
	}
	
	public ClassLoader getPluginLoader() {
		return pluginLoader;
	}
	
	void setPluginLoader(ClassLoader loader) {
		this.pluginLoader = loader;
	}
	
	void setBlocks(BlockInfo[] blocks) {
		this.blocks = blocks;
	}
	
	public class BlockInfo {
		public final Plugin plugin;
		public final String classString;
		private Class<? extends BaseBlockModel> blockClass;
		public final String name;
		public final ImageIcon icon;
		
		BlockInfo(Plugin plugin, String blockClass, String name, ImageIcon icon){
			this.plugin = plugin;
			this.classString = blockClass;
			this.name = name;
			this.icon = icon;
		}
		
		public boolean isVariableBlock() throws ClassNotFoundException {
			return VariableBlockModel.class.isAssignableFrom(getBlockClass());
		}
		
		/**
		 * 코드 블록의 클래스를 구합니다.<br>
		 * 한번 실행하면 저장되어 이후 실행에서는 같은 값을 반환합니다.
		 * @return 코드 블록의 클래스
		 * @throws ClassNotFoundException 코드 블록의 클래스를 (플러그인 내에서) 찾지 못했을 경우
		 * @throws ClassCastException 블록 정보 생성시 주어진 클래스가 코드 블록이 아닌 경우 
		 */
		public Class<? extends BaseBlockModel> getBlockClass() throws ClassNotFoundException {
			if(blockClass == null) {
				System.out.println("load class: " + classString);
				Class<?> baseClass = Class.forName(classString, true, pluginLoader);
				blockClass = baseClass.asSubclass(BaseBlockModel.class);
			}
			return blockClass;
		}
	}

	@Override
	public int compareTo(Plugin plugin) {
		return this.getName().compareTo(plugin.getName());
	}
}
