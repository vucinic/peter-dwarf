package com.peterdwarf.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultTreeModel;

import com.peterdwarf.dwarf.Abbrev;
import com.peterdwarf.dwarf.AbbrevEntry;
import com.peterdwarf.dwarf.CompileUnit;
import com.peterdwarf.dwarf.Definition;
import com.peterdwarf.dwarf.Dwarf;
import com.peterdwarf.dwarf.DwarfDebugLineHeader;
import com.peterdwarf.dwarf.DwarfHeaderFilename;
import com.peterdwarf.dwarf.DwarfLib;
import com.peterdwarf.dwarf.DwarfLine;
import com.peterdwarf.elf.Elf32_Shdr;
import com.peterswing.CommonLib;
import com.peterswing.FilterTreeModel;
import com.peterswing.advancedswing.searchtextfield.JSearchTextField;

public class PeterDwarfPanel extends JPanel {
	DwarfTreeCellRenderer treeCellRenderer = new DwarfTreeCellRenderer();
	DwarfTreeNode root = new DwarfTreeNode("Elf files", null);
	DefaultTreeModel treeModel = new DefaultTreeModel(root);
	FilterTreeModel filterTreeModel = new FilterTreeModel(treeModel, 4);
	JTree tree = new JTree(filterTreeModel);
	Vector<File> files = new Vector<File>();
	public Vector<Dwarf> dwarfs = new Vector<Dwarf>();

	public static void main(String args[]) {
		try {
			UIManager.setLookAndFeel("com.peterswing.white.PeterSwingWhiteLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
		JFrame frame = new JFrame();
		PeterDwarfPanel peterDwarfPanel = new PeterDwarfPanel();
		frame.getContentPane().add(peterDwarfPanel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		//peterDwarfPanel.init("/Users/peter/Desktop/bochs");
		//peterDwarfPanel.init("/Users/peter/workspace/PeterI/kernel/kernel");
		//peterDwarfPanel.init("/Users/peter/workspace/PeterI/kernel/kernel.o");
		//peterDwarfPanel.init("/Users/peter/workspace/PeterI/app/pshell/pshell.o");
		peterDwarfPanel.init(args[0]);
	}

	public PeterDwarfPanel() {
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		tree.setShowsRootHandles(true);
		tree.setCellRenderer(treeCellRenderer);
		scrollPane.setViewportView(tree);

		JToolBar toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);

		JButton expandAllButton = new JButton("expand");
		expandAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterTreeModel.reload();
			}
		});
		toolBar.add(expandAllButton);

