package net.wkbae.quickprogrammer.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.wkbae.quickprogrammer.Logger;
import net.wkbae.quickprogrammer.Logger.LogLevel;
import net.wkbae.quickprogrammer.file.parser.Plugin;
import net.wkbae.quickprogrammer.file.parser.PluginParser;
import net.wkbae.quickprogrammer.file.parser.Plugin.BlockInfo;
import net.wkbae.util.ErrorDialog;

import org.xml.sax.SAXException;


public class PluginManager {
	
	private static File[] pluginFiles;
	private static URL[] pluginURLs;
	
	private static ClassLoader loaderWithPlugins;
	
	public static ClassLoader getClassLoader(){
		return loaderWithPlugins;
	}
	
	private static HashMap<String, BlockInfo> blockInfos;
	public static BlockInfo getBlockInfo(String className) {
		return blockInfos.get(className);
	}
	
	private static HashMap<String, Plugin> allPlugins;
	private static LinkedHashMap<String, Plugin> orderedPlugins;
	
	public static Collection<Plugin> getPlugins() {
		ArrayList<Plugin> list = new ArrayList<>(orderedPlugins.values());
		Collections.sort(list);
		return list;
	}
	
	private PluginManager() {
	}
	
	private static boolean loaded = false;
	public static void loadPlugins(){
		if(!loaded){
			loaded = true;
			
			blockInfos = new HashMap<>();
			allPlugins = new HashMap<String, Plugin>();
			orderedPlugins = new LinkedHashMap<String, Plugin>();
			
			File pluginFolder = new File("plugin/");
			if(!pluginFolder.exists() || !pluginFolder.isDirectory()){
				pluginFolder.mkdir();
				pluginFolder.setReadable(true);
			}else{
				PluginParser pluginParser = null;
				try {
					pluginParser = new PluginParser();
				} catch (SAXException e1) {
					throw new IllegalStateException("플러그인 파서를 생성할 수 없습니다. 플러그인을 로드하지 않습니다.", e1);
				}
				
				if(pluginParser != null) {
					ArrayList<URL> urls = new ArrayList<URL>();
					pluginFiles = pluginFolder.listFiles(new JarFilter());
					for(File file : pluginFiles){
						if(!file.canRead()) {
							Logger.log(LogLevel.WARNING, new IOException(file.getName() + " 파일을 읽을 수 없습니다. 해당 플러그인을 불러오지 않습니다."));
							continue;
						}
						if(!file.isFile()) {
							continue;
						}
						Logger.log(LogLevel.VERBOSE, file.getName() + " 플러그인을 불러오고 있습니다...");
						try {
							Plugin plugin = pluginParser.parsePlugin(file);
							
							allPlugins.put(plugin.getIdentifier(), plugin);
							
							urls.add(file.toURI().toURL());
						} catch (Exception e) {
							//e.printStackTrace();
							new ErrorDialog(e);
						}
					}
					pluginURLs = urls.toArray(new URL[0]);
					loaderWithPlugins = new URLClassLoader(pluginURLs, PluginManager.getClassLoader());
					
					
					// 플러그인들을 나열하기 시작합니다.
					// 플러그인은 각각 의존(depend)하는 다른 플러그인이 있을 수 있으므로, 다른 플러그인이 의존하는 (의존성에서 상위의) 플러그인을 먼저 불러옵니다.
					HashMap<String, Plugin> dependentPlugins = new HashMap<String, Plugin>();
					for(Plugin plugin : allPlugins.values()) {
						if(checkDependency(plugin)) { // 의존성이 없는 플러그인
							orderedPlugins.put(plugin.getIdentifier(), plugin);
						} else { // 의존성이 있는 플러그인(저장해두었다가 처리)
							dependentPlugins.put(plugin.getIdentifier(), plugin);
						}
					}
					dropUnresolvedDependencies(dependentPlugins);
					while(!dependentPlugins.isEmpty()) {
						boolean anyPluginOrdered = false;
						Iterator<Map.Entry<String, Plugin>> dependent = dependentPlugins.entrySet().iterator();
						while(dependent.hasNext()) {
							Map.Entry<String, Plugin> entry = dependent.next();
							
							if(checkDependency(entry.getValue())) {
								orderedPlugins.put(entry.getKey(), entry.getValue());
								dependent.remove();
								anyPluginOrdered = true;
							} else {
								continue;
							}
						}
						
						if(!anyPluginOrdered) { // 플러그인이 나열되지 않았다면 
							// 비어있지 않음(while 조건) & 플러그인의 요구 조건이 만족되지 않음(나열되지 않음)
							// => 요구조건 만족 안됨, 혹은 Circular Dependency
							dropCircularDependencies(dependentPlugins);
						}
					}
					
					for(Plugin plugin : orderedPlugins.values()) {
						for(BlockInfo info : plugin.getBlocks()) {
							blockInfos.put(info.classString, info);
						}
					}
				}
			}
		}
	}
	
