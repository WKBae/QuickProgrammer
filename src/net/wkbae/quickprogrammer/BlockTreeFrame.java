package net.wkbae.quickprogrammer;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;

import javax.swing.Icon;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import net.wkbae.quickprogrammer.file.PluginManager;
import net.wkbae.quickprogrammer.file.parser.Plugin;
import net.wkbae.quickprogrammer.file.parser.Plugin.BlockInfo;
import net.wkbae.util.ErrorDialog;

/**
 * 로드된 모든 플러그인들의 블록 목록을 표시하는 창입니다.<br>
 * 내부에 있는 {@link BlockTreePane} 클래스에서 블록 생성에 관한 처리를 합니다.
 * @author WKBae
 */
public final class BlockTreeFrame extends JInternalFrame {
	private static final long serialVersionUID = 7383890365373643312L;
	
	/**
	 * 블록 목록 창을 생성합니다.<br>
	 * 창을 생성하면 자동으로 {@link #setVisible(boolean) setVisible(true)}이 호출되어 화면에 보이게 됩니다.
	 * @param parent 이 창의 부모 프레임({@link QuickProgrammer})
	 */
	BlockTreeFrame(QuickProgrammer parent) {
		super("블록 목록", true, true, false, true);
		
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		
		this.setSize(200, 500);
		
		this.add(new BlockTreePane(parent));
		
		setVisible(true);
	}
	
	/**
	 * 블록 목록을 표시할 부분입니다.<br>
	 * 플러그인이 많은 경우 스크롤을 할 수 있도록 {@link JScrollPane}을 상속받았습니다.
	 * @author WKBae
	 */
	public final static class BlockTreePane extends JScrollPane {
		private static final long serialVersionUID = 1283433256039486731L;
		
		private QuickProgrammer parent;
		private JTree blockTree;
		
		private DefaultMutableTreeNode rootNode;
		private DefaultMutableTreeNode blockNode;
		private DefaultMutableTreeNode variableNode;
		
		/**
		 * 다양한 플러그인의 블록들을 표시할 트리를 생성합니다.
		 * @param parent
		 */
		private BlockTreePane(QuickProgrammer parent) {
			super(new JTree(new DefaultMutableTreeNode()));
			
			this.parent = parent;
			
			blockTree = (JTree) this.getViewport().getView();
			blockTree.setCellRenderer(new IconRenderer());
			rootNode = (DefaultMutableTreeNode) blockTree.getModel().getRoot();
			
			blockTree.expandRow(0);
			blockTree.setRootVisible(false);
			blockTree.addMouseListener(new NewBlockListener());
			
			blockNode = new DefaultMutableTreeNode("Code Block");
			rootNode.add(blockNode);
			variableNode = new DefaultMutableTreeNode("Variable Block");
			rootNode.add(variableNode);
			
			for(Plugin plugin : PluginManager.getPlugins()) {
				addPlugin(plugin);
			}
			blockTree.expandRow(1);
			blockTree.expandRow(0);
		}
		
		/**
		 * 블록이 표시되는 트리를 얻습니다.
		 * @return 블록들이 표시된 {@link JTree} 객체
		 */
		public JTree getBlockTree(){
			return blockTree;
		}
		
		/**
		 * 블록 트리에 플러그인을 추가합니다.
		 * @param plugin
		 */
		public void addPlugin(Plugin plugin) {
			PluginNode node = new PluginNode(plugin, false);
			blockNode.add(node);
			
			node = new PluginNode(plugin, true);
			variableNode.add(node);
			
			((DefaultTreeModel) blockTree.getModel()).nodeStructureChanged(rootNode);
		}
		
		/**
		 * 블록을 나타내는 노드입니다.<br>
		 * 코드 블록의 {@link Class}를 가지고 있어 블록을 생성할 수 있고, 플러그인 파일에서 블록에 설정된 아이콘을 표시합니다.
		 * @author WKBae
		 * @see PluginNode
		 */
		private class BlockNode extends DefaultMutableTreeNode {
			private static final long serialVersionUID = -6124812766540468925L;
			
			public final Icon icon;
			//public final String label;
			public final Class<? extends BaseBlockModel> blockClass;
			
			BlockNode(String label, Icon icon, Class<? extends BaseBlockModel> blockClass) {
				super(label);
				//this.label = label;
				this.icon = icon;
				this.blockClass = blockClass;
				setAllowsChildren(false);
			}
		}
		