		JButton collapseButton = new JButton("collapse");
		collapseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterTreeModel.reload();
				CommonLib.expandAll(tree, false);
			}
		});
		toolBar.add(collapseButton);

		final JSearchTextField searchTextField = new JSearchTextField();
		searchTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				filterTreeModel.filter = searchTextField.getText();
				filterTreeModel.reload();
				//				CommonLib.expandAll(tree, true);
			}
		});
		searchTextField.setMaximumSize(new Dimension(300, 20));
		toolBar.add(searchTextField);

		ToolTipManager.sharedInstance().registerComponent(tree);
	}

	public void init(String filepath) {
		init(new File(filepath), 0, null);
	}

	public void clear() {
		root.children.clear();
		treeModel.nodeChanged(root);
	}

	public void init(final File file, long meoryOffset, final JDialog dialog) {
		final Vector<Dwarf> dwarfVector = DwarfLib.init(file, meoryOffset);
		ExecutorService exec = Executors.newFixedThreadPool(5);
		try {
			for (final Dwarf dwarf : dwarfVector) {
				exec.submit(new Runnable() {
					@Override
					public void run() {
						dwarfs.add(dwarf);
						if (dwarfVector == null) {
							System.err.println("dwarf init fail");
							return;
						}
						files.add(file);
						DwarfTreeNode node = new DwarfTreeNode(dwarf, root);
						root.children.add(node);

						// init section nodes
						DwarfTreeNode sectionNodes = new DwarfTreeNode("section", node);
						node.children.add(sectionNodes);
						for (Elf32_Shdr section : dwarf.sections) {
							showProgress(dialog, "loading dwarf : " + dwarf + ", section : " + section.section_name);
							DwarfTreeNode sectionSubNode = new DwarfTreeNode(section.section_name + ", offset: 0x" + Long.toHexString(section.sh_offset) + ", size: 0x"
									+ Long.toHexString(section.sh_size) + ", addr: 0x" + Long.toHexString(section.sh_addr), sectionNodes);
							String str = "<html><table>";
							str += "<tr><td>no.</td><td>:</td><td>" + section.number + "</td></tr>";
							str += "<tr><td>name</td><td>:</td><td>" + section.section_name + "</td></tr>";
							str += "<tr><td>offset</td><td>:</td><td>0x" + Long.toHexString(section.sh_offset) + "</td></tr>";
							str += "<tr><td>size</td><td>:</td><td>0x" + Long.toHexString(section.sh_size) + "</td></tr>";
							str += "<tr><td>type</td><td>:</td><td>" + section.sh_type + "</td></tr>";
							str += "<tr><td>addr</td><td>:</td><td>0x" + Long.toHexString(section.sh_addr) + "</td></tr>";
							str += "<tr><td>addr align</td><td>:</td><td>" + section.sh_addralign + "</td></tr>";
							str += "<tr><td>ent. size</td><td>:</td><td>" + section.sh_entsize + "</td></tr>";
							str += "<tr><td>flags</td><td>:</td><td>" + section.sh_flags + "</td></tr>";
							str += "<tr><td>info</td><td>:</td><td>" + section.sh_info + "</td></tr>";
							str += "<tr><td>link</td><td>:</td><td>" + section.sh_link + "</td></tr>";
							str += "<tr><td>name idx</td><td>:</td><td>" + section.sh_name + "</td></tr>";
							str += "</table></html>";
							sectionSubNode.tooltip = str;
							sectionNodes.children.add(sectionSubNode);
						}
						// enf init section nodes

						// init abbrev nodes
						DwarfTreeNode abbrevNode = new DwarfTreeNode("abbrev", node);
						node.children.add(abbrevNode);

						LinkedHashMap<Integer, LinkedHashMap<Integer, Abbrev>> abbrevList = dwarf.abbrevList;
						if (abbrevList != null) {
							for (Integer abbrevOffset : abbrevList.keySet()) {
								showProgress(dialog, "loading dwarf : " + dwarf + ", Abbrev offset : " + abbrevOffset);
								DwarfTreeNode abbrevSubnode = new DwarfTreeNode("Abbrev offset=" + abbrevOffset, abbrevNode);
								abbrevNode.children.add(abbrevSubnode);
								LinkedHashMap<Integer, Abbrev> abbrevHashtable = abbrevList.get(abbrevOffset);
								for (Integer abbrevNo : abbrevHashtable.keySet()) {
									Abbrev abbrev = abbrevHashtable.get(abbrevNo);
									DwarfTreeNode abbrevSubnode2 = new DwarfTreeNode(abbrev.number + ": " + Definition.getTagName(abbrev.tag) + " "
											+ (abbrev.has_children ? "has children" : "no children"), abbrevSubnode);
									abbrevSubnode.children.add(abbrevSubnode2);
									for (AbbrevEntry entry : abbrev.entries) {
										DwarfTreeNode abbrevSubnode3 = new DwarfTreeNode(entry.at + "\t" + entry.form + "\t" + Definition.getATName(entry.at) + "\t"
												+ Definition.getFormName(entry.form), abbrevSubnode2);
										abbrevSubnode2.children.add(abbrevSubnode3);
									}

								}
							}
						}
						// end init abbrev nodes

						// init compile unit nodes
						DwarfTreeNode debugLineNode = new DwarfTreeNode("compile unit", node);
						node.children.add(debugLineNode);

						Vector<CompileUnit> compileUnits = dwarf.compileUnits;
						for (CompileUnit compileUnit : compileUnits) {
							DwarfTreeNode compileUnitSubnode = new DwarfTreeNode(compileUnit.DW_AT_name + " , offset " + compileUnit.abbrev_offset + " (size "
									+ compileUnit.addr_size + ")", debugLineNode);
							debugLineNode.children.add(compileUnitSubnode);
						}
						// end init compile unit nodes

						// init headers
						DwarfTreeNode headNode = new DwarfTreeNode("header", node);
						node.children.add(headNode);

						Vector<DwarfDebugLineHeader> headers = dwarf.headers;

						for (DwarfDebugLineHeader header : headers) {
							DwarfTreeNode headerSubnode = new DwarfTreeNode("Offset: 0x" + Long.toHexString(header.offset) + ", Length: " + header.total_length
									+ ", DWARF Version: " + header.version + ", Prologue Length: " + header.prologue_length + ", Minimum Instruction Length: "
									+ header.minimum_instruction_length + ", Initial value of 'is_stmt': " + (header.default_is_stmt ? 1 : 0) + ", Line Base: " + header.line_base
									+ ", Line Range: " + header.line_range + ", Opcode Base: " + header.opcode_base, headNode);
							String str = "<html><table>";
							str += "<tr><td>offset</td><td>:</td><td>0x" + Long.toHexString(header.offset) + "</td></tr>";
							str += "<tr><td>total length</td><td>:</td><td>" + header.total_length + "</td></tr>";
							str += "<tr><td>DWARF Version</td><td>:</td><td>" + header.version + "</td></tr>";
							str += "<tr><td>Prologue Length</td><td>:</td><td>" + header.prologue_length + "</td></tr>";
							str += "<tr><td>Minimum Instruction Length</td><td>:</td><td>" + header.minimum_instruction_length + "</td></tr>";
							str += "<tr><td>Initial value of 'is_stmt'</td><td>:</td><td>" + (header.default_is_stmt ? 1 : 0) + "</td></tr>";
							str += "<tr><td>Line Base</td><td>:</td><td>" + header.line_base + "</td></tr>";
							str += "<tr><td>Line Range</td><td>:</td><td>" + header.line_range + "</td></tr>";
							str += "<tr><td>Opcode Base</td><td>:</td><td>" + header.opcode_base + "</td></tr>";
							str += "</table></html>";
							headerSubnode.tooltip = str;
							headNode.children.add(headerSubnode);

							DwarfTreeNode dirnamesNode = new DwarfTreeNode("dir name", headerSubnode);
							headerSubnode.children.add(dirnamesNode);
							for (String dir : header.dirnames) {
								dirnamesNode.children.add(new DwarfTreeNode(dir, dirnamesNode));
							}

							DwarfTreeNode filenamesNode = new DwarfTreeNode("file name", headerSubnode);
							headerSubnode.children.add(filenamesNode);
							for (DwarfHeaderFilename filename : header.filenames) {
								filenamesNode.children.add(new DwarfTreeNode(filename.file.getAbsolutePath(), filenamesNode));
							}

							DwarfTreeNode lineInfoNode = new DwarfTreeNode("line info", headerSubnode);
							headerSubnode.children.add(lineInfoNode);
							for (DwarfLine line : header.lines) {
								DwarfTreeNode lineSubnode = new DwarfTreeNode("file_num: " + line.file_num + ", line_num:" + line.line_num + ", column_num: " + line.column_num
										+ ", address: 0x" + line.address.toString(16), lineInfoNode);
								lineInfoNode.children.add(lineSubnode);
							}
						}
						// end init headers
					}
				});
			}
		} finally {
			exec.shutdown();
		}
		try {
			while (!exec.awaitTermination(200, TimeUnit.MILLISECONDS)) {
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("loadelf end");
		filterTreeModel.nodeChanged(root);
	}

	private void showProgress(JDialog dialog, String message) {
		if (dialog != null) {
			((JLabel) dialog.getContentPane()).setText(message);
		}
	}
}