	private static void dropUnresolvedDependencies(HashMap<String, Plugin> pluginsToCheck) {
		if(pluginsToCheck == null || pluginsToCheck.isEmpty()) {
			return;
		}
		
		Iterator<Map.Entry<String, Plugin>> iter = pluginsToCheck.entrySet().iterator();
		while(iter.hasNext()) {
			Map.Entry<String, Plugin> entry = iter.next();
			
			ArrayList<String> unresolvedDependency = new ArrayList<String>();
			for(String depend : entry.getValue().getDependency()) {
				if(!allPlugins.containsKey(depend)) {
					unresolvedDependency.add(depend);
				}
			}
			if(!unresolvedDependency.isEmpty()) {
				
				iter.remove();
				
				String title = "[" + entry.getKey() + "] ";
				StringBuilder message = new StringBuilder();
				message.append(title);
				message.append("이 플러그인에 필요한 다음 플러그인을 찾을 수 없습니다.\n");
				message.append(title);
				boolean first = true;
				for(String depend : unresolvedDependency) {
					if(first) {
						message.append(" - ");
						first = false;
					} else {
						message.append(", ");
					}
					message.append(depend);
				}
				message.append("\n");
				message.append(title);
				message.append("플러그인이 로드되지 않습니다.");
				System.out.println(message.toString());
			}
		}
	}
	
	private static void dropCircularDependencies(HashMap<String, Plugin> remainedPlugins) {
		tarjanIndex = 0;
		tarjanS.empty();
		circularDependencies.clear();
		for(Plugin plugin : remainedPlugins.values()) {
			TarjanVertice v = TarjanVertice.getVertice(plugin);
			if(v.index == -1) {
				strongconnect(v);
			}
		}
		
		for(ArrayList<Plugin> circularDependency : circularDependencies) {
			StringBuilder message = new StringBuilder();
			message.append("다음 플러그인 간에 상호 의존성을 가진, 혹은 그러한 플러그인에 의존하는 플러그인이 발견되었습니다.\n");
			boolean first = true;
			for(Plugin plugin : circularDependency) {
				if(first) {
					message.append(" - ");
					first = false;
				} else {
					message.append(", ");
				}
				message.append(plugin.getName());
				message.append("(");
				message.append(plugin.getIdentifier());
				message.append(")");
				remainedPlugins.remove(plugin.getIdentifier());
			}
			message.append("\n");
			message.append("오류가 발생할 수 있으므로 위 플러그인들은 로드되지 않습니다.");
			Logger.log(LogLevel.WARNING, message.toString());
		}
	}
	
	/* 
	 * Tarjan's Strongly Connected Components Algorithm
	 * http://en.wikipedia.org/wiki/Tarjan%E2%80%99s_strongly_connected_components_algorithm
	 */
	private static int tarjanIndex = 0;
	private static Stack<TarjanVertice> tarjanS = new Stack<TarjanVertice>();
	private static void strongconnect(TarjanVertice v) {
		v.index = tarjanIndex;
		v.lowlink = tarjanIndex;
		tarjanIndex++;
		tarjanS.push(v);
		
		for(String depend : getUnresolvedDependency(v.plugin)) {
			TarjanVertice w = TarjanVertice.getVertice(allPlugins.get(depend));
			if(w.index == -1) {
				strongconnect(w);
				v.lowlink = Math.min(v.lowlink, w.lowlink);
			} else if (tarjanS.contains(w)) {
				v.lowlink = Math.min(v.lowlink, w.index);
			}
		}
		
		if(v.lowlink == v.index) {
			ArrayList<Plugin> stronglyConnectedComponent = new ArrayList<Plugin>();
			TarjanVertice w;
			do {
				w = tarjanS.pop();
				stronglyConnectedComponent.add(w.plugin);
			} while (w != v);
			circularDependencies.add(stronglyConnectedComponent);
		}
	}
	
	private static ArrayList<ArrayList<Plugin>> circularDependencies = new ArrayList<ArrayList<Plugin>>();
	
	private static class TarjanVertice {
		private static HashMap<Plugin, TarjanVertice> vertices = new HashMap<Plugin, TarjanVertice>();
		public static TarjanVertice getVertice(Plugin plugin) {
			TarjanVertice v = vertices.get(plugin);
			if(v == null) {
				if(plugin == null) {
					throw new IllegalArgumentException("플러그인을 null로 할 수 없습니다.");
				}
				v = new TarjanVertice(plugin);
				vertices.put(plugin, v);
			}
			return v;
		}
		public final Plugin plugin;
		public int index = -1;
		public int lowlink = -1;
		private TarjanVertice(Plugin plugin) {
			this.plugin = plugin;
		}
	}
	
	private static List<String> getUnresolvedDependency(Plugin plugin) {
		ArrayList<String> unresolved = new ArrayList<String>();
		for(String depend : plugin.getDependency()) {
			if(!orderedPlugins.containsKey(depend)) {
				unresolved.add(depend);
			}
		}
		return unresolved;
	}
	
	private static boolean checkDependency(Plugin plugin) {
		String[] dependency = plugin.getDependency();
		if(dependency == null) {
			return true;
		}
		for(String depend : dependency) {
			if(!orderedPlugins.containsKey(depend)) {
				if(depend.equals(plugin.getIdentifier())) { // 자기 자신에 의존 -> 무시
					continue;
				}
				return false;
			}
		}
		return true;
	}

	private static class JarFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".jar");
		}
	}
	
}