		/**
		 * 플러그인을 나타내는 노드입니다.<br>
		 * 내부에 {@link BlockNode}를 가지며, 플러그인 파일에서 설정된 아이콘을 표시합니다.
		 * @author WKBae
		 * @see BlockNode
		 */
		private class PluginNode extends DefaultMutableTreeNode {
			private static final long serialVersionUID = 2487302535200233375L;
			
			public final Icon icon;
			//public final Plugin plugin;
			
			PluginNode(Plugin plugin, boolean isVariableNode) {
				super(plugin.getName());
				//this.plugin = plugin;
				this.icon = plugin.getIcon();
				
				for(BlockInfo info : plugin.getBlocks()) {
					try {
						Class<? extends BaseBlockModel> blockClass = info.getBlockClass();
						if(VariableBlockModel.class.isAssignableFrom(blockClass)) {
							if(isVariableNode) {
								BlockNode node = new BlockNode(info.name, info.icon, blockClass);
								this.add(node);
							}
						} else {
							if(!isVariableNode) {
								BlockNode node = new BlockNode(info.name, info.icon, blockClass);
								this.add(node);
							}
						}
					} catch (ClassNotFoundException e) {
						new ErrorDialog(e);
					}
					
				}
			}
		}
		
		/**
		 * 더블클릭을 감지하고 블록을 생성하는 클래스입니다.<br>
		 * 트리의 블록 노드({@link BlockNode})를 더블클릭하면 {@link AddBlockJob}을 생성하여 실행합니다.
		 * @author WKBae
		 * @see AddBlockJob
		 */
		private class NewBlockListener extends MouseAdapter {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2 && parent.getCurrentProgram() != null) {
					if(blockTree.getRowForLocation(e.getX(), e.getY()) != -1) {
						Object last = blockTree.getPathForLocation(e.getX(), e.getY()).getLastPathComponent();
						if(last instanceof BlockNode) {
							BlockNode node = (BlockNode) last;
							createBlock(node.blockClass);
						}
						e.consume();
					}
				}
				
			}
			
			private void createBlock(Class<? extends BaseBlockModel> blockClass) {
				Program program = parent.getCurrentProgram();
				program.getJobManager().startMultipleJobs();
				program.getJobManager().doJob(new AddBlockJob(program, blockClass));
			}
		}
		
		/**
		 * 플러그인 노드와 블록 노드의 아이콘을 표시하는 클래스입니다.
		 * @author WKBae
		 * @see PluginNode
		 * @see BlockNode
		 */
		private class IconRenderer extends DefaultTreeCellRenderer {
			private static final long serialVersionUID = -7689298938176649079L;
			
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if(value instanceof BlockNode) {
					BlockNode node = (BlockNode) value;
					if(node.icon != null) {
						setIcon(node.icon);
					}
				} else if(value instanceof PluginNode) {
					PluginNode node = (PluginNode) value;
					if(node.icon != null) {
						setIcon(node.icon);
					}
				}
				return this;
			}
		}
	}
	
	/**
	 * 블록을 생성하는 작업 클래스입니다.<br>
	 * 코드 블록의 {@link Class} 객체를 이용하여 블록을 생성하는데, {@link BaseBlockModel#BaseBlockModel(Program) BaseBlockModel(Program)}처럼 Program을 유일한 인자로 가진 생성자가 반드시 있어야 합니다.
	 * @author WKBae
	 * @see BaseBlockModel
	 */
	private static class AddBlockJob extends Job {
		
		private Program program;
		private Class<? extends BaseBlockModel> blockClass;
		
		private AddBlockJob(Program program, Class<? extends BaseBlockModel> blockClass) {
			this.program = program;
			this.blockClass = blockClass;
		}
		
		@Override
		protected void execute() {
			try {
				Constructor<? extends BaseBlockModel> con = blockClass.getDeclaredConstructor(Program.class);
				BaseBlockModel block = con.newInstance(program);
				
				boolean result = program.addBlock(block.getBlock());
				
				if(result == false) {
					program.getJobManager().removeJob(program.getJobManager().finishMultipleJobs());
				} else {
					program.getJobManager().finishMultipleJobs();
					program.getJobManager().createSnapshot();
				}
				
			} catch (NoSuchMethodException e) {
				IllegalClassFormatException ex = new IllegalClassFormatException("생성자를 찾을 수 없습니다. 생성자의 인수는 Program 클래스 하나만 있어야 합니다.");
				ex.setStackTrace(e.getStackTrace());
				new ErrorDialog(ex);
			} catch (Throwable e) {
				new ErrorDialog(e);
			}
		}
	}
}

