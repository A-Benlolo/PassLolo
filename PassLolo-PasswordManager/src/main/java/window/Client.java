package window;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import crypto.AES;
import crypto.Password;
import loloTable.PaddedCellRenderer;
import loloTable.PasswordCellRenderer;
import loloTable.PasswordTableModel;
import loloTable.TableEntry;
import loloTree.SelectionColorsRenderer;
import utilities.Files;
import utilities.PrintableTable;

public class Client extends JFrame {
	private static final long serialVersionUID = -2669181086263505325L;
		
	public static boolean clearClipboard = true;
	public static int maxTime = 15;
	public static boolean autoGeneratePassword = true;
	public static boolean autoCheckDictionary = false;
	public static boolean rememberLastOpenedVault = true;
	public static boolean showEntriesOnTree = true;
	public static boolean onlySearchFieldsVisibleOnTable = false;
	public static String lastOpenedVault ="";

	// The timer
	JLabel copyingStatus = new JLabel("Right click on a row to see copying options.");
	public Timer timer = new Timer(1000, updateTime(copyingStatus));
	public int clock = 0;

	// The size of things
	public static int width = 937;
	public static int height = 865;

	// Styling constants
	public static final char DEFAULT_ECHO_CHAR = new JPasswordField().getEchoChar();
	public static final int MARGIN = 10;

	// Colors
	public static final Color SELECTION_BACKGROUND = new Color(175, 212, 242);
	public static final Color SELECTION_FOREGROUND = new Color(0, 0, 0);
	public static final Color FOCUSED_TEXT_COLOR = new Color(0, 0, 0);
	public static final Color UNFOCUSED_TEXT_COLOR = new Color(100, 100, 100);

	// Listeners
	public static MouseAdapter showPopupMenuOnTree;
	public static MouseAdapter showPopupMenuOnTable;
	public static MouseAdapter populateInformation;
	public static TreeSelectionListener sortEntries;
	public static ListSelectionListener updateSelectionStatus;

	// The working data
	public static Client frame;
	public static String vaultName = "";
	public static boolean fileOpened = false;
	public static boolean changesMade = false;
	public static String[] encryptedLines;
	public static byte[] hashedKey; 
	
	// Launch the application
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new Client();
					frame.setIconImage(new ImageIcon("..\\resources\\windowIcon.png").getImage());
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// Apply look and feel of this computer's UI
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Create the frame
	public Client() {
		// Read the settings from the settings file
		readSettings();
		
		// Get input from the user for file and master password
		Object[] selectedInfo;
		if(rememberLastOpenedVault)
			selectedInfo = VaultOpener.display(DEFAULT_ECHO_CHAR, VaultOpener.OPEN, autoCheckDictionary, lastOpenedVault);
		else
			selectedInfo = VaultOpener.display(DEFAULT_ECHO_CHAR, VaultOpener.OPEN, autoCheckDictionary);
		if(selectedInfo != null)
			hashedKey = (byte[])selectedInfo[0];
		else
			hashedKey = new byte[0];
		
		// Create the content pane
		JLayeredPane contentPane = new JLayeredPane();
		contentPane.setLayout(null);
		contentPane.setOpaque(true);
		contentPane.setBackground(new Color(235, 235, 235));
		setContentPane(contentPane);

		// Create the menu bar
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// Create the File menu and MenuItems
		JMenu fileMenu = new JMenu("File");
		JMenuItem fileNew = createMenuItem("New...");
		JMenuItem fileOpen = createMenuItem("Open...");
		JMenuItem fileSave = createMenuItem("Save");
		JMenuItem fileSaveAs = createMenuItem("Save As...");
		JMenuItem filePrint = createMenuItem("Print...");
		JMenuItem fileChange = createMenuItem("Change Master Password");
		JMenuItem fileLock = createMenuItem("Lock Vault");
		JMenuItem fileExit = createMenuItem("Exit");
		menuBar.add(fileMenu);
		fileMenu.add(fileNew);
		fileMenu.add(fileOpen);
		fileMenu.addSeparator();
		fileMenu.add(fileSave);
		fileMenu.add(fileSaveAs);
		fileMenu.addSeparator();
		fileMenu.add(filePrint);
		fileMenu.addSeparator();
		fileMenu.add(fileChange);
		fileMenu.add(fileLock);
		fileMenu.add(fileExit);
				
		// Create the Entry Menu and MenuItems
		JMenu entryMenu = new JMenu("Entry");
		JMenuItem entryCopyTitle = createMenuItem("Copy Title");
		JMenuItem entryCopyUsername1 = createMenuItem("Copy Username");
		JMenuItem entryCopyPassword1 = createMenuItem("Copy Password");
		JMenu entryCopyField = createMenu("Copy Field");
		JMenuItem entryCopyUsername2 = createMenuItem("Copy Username");
		JMenuItem entryCopyPassword2 = createMenuItem("Copy Password");
		JMenuItem entryCopyNotes = createMenuItem("Copy Notes");
		JMenuItem entryCopyAll = createMenuItem("Copy All Fields");
		JMenuItem entryAdd = createMenuItem("Add Entry...");
		JMenuItem entryEdit = createMenuItem("Edit Entry...");
		JMenuItem entryRemove = createMenuItem("Remove Entry...");
		JMenuItem entrySelectAll = createMenuItem("Select All");
		menuBar.add(entryMenu);
		entryMenu.add(entryCopyUsername1);
		entryMenu.add(entryCopyPassword1);
		entryMenu.add(entryCopyField);
		entryCopyField.add(entryCopyTitle);
		entryCopyField.add(entryCopyUsername2);
		entryCopyField.add(entryCopyPassword2);
		entryCopyField.add(entryCopyNotes);
		entryCopyField.add(entryCopyAll);
		entryMenu.addSeparator();
		entryMenu.add(entryAdd);
		entryMenu.add(entryEdit);
		entryMenu.add(entryRemove);
		entryMenu.addSeparator();
		entryMenu.add(entrySelectAll);

		// Create the Tools Menu and MenuItems
		JMenu toolsMenu = new JMenu("Tools");
		JMenuItem toolsGeneratePassword = createMenuItem("Generate Password...");
		JMenuItem toolsGeneratePasswordList = createMenuItem("Generate Password List...");
		JMenuItem toolsSettings = createMenuItem("Settings...");
		menuBar.add(toolsMenu);
		toolsMenu.add(toolsGeneratePassword);
		toolsMenu.add(toolsGeneratePasswordList);
		toolsMenu.add(toolsSettings);

		// Create the Help Menu and MenuItems
		JMenu helpMenu = new JMenu("Help");
		JMenuItem helpOpenHelp = createMenuItem("Open Help...");
		JMenuItem helpWebsite = createMenuItem("Alex Benlolo Website...");
		JMenuItem helpAbout = createMenuItem("About...");
		menuBar.add(helpMenu);
		helpMenu.add(helpOpenHelp);
		helpMenu.add(helpWebsite);
		helpMenu.add(helpAbout);

		// Create the quick bar components
		JPanel quickPanel = new JPanel();
		quickPanel.setBorder(BorderFactory.createLineBorder(new Color(137, 140, 144), 1));
		quickPanel.setBackground(Color.white);
		quickPanel.setLayout(null);
		contentPane.add(quickPanel);
		JButton newButton = createQuickButton("..\\resources\\new.png", 0, "Create a new vault");
		JButton openButton = createQuickButton("..\\resources\\open.png", 1, "Open an existing vault");
		JButton saveButton = createQuickButton("..\\resources\\save.png", 2, "Save the vault");
		JSeparator saveAddSeparator = new JSeparator(SwingConstants.VERTICAL);
		JButton addButton = createQuickButton("..\\resources\\add.png", 3, "Add an entry");
		JSeparator addSearchSeparator = new JSeparator(SwingConstants.VERTICAL);
		JTextField searchField = new JTextField("Search...");
		JSeparator searchEndSeparator = new JSeparator(SwingConstants.VERTICAL);
		searchField.setForeground(UNFOCUSED_TEXT_COLOR);
		searchField.setBorder(null);
		addButton.setBounds(addButton.getX()+5, addButton.getY(), addButton.getWidth(), addButton.getHeight());
		saveAddSeparator.setBounds(saveButton.getX()+saveButton.getWidth()+2, 2, 1, saveButton.getHeight()-4);
		addSearchSeparator.setBounds(addButton.getX()+addButton.getWidth()+2, 2, 1, addButton.getHeight()-4);
		searchField.setBounds(addSearchSeparator.getX()+5, 1, 100, addButton.getHeight()-1);
		searchEndSeparator.setBounds(searchField.getX()+searchField.getWidth()+2, 2, 1, searchField.getHeight()-4);
		quickPanel.add(newButton);
		quickPanel.add(openButton);
		quickPanel.add(saveButton);
		quickPanel.add(saveAddSeparator);
		quickPanel.add(addButton);
		quickPanel.add(addSearchSeparator);
		quickPanel.add(searchField);
		quickPanel.add(searchEndSeparator);

		// Create and customize the categoryTree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(vaultName);
		JTree categoryTree = new JTree(root);
		DefaultTreeModel categoryTreeModel = (DefaultTreeModel) categoryTree.getModel();
		JScrollPane categoryScrollPane = new JScrollPane(categoryTree);
		SelectionColorsRenderer selectionColorsRenderer = new SelectionColorsRenderer();
		selectionColorsRenderer.setColors(SELECTION_BACKGROUND, SELECTION_FOREGROUND);
		categoryTree.setCellRenderer(selectionColorsRenderer);
		categoryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		categoryScrollPane.setBackground(Color.white);
		categoryTree.setFocusable(false);
		contentPane.add(categoryScrollPane);

		// Create the entryTable
		JTable entryTable = new JTable(new PasswordTableModel(new Object[] { "Title", "Username", "Password", "Notes", "ID" }, 0));
		entryTable.getColumnModel().getColumn(4).setMinWidth(0);
		entryTable.getColumnModel().getColumn(4).setMaxWidth(0);
		entryTable.getColumnModel().getColumn(4).setWidth(0);
		JScrollPane entryScrollPane = new JScrollPane(entryTable);
		// Sort the entryTable by the first column
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(entryTable.getModel());
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		entryTable.setRowSorter(sorter);
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		sorter.sort();
		// Customize the entryTable
		entryTable.setFocusable(false);
		entryTable.getTableHeader().setReorderingAllowed(false);
		entryTable.setRowHeight(25);
		entryTable.setSelectionBackground(SELECTION_BACKGROUND);
		entryTable.setSelectionForeground(SELECTION_FOREGROUND);
		entryTable.setShowHorizontalLines(false);
		entryTable.setGridColor(new Color(0, 0, 0, 35));
		entryScrollPane.getViewport().setBackground(Color.white);
		// Apply the password mask to the entryTable
		PaddedCellRenderer paddedCellRenderer = new PaddedCellRenderer();
		PasswordCellRenderer passwordCellRenderer = new PasswordCellRenderer();
		passwordCellRenderer.setHashedPassword(hashedKey);
		entryTable.setDefaultRenderer(Object.class, paddedCellRenderer);
		entryTable.getColumnModel().getColumn(2).setCellRenderer(passwordCellRenderer);
		entryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		contentPane.add(entryScrollPane);

		// Create the information components
		JPanel informationPanel = new JPanel();
		JLabel[] information = new JLabel[6];
		for (int i = 0; i < information.length; i++)
			information[i] = new JLabel();
		// Customize the informationPanel
		GridLayout gridLayout = new GridLayout(3, 0);
		informationPanel.setLayout(gridLayout);
		for (int i = 0; i < information.length; i++)
			informationPanel.add(information[i]);
		clearInformationLabels(information);
		informationPanel.setBorder(BorderFactory.createLineBorder(new Color(137, 140, 144), 1));
		informationPanel.setBackground(Color.white);
		contentPane.add(informationPanel);

		// Create the status bar
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(BorderFactory.createLineBorder(new Color(137, 140, 144), 1));
		statusPanel.setBackground(Color.white);
		statusPanel.setLayout(null);
		contentPane.add(statusPanel);

		// Create the selection status
		JLabel selectionStatus = new JLabel("n of N selected");
		selectionStatus.setBounds(5, 0, 150, 20);
		statusPanel.add(selectionStatus);
		// Create the separator
		JSeparator statusPanelSeparator = new JSeparator(SwingConstants.VERTICAL);
		saveAddSeparator.setBounds(saveButton.getX()+saveButton.getWidth()+2, 2, 1, saveButton.getHeight()-4);
		statusPanelSeparator.setBounds(selectionStatus.getX()+selectionStatus.getWidth()+2, 2, 1, selectionStatus.getHeight()-4);
		statusPanel.add(statusPanelSeparator);
		// Customize the copying status
		copyingStatus.setBounds(statusPanelSeparator.getX()+3, 0, 400, 20);
		statusPanel.add(copyingStatus);

		// Create the menu when right clicking on the entryTable
		JPopupMenu entryTablePopupMenu = new JPopupMenu();
		JMenuItem popupCopyTitle = createMenuItem("Copy Title");
		JMenuItem popupCopyUsername1 = createMenuItem("Copy Username");
		JMenuItem popupCopyPassword1 = createMenuItem("Copy Password");
		JMenu popupCopyField = createMenu("Copy Field");
		JMenuItem popupCopyUsername2 = createMenuItem("Copy Username");
		JMenuItem popupCopyPassword2 = createMenuItem("Copy Password");
		JMenuItem popupCopyNotes = createMenuItem("Copy Notes");
		JMenuItem popupCopyAll = createMenuItem("Copy All Fields");
		JMenuItem popupAdd = createMenuItem("Add Entry...");
		JMenuItem popupEdit = createMenuItem("Edit Entry...");
		JMenuItem popupRemove = createMenuItem("Remove Entry...");
		JMenuItem popupSelectAll = createMenuItem("Select All");
		entryTablePopupMenu.add(popupCopyUsername1);
		entryTablePopupMenu.add(popupCopyPassword1);
		entryTablePopupMenu.add(popupCopyField);
		popupCopyField.add(popupCopyTitle);
		popupCopyField.add(popupCopyUsername2);
		popupCopyField.add(popupCopyPassword2);
		popupCopyField.add(popupCopyNotes);
		popupCopyField.add(popupCopyAll);
		entryTablePopupMenu.addSeparator();
		entryTablePopupMenu.add(popupAdd);
		entryTablePopupMenu.add(popupEdit);
		entryTablePopupMenu.add(popupRemove);
		entryTablePopupMenu.addSeparator();
		entryTablePopupMenu.add(popupSelectAll);
		
		// Apply the menu mnemonics
		popupCopyUsername1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		popupCopyPassword1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		popupCopyUsername2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		popupCopyPassword2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		popupAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		popupEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));
		popupRemove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		popupSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		// Apply the menu mnemonics
		entryCopyUsername1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		entryCopyPassword1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		entryCopyUsername2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		entryCopyPassword2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		entryAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
		entryEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0));
		entryRemove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		entrySelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		// Apply the menu mnemonics
		fileSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		fileNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		filePrint.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		fileLock.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));

		// Apply the enable menu options listeners
		setEnableComponents(false, 
				popupCopyUsername1, popupCopyPassword1, popupCopyField, popupCopyTitle, popupCopyUsername2, popupCopyPassword2, popupCopyNotes, popupCopyAll,
				entryCopyUsername1, entryCopyPassword1, entryCopyField, entryCopyTitle, entryCopyUsername2, entryCopyPassword2, entryCopyNotes, entryCopyAll,
				popupEdit, entryEdit,
				popupRemove, entryRemove,
				fileSave, fileSaveAs, saveButton,
				entryAdd, popupAdd, addButton,
				entrySelectAll, popupSelectAll,
				filePrint, fileLock, fileChange,
				toolsGeneratePassword, toolsGeneratePasswordList);
		
		applyEnableListener(1, 1, entryTable,
				popupCopyUsername1, popupCopyPassword1, popupCopyField, popupCopyTitle, popupCopyUsername2, popupCopyPassword2, popupCopyNotes, popupCopyAll,
				entryCopyUsername1, entryCopyPassword1, entryCopyField, entryCopyTitle, entryCopyUsername2, entryCopyPassword2, entryCopyNotes, entryCopyAll,
				popupEdit, entryEdit);
		applyEnableListener(1, Integer.MAX_VALUE, entryTable, popupRemove, entryRemove);
		
		// Read the information from the file that is selected at startup
		LinkedList<TableEntry> tableEntries = new LinkedList<TableEntry>();
		if(selectedInfo != null) {
			tableEntries = new LinkedList<TableEntry>(decryptLines(
					selectedInfo, fileSave, fileSaveAs, saveButton,
					toolsGeneratePassword, toolsGeneratePasswordList,
					entryAdd, popupAdd, addButton,
					entrySelectAll, popupSelectAll,
					fileLock, filePrint, fileChange));
			String name = vaultName;
			DefaultTreeModel treeModel = (DefaultTreeModel) categoryTree.getModel();
			root = new DefaultMutableTreeNode(getStringAfterLastOccurrence(name.substring(0, name.length()-4), '\\'));
			treeModel.setRoot(root);
		}
		selectionStatus.setText("0 of "+entryTable.getRowCount()+" selected.");

		// Create the categories based on the entries
		createNodes(root, tableEntries);
		categoryTreeModel.reload();

		// Apply the recursive listeners
		recursizeRemoveSelectionFromTable(contentPane, entryTable, information);
		recursizeRemoveSelectionByEscape(contentPane, entryTable, information, selectionStatus, categoryTree, searchField);

		// Apply the search bar listeners
		searchField.addFocusListener(populateTextfieldWhenNotFocused(searchField, "Search...", categoryTree));
		searchField.getDocument().addDocumentListener(searchTableByField(searchField, entryTable, tableEntries, information, entryTablePopupMenu, selectionStatus));
		
		// Apply the copying listeners
		popupCopyTitle.addActionListener(copyTableEntry(popupCopyTitle, entryTable, "TITLE", tableEntries, copyingStatus));
		popupCopyUsername1.addActionListener(copyTableEntry(popupCopyUsername1, entryTable, "USERNAME", tableEntries, copyingStatus));
		popupCopyPassword1.addActionListener(copyTableEntry(popupCopyPassword1, entryTable, "PASSWORD", tableEntries, copyingStatus));
		popupCopyUsername2.addActionListener(copyTableEntry(popupCopyUsername2, entryTable, "USERNAME", tableEntries, copyingStatus));
		popupCopyPassword2.addActionListener(copyTableEntry(popupCopyPassword2, entryTable, "PASSWORD", tableEntries, copyingStatus));
		popupCopyNotes.addActionListener(copyTableEntry(popupCopyNotes, entryTable, "NOTES", tableEntries, copyingStatus));
		popupCopyAll.addActionListener(copyTableEntry(popupCopyAll, entryTable, "ALL", tableEntries, copyingStatus));
		entryCopyTitle.addActionListener(copyTableEntry(popupCopyTitle, entryTable, "TITLE", tableEntries, copyingStatus));
		entryCopyUsername1.addActionListener(copyTableEntry(popupCopyUsername1, entryTable, "USERNAME", tableEntries, copyingStatus));
		entryCopyPassword1.addActionListener(copyTableEntry(popupCopyPassword1, entryTable, "PASSWORD", tableEntries, copyingStatus));
		entryCopyUsername2.addActionListener(copyTableEntry(popupCopyUsername2, entryTable, "USERNAME", tableEntries, copyingStatus));
		entryCopyPassword2.addActionListener(copyTableEntry(popupCopyPassword2, entryTable, "PASSWORD", tableEntries, copyingStatus));
		entryCopyNotes.addActionListener(copyTableEntry(popupCopyNotes, entryTable, "NOTES", tableEntries, copyingStatus));
		entryCopyAll.addActionListener(copyTableEntry(popupCopyAll, entryTable, "ALL", tableEntries, copyingStatus));

		// Apply the select all listeners
		popupSelectAll.addActionListener(selectAll(entryTable));
		entrySelectAll.addActionListener(selectAll(entryTable));

		// Apply the generate password listeners
		toolsGeneratePassword.addActionListener(generatePassword(
				entryTable, tableEntries, information, entryTablePopupMenu, selectionStatus,
				categoryTree));
		toolsGeneratePasswordList.addActionListener(generatePasswordList(
				entryTable, tableEntries, information, entryTablePopupMenu, selectionStatus,
				categoryTree));

		// Apply the add entry listeners
		entryAdd.addActionListener(addEntry(
				entryTable, tableEntries, information, entryTablePopupMenu, entryAdd, selectionStatus,
				categoryTree));
		popupAdd.addActionListener(addEntry(
				entryTable, tableEntries, information, entryTablePopupMenu, popupAdd, selectionStatus,
				categoryTree));
		addButton.addActionListener(addEntry(
				entryTable, tableEntries, information, entryTablePopupMenu, addButton, selectionStatus,
				categoryTree));

		// Apply the edit entry listeners
		entryEdit.addActionListener(editEntry(
				entryTable, tableEntries, information, entryTablePopupMenu, entryEdit, selectionStatus,
				categoryTree));
		popupEdit.addActionListener(editEntry(
				entryTable, tableEntries, information, entryTablePopupMenu, popupEdit, selectionStatus,
				categoryTree));
		
		// Apply the remove entry listeners
		entryRemove.addActionListener(removeEntry(
				entryTable, tableEntries, information, entryTablePopupMenu, entryRemove, selectionStatus,
				categoryTree));
		popupRemove.addActionListener(removeEntry(
				entryTable, tableEntries, information, entryTablePopupMenu, popupRemove, selectionStatus,
				categoryTree));

		// Add the save listeners
		fileSave.addActionListener(saveFile(tableEntries, fileSave));
		saveButton.addActionListener(saveFile(tableEntries, saveButton));
		fileSaveAs.addActionListener(saveFileAs(tableEntries, fileSaveAs, categoryTree));
		
		// Add the open listeners
		fileOpen.addActionListener(openOrNewFile(VaultOpener.OPEN,
				entryTable, tableEntries, information, entryTablePopupMenu, fileOpen, selectionStatus,
				categoryTree,
				fileSave, fileSaveAs, saveButton,
				entryAdd, popupAdd, addButton, entrySelectAll, popupSelectAll,
				filePrint, fileLock, fileChange,
				toolsGeneratePassword, toolsGeneratePasswordList));
		openButton.addActionListener(openOrNewFile(VaultOpener.OPEN,
				entryTable, tableEntries, information, entryTablePopupMenu, openButton, selectionStatus,
				categoryTree,
				fileSave, fileSaveAs, saveButton,
				entryAdd, popupAdd, addButton, entrySelectAll, popupSelectAll,
				filePrint, fileLock, fileChange,
				toolsGeneratePassword, toolsGeneratePasswordList));
		
		// Add the new listeners
		fileNew.addActionListener(openOrNewFile(VaultOpener.NEW,
				entryTable, tableEntries, information, entryTablePopupMenu, fileNew, selectionStatus,
				categoryTree,
				fileSave, fileSaveAs, saveButton,
				entryAdd, popupAdd, addButton, entrySelectAll, popupSelectAll,
				filePrint, fileLock, fileChange,
				toolsGeneratePassword, toolsGeneratePasswordList));
		newButton.addActionListener(openOrNewFile(VaultOpener.NEW,
				entryTable, tableEntries, information, entryTablePopupMenu, newButton, selectionStatus,
				categoryTree,
				fileSave, fileSaveAs, saveButton,
				entryAdd, popupAdd, addButton, entrySelectAll, popupSelectAll,
				filePrint, fileLock, fileChange,
				toolsGeneratePassword, toolsGeneratePasswordList));
		
		// Add the lock vault listener
		fileLock.addActionListener(lockVault(
				entryTable, tableEntries, information, entryTablePopupMenu, fileLock, selectionStatus,
				categoryTree,
				fileSave, fileSaveAs, saveButton,
				entryAdd, popupAdd, addButton, entrySelectAll, popupSelectAll,
				filePrint, fileLock, fileChange,
				toolsGeneratePassword, toolsGeneratePasswordList));
		
		// Add the print listener
		filePrint.addActionListener(printVault(tableEntries, entryTable));
		
		// Add the Alex Benlolo website listener
		helpWebsite.addActionListener(openLink("http://www.alex-benlolo.tech"));
		
		// Add the listener to show the about window
		helpAbout.addActionListener(showAbout());
		
		// Add the listener to show the settings window
		toolsSettings.addActionListener(showSettings(categoryTree, tableEntries));
		
		// Add the listener to show the password change window
		fileChange.addActionListener(showPasswordChange(tableEntries, passwordCellRenderer, entryTable, information, entryTablePopupMenu, selectionStatus));
		
		// Refresh the table listeners
		refreshTableListeners(entryTable, tableEntries, information, entryTablePopupMenu, selectionStatus);

		// Refresh the tree listeners
		refreshTreeListeners(categoryTree, entryTable, tableEntries, information, entryTablePopupMenu, selectionStatus, searchField);
		categoryTree.setSelectionRow(0);
		
		// Get the screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screenSize.getWidth();
		double screenHeight = screenSize.getHeight();
		
		// Create the frame
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds((int)(screenWidth/2)-width/2, (int)(screenHeight/2)-height/2, width, height);
		setTitle("PassLolo");
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent componentEvent) {
				width = getWidth();
				height = getHeight();

				// Update the quick bar bounds
				quickPanel.setBounds(0, -1, width, 25);

				// Update the categoryTree size
				categoryScrollPane.setBounds(
						MARGIN,
						quickPanel.getY() + quickPanel.getHeight() + 5,
						20 * (width / 100),
						height - 210);

				// Update the entryTable size
				entryScrollPane.setBounds(
						categoryScrollPane.getX() + categoryScrollPane.getWidth() + MARGIN,
						categoryScrollPane.getY(),
						80 * (width / 100) - MARGIN,
						categoryScrollPane.getHeight());
				entryTable.revalidate();
				setTableColumnsWidth(entryTable, entryScrollPane.getWidth(), new double[] { 15, 15, 30, 40, 0 });

				// Update the information size
				informationPanel.setBounds(
						MARGIN,
						categoryScrollPane.getY() + categoryScrollPane.getHeight() + MARGIN,
						MARGIN + categoryScrollPane.getWidth() + entryScrollPane.getWidth(),
						height - (categoryScrollPane.getHeight() + MARGIN) - 115);
				informationPanel.revalidate();

				// Update the status bar bounds
				statusPanel.setBounds(0, height-80, width, 20);
			}
		});
	
		// Add the listeners for closing the window
		addWindowListener(exitWindowThroughX(this, tableEntries));
		fileExit.addActionListener(exitWindowThroughMenu(this, tableEntries));
	}

	//*********************//
	//* General Solutions *//
	//*********************//

	/**
	 * Adds a mouse listener to the parent and all children to remove the selection
	 * from a table when a mouse press occurs.
	 * 
	 * @param root  The root of table.
	 * @param table The table to remove the selection from.
	 * @param information The labels that are populated by table selection.
	 */
	public void recursizeRemoveSelectionFromTable(Component root, JTable table, JLabel[] information) {
		root.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				table.clearSelection();
				clearInformationLabels(information);
			}
		});
		if (root instanceof Container) {
			Component[] children = ((Container) root).getComponents();
			for (Component child : children)
				if (!(child.equals(table)))
					recursizeRemoveSelectionFromTable(child, table, information);
		}
	}

	/**
	 * Adds a key listener to the parent and all children to remove selections from
	 * table, tree, and information when ESC is pressed.
	 * 
	 * @param root        The starting component.
	 * @param table       The table to deselect from.
	 * @param information The labels that the table updates.
	 * @param categories  The tree to deselect from.
	 * @param searchField The search field for the table.
	 */
	public void recursizeRemoveSelectionByEscape(Component root, JTable table, JLabel[] information, JLabel selectionStatusLabel, JTree categories, JTextField searchField) {
		root.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
					// Clear the table
					table.clearSelection();
					
					// Clear the information labels
					clearInformationLabels(information);
					
					// Clear the category tree
					categories.clearSelection();
					
					// Reset the search field
					searchField.setForeground(UNFOCUSED_TEXT_COLOR);
					searchField.setText("Search...");
					searchField.setFocusable(false);
					searchField.setFocusable(true);
					
					// Reset the selection status
					selectionStatusLabel.setText("0 of "+table.getRowCount()+" selected.");
				}
			}

			public void keyPressed(KeyEvent e) { /* Not applicable */ }

			public void keyReleased(KeyEvent e) { /* Not applicable */ }

		});
		
		if (root instanceof Container) {
			Component[] children = ((Container) root).getComponents();
			for (Component child : children)
				recursizeRemoveSelectionByEscape(child, table, information, selectionStatusLabel, categories, searchField);
		}
	}

	/**
	 * Sets the preferred column widths for a table.
	 * 
	 * @param table               The table whose columns are being modified.
	 * @param tablePreferredWidth The preferred width of the table.
	 * @param percentages         An array of percentages for the column widths
	 */
	public static void setTableColumnsWidth(JTable table, int tablePreferredWidth, double[] percentages) {
		TableColumn column;
		double total = 0;
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
			total += percentages[i];

		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth((int) (tablePreferredWidth * (percentages[i] / total)));
		}
	}

	/**
	 * Remove the data from the information labels, leaving only the titles.
	 * 
	 * @param information The array of labels.
	 */
	public static void clearInformationLabels(JLabel[] information) {
		information[0].setText("  Category: ");
		information[1].setText("  Title: ");
		information[2].setText("  Creation Time: ");
		information[3].setText("  Username: ");
		information[4].setText("  Last Modification Time: ");
		information[5].setText("  Password: ");
	}

	/**
	 * Append data to the information titles.
	 * 
	 * @param information The array of labels.
	 * @param data        The array of what is to be appended.
	 */
	public static void populateInformationLabels(JLabel[] information, String[] data) {
		for (int i = 0; i < information.length; i++)
			information[i].setText(information[i].getText() + data[i]);
	}

	/**
	 * Toggle components to be enabled or disabled.
	 * 
	 * @param b The enabled state.
	 * @param components The components.
	 */
	public static void toggleFunctionEnabled(boolean b, Component... components) {
		fileOpened = b;
		for(int i = 0; i < components.length; i++)
			components[i].setEnabled(b);
	}
	
	/**
	 * Create a JMenuItem with some style.
	 * 
	 * @param name The name of the JMenuItem.
	 * 
	 * @return A stylized JMenuItem.
	 */
	public JMenuItem createMenuItem(String name) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.setPreferredSize(new Dimension(250, 22));
		menuItem.setMargin(new Insets(0, 0, 0, 0));
		return menuItem;
	}

	/**
	 * Create a JMenu with some style.
	 * 
	 * @param name The name of the JMenu.
	 * 
	 * @return A stylized JMenu.
	 */
	public JMenu createMenu(String name) {
		JMenu menu = new JMenu(name);
		menu.setPreferredSize(new Dimension(250, 22));
		menu.setMargin(new Insets(0, 0, 0, 0));
		return menu;
	}

	/**
	 * Creates a JButton with some style.
	 * 
	 * @param iconPath The label on the button.
	 * @param index The index on the quick bar.
	 * @param toolTip The tip to show when hovering over the button.
	 * 
	 * @return A stylized JButton.
	 */
	public JButton createQuickButton(String iconPath, int index, String toolTip) {
		JButton button = new JButton(new ImageIcon(iconPath));
		button.setFocusPainted(false);
		button.setBounds(index*23, 0, 23, 23);
		button.setBackground(Color.white);
		button.setBorderPainted(false);
		button.setMargin(new Insets(0, 0, 0 ,0));
		button.setToolTipText(toolTip);
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				if(button.isEnabled())
					button.setBackground(SELECTION_BACKGROUND);
				else
					button.setBackground(null);
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				button.setBackground(Color.white);
			}
		});
		
		return button;
	}

	/**
	 * Return a listener that populates a text field with default text when not in focus.
	 * 
	 * @param field The field to populate.
	 * @param population The term to populate the field with.
	 * @param categories The JTree to remove selection from.
	 * 
	 * @return A FocusListener.
	 */
	public FocusListener populateTextfieldWhenNotFocused(JTextField field, String population, JTree categories) {
		return new FocusListener() {
			public void focusGained(FocusEvent e) {
				categories.clearSelection();
				field.setForeground(FOCUSED_TEXT_COLOR);
				if(field.getText().equals(population))
					field.setText(null);
			}

			public void focusLost(FocusEvent e) {
				field.setForeground(UNFOCUSED_TEXT_COLOR);
				if(field.getText().isEmpty())
					field.setText("Search...");
			}
		};
	}

	/**
	 * Get a sub-string after the last occurrence of a specified character.
	 * 
	 * @param str The string to search.
	 * @param c The character to search for.
	 * 
	 * @return The sub-string after the last occurrence of c in str.
	 */
	public String getStringAfterLastOccurrence(String str, char c) {
		int charPosition = -1;
		String toReturn = "";
		
	    // Get the position of the last period
		for(int i = 0; i < str.length(); i++)
			if(str.charAt(i) == c)
				charPosition = i;
		
		if(charPosition != -1)
			for(int i = charPosition+1; i < str.length(); i++)
				toReturn += str.charAt(i);
		
		return toReturn;
	}
	
	//*********************//
	//* Closing Listeners *//
	//*********************//
	
	/**
	 * Ensure the vault is saved before closing.
	 * <br>
	 * If it is not, ask the user if they want to save first.
	 * 
	 * @param frame The root frame of this application.
	 * @param entries The entries for the table.
	 * 
	 * @return A WindowAdapter.
	 */
	public WindowAdapter exitWindowThroughX(JFrame frame, LinkedList<TableEntry> entries) {
		return new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if(doClosingSequence(entries))
					System.exit(0);
			}
		};
	}
	
	/**
	 * Ensure the vault is saved before closing.
	 * <br>
	 * If it is not, ask the user if they want to save first.
	 * 
	 * @param frame The root frame of this application.
	 * @param entries The entries for the table.
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener exitWindowThroughMenu(JFrame frame, LinkedList<TableEntry> entries) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				if(doClosingSequence(entries))
					System.exit(0);
			}
		};
	}
	
	/**
	 * The driver method behind exitWindowThroughX and exitWindowThroughMenu
	 * 
	 * @param frame The root frame of this application.
	 * @param entries The entries for the table.
	 * 
	 * @return A boolean value for if the user successfully closed the program.
	 */
	public boolean doClosingSequence(LinkedList<TableEntry> entries) {
		if(changesMade) {
			int option = JOptionPane.showConfirmDialog(null, "There are unsaved changes. Would you like to save before exitting?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
			
			if(option == JOptionPane.YES_OPTION) {						
				saveToFile(entries, vaultName);
				hashedKey = null;
				vaultName = null;
				return true;
			}
			else if(option == JOptionPane.NO_OPTION) {
				hashedKey = null;
				vaultName = null;
				return true;
			}
			else
				return false;
		}
		else
			return true;
	}
	
	//*****************//
	//* Table Methods *//
	//*****************//

	/**
	 * Refresh all of the listeners corresponding to the table.
	 * 
	 * @param table       The table that has listeners.
	 * @param entries     All of the entries for the table.
	 * @param information The labels that will be populated when the table is
	 *                    clicked.
	 * @param popupMenu	  The popup menu for right clicking.
	 * @param selectionStatusLabel The label that displays the number of selected rows.
	 */
	public void refreshTableListeners(JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, JLabel selectionStatusLabel) {
		table.removeMouseListener(populateInformation);
		table.removeMouseListener(showPopupMenuOnTable);
		table.getParent().removeMouseListener(showPopupMenuOnTable);
		table.getSelectionModel().removeListSelectionListener(updateSelectionStatus);

		populateInformation = grabInformationFromTable(table, entries, information);
		showPopupMenuOnTable = showPopupMenu(table, popupMenu);
		updateSelectionStatus = updateSelectionStatus(table, selectionStatusLabel);

		table.addMouseListener(populateInformation);
		table.addMouseListener(showPopupMenuOnTable);
		table.getParent().addMouseListener(showPopupMenuOnTable);
		table.getSelectionModel().addListSelectionListener(updateSelectionStatus);
	}

	/**
	 * Create a mouse listener that will populate an array of labels with data
	 * corresponding the the pressed row.
	 * 
	 * @param table       The table that has been clicked.
	 * @param entries     The entries in the table.
	 * @param information The labels to populate.
	 * 
	 * @return A MouseAdapter.
	 */
	public MouseAdapter grabInformationFromTable(JTable table, LinkedList<TableEntry> entries, JLabel[] information) {
		return new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				// Get the selected row
				int row = table.rowAtPoint(me.getPoint());
				int correctedRow = 0;

				// Find the index in the list that corresponds to the table
				TableEntry selectedEntry = new TableEntry();
				selectedEntry.setID((String)table.getValueAt(row, 4));
				for(int i = 0; i < entries.size(); i++) {
					if(entries.get(i).matchesID(selectedEntry)) {
						correctedRow = i;
						break;
					}
				}
				
				// Create the data
				TableEntry tmp = entries.get(correctedRow);
				String[] data = { tmp.getCategory(), tmp.getTitle(),
						tmp.getCreatedTimestamp(), tmp.getUsername(),
						tmp.getModifiedTimestamp(), tmp.getPassword(true) };
				clearInformationLabels(information);
				populateInformationLabels(information, data);
			}
		};
	}

	/**
	 * Return a MouseListener that shows a popup menu when right clicking on the entryTable.
	 * 
	 * @param table		The table that the popup menu applies to.
	 * @param popupMenu The popup menu to show.
	 * 
	 * @return A MouseAdapter.
	 */
	public MouseAdapter showPopupMenu(JTable table, JPopupMenu popupMenu) {
		return new MouseAdapter() {
			private int start = 0;
			// The mouse was pressed
			public void mousePressed(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON3) {
					// If no rows are selected, select the row that was right clicked
					if(table.getSelectedRowCount() == 0) {
						start = table.rowAtPoint(me.getPoint());
						if(start != -1)
							table.setRowSelectionInterval(start, start);
					}
					// Else if one row was selected, select the new row instead
					else if(table.getSelectedRowCount() == 1) {
						table.clearSelection();
						start = table.rowAtPoint(me.getPoint());
						if(start != -1)
							table.setRowSelectionInterval(start, start);
					}
				}
			}

			// The mouse was released
			public void mouseReleased(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON3) {
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			}
		};
	}

	/**
	 * Return a DocumentListener that will sort a table based on what is typed in a text field.
	 * 
	 * @param field The field the search term is typed in.
	 * @param table The table to be sorted.
	 * @param entries All entries to search through.
	 * @param information The labels the table populates.
	 * @param popupMenu The popup menu for the table.
	 * @param selectionStatusLabel The label that displays the number of selected rows.
	 * 
	 * @return A DocumentListener.
	 */
	public DocumentListener searchTableByField(JTextField field, JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, JLabel selectionStatusLabel) {
		return new DocumentListener() {

			public void insertUpdate(DocumentEvent e) { doSearch(); }

			public void removeUpdate(DocumentEvent e) { doSearch(); }

			public void changedUpdate(DocumentEvent e) {/* Not Applicable */ }

			private void doSearch() {
				// Get the current search
				String search = field.getText().toLowerCase();
				LinkedList<TableEntry> sorted = new LinkedList<TableEntry>();
				Object[] fields;
				boolean isMatch = false;

				// If the search is empty or the default value, add everything to sorted that is not in the recycling bin
				if(search.isEmpty() || search.equals("search...")) {
					for (TableEntry entry : entries) 
						if(!entry.getCategory().equals("[Recycling Bin]")) 
							sorted.add(entry);
				}

				// Else, search the fields for the string and add matches to sorted
				else {
					// Search only the visible fields
					if(onlySearchFieldsVisibleOnTable) {
						for(TableEntry entry : entries) {
							fields = entry.toMinimalObjectArray();
							isMatch = false;
							for(int i = 0; i < fields.length-1; i++) {
								if(((String)fields[i]).toLowerCase().contains(search)) {
									isMatch = true;
									break;
								}
							}
							if(isMatch)
								sorted.add(entry);
						}
					}
					// Search every field
					else {
						for(TableEntry entry : entries) {
							fields = entry.toFullObjectArray();
							isMatch = false;
							for(int i = 0; i < fields.length; i++) {
								if(((String)fields[i]).toLowerCase().contains(search)) {
									isMatch = true;
									break;
								}
							}
							if(isMatch)
								sorted.add(entry);
						}
					}
				}

				// Add sorted entries to the table
				resetTable(table, sorted);

				// Update the selectionStatusLabel
				selectionStatusLabel.setText("0 of "+table.getRowCount()+" selected.");
				
				// Refresh listeners
				refreshTableListeners(table, sorted, information, popupMenu, selectionStatusLabel);
			}
		};
	}

	/**
	 * Update a label to display the number of rows selected out of the total number of rows.
	 * 
	 * @param table The table with rows.
	 * @param label The label to update.
	 * 
	 * @return A ListSelectionListener.
	 */
	public ListSelectionListener updateSelectionStatus(JTable table, JLabel label) {
		return new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				label.setText(table.getSelectedRowCount()+" of "+table.getRowCount()+" selected.");
			}
		};
	}

	/**
	 * Remove all of the rows from a table.
	 * 
	 * @param table The table to clear.
	 */
	public void clearTable(JTable table) {
		int howManyRows = table.getRowCount();
		for (int i = 0; i < howManyRows; i++)
			((PasswordTableModel) table.getModel()).removeRow(0);
	}
	
	/**
	 * Add a list of entries to a table.
	 * 
	 * @param table		The table to add to.
	 * @param entries	The list of entries to add.
	 */
	public void addToTable(JTable table, LinkedList<TableEntry> entries) {
		for (TableEntry entry : entries)
			((PasswordTableModel) table.getModel()).addRow(entry);
	}
	
	/**
	 * Remove all the rows from the table and add on a new list of entries.
	 * 
	 * @param table		The table to reset.
	 * @param entries	The new entries to add.
	 */
	public void resetTable(JTable table, LinkedList<TableEntry> entries) {
		clearTable(table);
		addToTable(table, entries);
	}
	
	//****************//
	//* Tree Methods *//
	//****************//

	/**
	 * Refresh all of the listeners corresponding to the tree.
	 * 
	 * @param tree        The tree that has listeners.
	 * @param table       The table that the tree organizes.
	 * @param entries     All of the entries for the table.
	 * @param information The labels that the table updates.
	 * @param popupMenu	  The popup menu for right clicking on the table.
	 * @param selectionStatusLabel The label that displays the number of selected rows.
	 * @param searchField The search field for the window.
	 */
	public void refreshTreeListeners(JTree tree, JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, JLabel selectionStatusLabel, JTextField searchField) {
		tree.removeTreeSelectionListener(sortEntries);
		tree.removeMouseListener(showPopupMenuOnTree);

		sortEntries = sortTableBySelection(tree, table, entries, information, popupMenu, selectionStatusLabel, searchField);
		showPopupMenuOnTree = showPopupMenu(tree, popupMenu);

		tree.addTreeSelectionListener(sortEntries);
		tree.addMouseListener(showPopupMenuOnTree);
	}

	/**
	 * Return a listener that will sort the entry table based on the selection of
	 * the tree.
	 * 
	 * @param tree        The tree being selected.
	 * @param table       The table being sorted.
	 * @param entries     The entries on the table.
	 * @param information The labels that are updated by the table.
	 * @param popupMenu	  The popup menu for right clicking on the table.
	 * @param selectionStatusLabel The label the displays the number of selected rows.
	 * @param searchField The search field for the window.
	 * 
	 * @return A TreeSelectionListener.
	 */
	public TreeSelectionListener sortTableBySelection(JTree tree, JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, JLabel selectionStatusLabel, JTextField searchField) {
		return new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				// The list of sort entries
				LinkedList<TableEntry> sorted = new LinkedList<TableEntry>();

				// The selected node and parent node
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				DefaultMutableTreeNode parentNode = null;

				// If nothing is selected, show everything not in the recycling bin
				if (selectedNode == null) {
					for (TableEntry entry : entries) 
						if(!entry.getCategory().equals("[Recycling Bin]")) 
							sorted.add(entry);
				}
				else {			
					// Clear the search field
					searchField.setText("Search...");

					// Get the name of the node
					String selectedName = selectedNode.getUserObject().toString();
					String parentName = null;

					// If leaf nodes are being shown
					if(showEntriesOnTree) {
						// If the node is a leaf
						if (selectedNode.isLeaf()) {
							parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
							// If the parent node is null and the node is a leaf, there are no categories or entries
							if(parentNode == null)
								return;
							
							parentName = parentNode.getUserObject().toString();
							// If the parent is not the root, sort by title and category
							if (!parentNode.isRoot()) {
								for (TableEntry entry : entries)
									if (entry.getTitle().equals(selectedName) && entry.getCategory().equals(parentName))
										sorted.add(entry);
							}
							// Else, sort by title and an empty category
							else {
								for (TableEntry entry : entries)
									if ((entry.getTitle().isEmpty() || entry.getTitle().equals(selectedName)) && entry.getCategory().isEmpty())
										sorted.add(entry);
							}

						}
						// If the node is the root, show everything not in the recycling bin
						else if (selectedNode.isRoot()) {
							for (TableEntry entry : entries)
								if(!entry.getCategory().equals("[Recycling Bin]"))
									sorted.add(entry);
						}
						// Else, sort by category
						else {
							for (TableEntry entry : entries)
								if (entry.getCategory().equals(selectedName))
									sorted.add(entry);
						}
					}

					// If leaf nodes are not being shown
					else {
						// If the node is the root, show everything not in the recycling bin
						if (selectedNode.isRoot()) {
							for (TableEntry entry : entries)
								if(!entry.getCategory().equals("[Recycling Bin]"))
									sorted.add(entry);
						}
						// Else, sort by category
						else {
							for (TableEntry entry : entries)
								if (entry.getCategory().equals(selectedName))
									sorted.add(entry);
						}
					}
				}
				
				// Display the nodes on the table
				resetTable(table, sorted);

				// Update the selectionStatusLabel
				selectionStatusLabel.setText(0+" of "+table.getRowCount()+" selected.");

				refreshTableListeners(table, sorted, information, popupMenu, selectionStatusLabel);
			}
		};
	}

	/**
	 * Return a MouseListener that shows a popup menu when right clicking on the entryTable.
	 * 
	 * @param table		The table that the popup menu applies to.
	 * @param popupMenu The popup menu to show.
	 * 
	 * @return A MouseAdapter.
	 */
	public MouseAdapter showPopupMenu(JTree tree, JPopupMenu popupMenu) {
		return new MouseAdapter() {
			// The mouse was pressed
			public void mousePressed(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON3) {
					int selectedRow = tree.getRowForLocation(me.getX(), me.getY());
			        TreePath selectedPath = tree.getPathForLocation(me.getX(), me.getY());
			        tree.setSelectionPath(selectedPath);
			        if(selectedRow != -1)
			        	tree.setSelectionRow(selectedRow);
				}
			}

			// The mouse was released
			public void mouseReleased(MouseEvent me) {
				if(me.getButton() == MouseEvent.BUTTON3) {
					popupMenu.show(me.getComponent(), me.getX(), me.getY());
				}
			}
		};
	}
	
	/**
	 * Create the nodes for the categoryTree
	 * 
	 * @param root The root node.
	 * @param tableEntries The list of entries.
	 */
	public void createNodes(DefaultMutableTreeNode root, LinkedList<TableEntry> tableEntries) {
		LinkedList<DefaultMutableTreeNode> categories = new LinkedList<DefaultMutableTreeNode>();
		LinkedList<String> categoryNames = new LinkedList<String>();
		DefaultMutableTreeNode node = null;
		int index = -1;

		// Add the nodes to the tree
		for (TableEntry entry : tableEntries) {
			// Create the node
			if(!entry.getTitle().isEmpty())
				node = new DefaultMutableTreeNode(entry.getTitle());
			else
				node = new DefaultMutableTreeNode("[No Title]");

			// If the entry does not have a category, make it a child of the root.
			if (entry.getCategory().isEmpty())
				root.add(node);
			else {
				// Determine if the category already exists
				index = -1;
				for (int i = 0; i < categoryNames.size(); i++) {
					if (categoryNames.get(i).equals(entry.getCategory())) {
						index = i;
						break;
					}
				}

				// If the category exists, make it a child
				if (index != -1) {
					if(showEntriesOnTree)
						categories.get(index).add(node);
				}

				// Otherwise, create the category and make it a child
				else {
					categories.add(new DefaultMutableTreeNode(entry.getCategory()));
					categoryNames.add(entry.getCategory());
					root.add(categories.getLast());
					if(showEntriesOnTree)
						categories.getLast().add(node);
				}
			}
		}

		// Sort the nodes
		// Created an array of all children of root
		DefaultMutableTreeNode[] children = new DefaultMutableTreeNode[root.getChildCount()];
		for(int i = 0; i < children.length; i++)
			children[i] = (DefaultMutableTreeNode)root.getChildAt(i);

		// Remove all the children from root
		root.removeAllChildren();

		// Sort the children
		DefaultMutableTreeNode tmp;
		for(int i = 0; i < children.length; i++) {
			for(int j = i + 1; j < children.length; j++) {
				if(children[i].toString().toLowerCase().compareTo(children[j].toString().toLowerCase()) > 0) {
					tmp = children[j];
					children[j] = children[i];
					children[i] = tmp;
				}
			}
		}

		// Add the children back to root
		for(int i = 0; i < children.length; i++)
			root.add(children[i]);
	}

	//*****************//
	//* Timer Methods *//
	//*****************//

	/**
	 * An ActionListener that will clear the clipboard after 15 seconds.
	 * 
	 * @param updateLabel the label that shows the time.
	 * 
	 * @return An ActionListener that will clear the clipboard after 15 seconds.
	 */
	public ActionListener updateTime(JLabel updateLabel) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clock++;
				// Update the label
				if(clearClipboard) {
					if(maxTime-clock != 1)
						updateLabel.setText("Data copied to clipboard. Clipboard will be cleared in "+(maxTime-clock)+" seconds.");
					else
						updateLabel.setText("Data copied to clipboard. Clipboard will be cleared in "+(maxTime-clock)+" second.");
				}

				// Check if it is time to restart the clock
				if(clock >= maxTime) {
					timer.stop();
					clock = 0;
					updateLabel.setText("Right click on a row to see copying options.");
					if(clearClipboard)
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(null), null);
				}
			}
		};
	}

	//****************//
	//* Copy Methods *//
	//****************//

	/**
	 * A MouseListener that copies the specified data from a table when a menu item is selected.
	 * <p>
	 * Column name capitalization has no effect.<br>
	 * Column names include... <br>
	 * <b> TITLE </b> - The title. <br>
	 * <b> USERNAME </b> - The username. <br>
	 * <b> PASSWORD </b> - The password. <br>
	 * <b> NOTES </b> - The notes. <br>
	 * <b> ALL </b> - All fields, including those not shown on the table. <br>
	 * 
	 * @param menuItem The JMenuItem being selected.
	 * @param table The JTable to copy from.
	 * @param column The column to copy from.
	 * @param entries The entries on the table.
	 * @param hashedKey The key used to encrypt the password.
	 * @param updateLabel The label that updates the user.
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener copyTableEntry(JMenuItem menuItem, JTable table, String column, LinkedList<TableEntry> entries, JLabel updateLabel) {
		return new ActionListener() {
			public static final String TITLE = "TITLE";
			public static final String USERNAME = "USERNAME";
			public static final String PASSWORD = "PASSWORD";
			public static final String NOTES = "NOTES";
			public static final String ALL = "ALL";
			public void actionPerformed(ActionEvent me) {
				// Get the row and column
				int row = table.getSelectedRow();
				int correctedRow = 0;
				String correctedColumn = column.toUpperCase();

				// Find the index in the list that corresponds to the table
				TableEntry selectedEntry = new TableEntry();
				selectedEntry.setID((String)table.getValueAt(row, 4));
				for(int i = 0; i < entries.size(); i++) {
					if(entries.get(i).matchesID(selectedEntry)) {
						correctedRow = i;
						break;
					}
				}

				// Prepare the sensitive data
				String decryptedPassword;
				byte[] decryptedPasswordBytes;

				// Prepare all data
				Object[] allColumnsBefore;
				String[] allColumnsAfter;
				String[] names = {"Category", "Title", "Username", "Password", "Creation Time", "Last Modification Time", "Notes"};
				String combined = "";

				// Initialize the copying tools
				StringSelection stringSelection; 
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

				// Do the copying
				switch(correctedColumn) {
				case TITLE:
					stringSelection = new StringSelection(entries.get(correctedRow).getTitle());
					clipboard.setContents(stringSelection, null);
					break;

				case USERNAME:
					stringSelection = new StringSelection(entries.get(correctedRow).getUsername());
					clipboard.setContents(stringSelection, null);
					break;

				case PASSWORD:
					try {
						// Decrypt and copy the password
						decryptedPasswordBytes = AES.decrypt(Base64.getDecoder().decode(entries.get(correctedRow).getPassword(false).getBytes()), hashedKey);
						decryptedPassword = new String(decryptedPasswordBytes);
						stringSelection = new StringSelection(decryptedPassword);
						clipboard.setContents(stringSelection, null);

						// Wipe the sensitive information
						decryptedPassword = null;
						stringSelection = null;
						for(int i = 0; i < decryptedPasswordBytes.length; i++)
							decryptedPasswordBytes[i] = '\0';
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;

				case NOTES:
					stringSelection = new StringSelection(entries.get(correctedRow).getNotes());
					clipboard.setContents(stringSelection, null);
					break;

				case ALL:
					try {
						// Get all data
						allColumnsBefore = entries.get(correctedRow).toFullObjectArray();
						allColumnsAfter = new String[allColumnsBefore.length];
						for(int i = 0; i < allColumnsBefore.length; i++)
							allColumnsAfter[i] = (String)allColumnsBefore[i];

						// Decrypt the password
						decryptedPasswordBytes = AES.decrypt(Base64.getDecoder().decode(allColumnsAfter[3].getBytes()), hashedKey);
						allColumnsAfter[3] = new String(decryptedPasswordBytes);

						// Append all data together
						for(int i = 0; i < allColumnsAfter.length; i++) {
							combined+= names[i]+": "+allColumnsAfter[i];
							if(i != allColumnsAfter.length-1)
								combined += ", ";
						}

						// Copy it
						stringSelection = new StringSelection(combined);
						clipboard.setContents(stringSelection, null);

						// Wipe the sensitive data
						allColumnsAfter[3] = null;
						combined = null;
						stringSelection = null;
						for(int i = 0; i < decryptedPasswordBytes.length; i++)
							decryptedPasswordBytes[i] = '\0';
					} catch (Exception e) {
						e.printStackTrace();
					}

					break;
				}

				// Start the timer to clear the clipboard after MAX_TIME seconds
				if(clearClipboard) {
					updateLabel.setText("Data copied to clipboard. Clipboard will be cleared in "+maxTime+" seconds.");
					clock = 0;
					timer.restart();
				}
				else {
					updateLabel.setText("Data copied to clipboard. Clipboard will not be cleared.");
					clock = 0;
					timer.restart();
				}
			}
		};
	}

	/**
	 * Apply the enableMenuItemOnSelection listener to a list of components.
	 * 
	 * @param min The minimum number of selected rows the table must have.
	 * @param max The maximum number of selected rows the table may have.
	 * @param table The JTable that needs to have a selection.
	 * @param components The components that will be enabled.
	 */
	public void applyEnableListener(int min, int max, JTable table, Component... components) {
		for(int i = 0; i < components.length; i++)
			table.getSelectionModel().addListSelectionListener(enableComponentOnSelection(min, max, components[i], table));
	}

	/**
	 * Enable a component only when a row on a table is selected.
	 *
	 * @param min The minimum number of selected rows the table must have.
	 * @param max The maximum number of selected rows the table may have.
	 * @param component The component to enable.
	 * @param table The table that needs to have a selected row.
	 * 
	 * @return A ListSelectionListener.
	 */
	public ListSelectionListener enableComponentOnSelection(int min, int max, Component component, JTable table) {
		return new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				component.setEnabled(table.getSelectedRowCount() >= min && table.getSelectedRowCount() <= max);
			}
		};
	}

	/**
	 * Set a list of components to be disabled.
	 * 
	 * @param components The list of components.
	 */
	public void setEnableComponents(boolean b, Component... components) {
		for(int i = 0; i < components.length; i++)
			components[i].setEnabled(b);
	}

	//**********************//
	//* Select All Methods *//
	//**********************//

	/**
	 * Select everything on a table when a component is pressed.
	 * 
	 * @param table The table to select everything on.
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener selectAll(JTable table) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				table.selectAll();
			}
		};
	}

	//*********************************//
	//* Password Generation Listeners *//
	//*********************************//

	/**
	 * Show the window to generate a password, and add it to the table.
	 * 
	 * @param table The table to add the password to.
	 * @param entries The list of entries already on the table.
	 * @param information The information labels updated by the table. 
	 * @param popupMenu The popup menu for the table.
	 * @param selectionStatusLabel The selection status of the table.
	 * @param categoryTree The category tree.
	 * @param hashedKey The key to encrypt the password column.
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener generatePassword(JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, JLabel selectionStatusLabel, JTree categoryTree) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				// Generate the password
				char[] generatedPasswordChars = PasswordGenerator.display(DEFAULT_ECHO_CHAR, true, autoGeneratePassword, autoCheckDictionary);
				String generatedPassword;
				if(generatedPasswordChars != null)
					generatedPassword = new String(generatedPasswordChars);
				else
					generatedPassword = null;

				// If the user went through with the generation, add it to the table
				if(generatedPassword != null) {
					// Encrypt and encode the password
					try {
						generatedPassword = new String(Base64.getEncoder().encode(AES.encrypt(generatedPassword.getBytes(), hashedKey)));
					} catch (Exception e) {
						e.printStackTrace();
					}
					// Make the table entry
					TableEntry newEntry = new TableEntry("", "", "", generatedPassword, "Generated Password.");
					
					// Add it to the list, and add it to the table
					entries.add(newEntry);
					((PasswordTableModel) table.getModel()).addRow(newEntry);

					// Update the category tree
					DefaultTreeModel treeModel = (DefaultTreeModel) categoryTree.getModel();
					DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
					root.removeAllChildren();
					createNodes(root, entries);
					treeModel.reload();

					// Update the table listeners
					refreshTableListeners(table, entries, information, popupMenu, selectionStatusLabel);
					
					changesMade = true;
					if(frame.getTitle().charAt(frame.getTitle().length()-1) != '*')
						frame.setTitle(frame.getTitle()+"*");
				}

				// Wipe the sensitive data
				generatedPassword = null;
				if(generatedPasswordChars != null)
					for(int i = 0; i < generatedPasswordChars.length; i++)
						generatedPasswordChars[i] = '\0';
			}
		};
	}

	/**
	 * Show the window to generate a password list, create a category called creation time, and add that all to the table.
	 * 
	 * @param table The table to add the password to.
	 * @param entries The list of entries already on the table.
	 * @param information The information labels updated by the table. 
	 * @param popupMenu The popup menu for the table.
	 * @param selectionStatusLabel The selection status of the table.
	 * @param categoryTree The category tree.
	 * @param hashedKey The key to encrypt the password column.
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener generatePasswordList(JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, JLabel selectionStatusLabel, JTree categoryTree) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				// Get the password settings
				char[] settingsChars = PasswordGenerator.display(DEFAULT_ECHO_CHAR, false, false, false);
				if(settingsChars == null)
					return;
				boolean[] settings = new boolean[settingsChars.length];
				for(int i = 0; i < settings.length; i++)
					settings[i] = settingsChars[i] == 'T';

				String generatedPassword = "";
				for(int i = 0; i < (int)settingsChars[6]; i++) {
					generatedPassword = Password.generate((int) settingsChars[5], settings[0], settings[1], settings[2], settings[3], settings[4]);
					// Encrypt and encode the password
					try {
						generatedPassword = new String(Base64.getEncoder().encode(AES.encrypt(generatedPassword.getBytes(), hashedKey)));
					} catch (Exception e) {
						e.printStackTrace();
					}
					// Make the table entry, and it to the list, and add it to the table
					TableEntry newEntry = new TableEntry("", "", "", generatedPassword, "Generated Password #"+(i+1)+".");
					newEntry.setCategory(newEntry.getCreatedTimestamp());
					entries.add(newEntry);
					((PasswordTableModel) table.getModel()).addRow(newEntry);

					// Update the category tree
					DefaultTreeModel treeModel = (DefaultTreeModel) categoryTree.getModel();
					DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();
					root.removeAllChildren();
					createNodes(root, entries);
					treeModel.reload();

					// Update the table listeners
					refreshTableListeners(table, entries, information, popupMenu, selectionStatusLabel);
					
					changesMade = true;
					if(frame.getTitle().charAt(frame.getTitle().length()-1) != '*')
						frame.setTitle(frame.getTitle()+"*");
				}

				// Wipe the sensitive data
				generatedPassword = null;
			}

		};
	}

	//***************************//
	//* Entry Addition Listener *//
	//***************************//
	
	/**
	 * Show the window to add an entry.
	 * 
	 * @param table The table to add the password to.
	 * @param entries The list of entries already on the table.
	 * @param information The information labels updated by the table. 
	 * @param popupMenu The popup menu for the table.
	 * @param component The component that must be enabled for this action.
	 * @param selectionStatusLabel The selection status of the table.
	 * @param categoryTree The category tree.
	 * @param hashedKey The key to encrypt the password column.
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener addEntry(JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, Component component, JLabel selectionStatusLabel, JTree categoryTree) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				if(component.isEnabled()) {
					// Deselect everything from the table
					table.clearSelection();

					// Get the root of the tree for determining the existing categories
					DefaultTreeModel treeModel = (DefaultTreeModel) categoryTree.getModel();
					DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();

					// Create a linked list of categories
					LinkedList<String> categories = new LinkedList<String>();
					for(int i = 0; i < root.getChildCount(); i++) {
						if(root.getChildAt(i).getChildCount() != 0)
							categories.add(root.getChildAt(i).toString());
					}

					// Display the entry editor
					Object tmp = categoryTree.getLastSelectedPathComponent();
					TableEntry newEntry;
					if(tmp != null && !root.equals(tmp))
						newEntry = TableEntryEditor.display(DEFAULT_ECHO_CHAR, hashedKey, TableEntryEditor.ADD, categories.toArray(), autoGeneratePassword, autoCheckDictionary, tmp.toString());
					else
						newEntry = TableEntryEditor.display(DEFAULT_ECHO_CHAR, hashedKey, TableEntryEditor.ADD, categories.toArray(), autoGeneratePassword, autoCheckDictionary);

					if(newEntry != null) {
						// Add the new entry to the table and list
						entries.add(newEntry);
						((PasswordTableModel) table.getModel()).addRow(newEntry);

						// Update the category tree
						root.removeAllChildren();
						createNodes(root, entries);
						treeModel.reload();

						// Update the table listeners
						refreshTableListeners(table, entries, information, popupMenu, selectionStatusLabel);

						changesMade = true;
						if(frame.getTitle().charAt(frame.getTitle().length()-1) != '*')
							frame.setTitle(frame.getTitle()+"*");
					}
				}
			}
		};
	}

	//*******************************//
	//* Entry Modification Listener *//
	//*******************************//
	
	/**
	 * Show the window to edit an entry.
	 * 
	 * @param table The table to add the password to.
	 * @param entries The list of entries already on the table.
	 * @param information The information labels updated by the table. 
	 * @param popupMenu The popup menu for the table.
	 * @param menuItem The menu item this listener will associate with.
	 * @param selectionStatusLabel The selection status of the table.
	 * @param categoryTree The category tree.
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener editEntry(JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, JMenuItem menuItem, JLabel selectionStatusLabel, JTree categoryTree) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				if(menuItem.isEnabled()) {
					// Get the selected row
					int row = table.getSelectedRow();
					int correctedRow = 0;

					// Find the index in the list that corresponds to the table
					TableEntry selectedEntry = new TableEntry();
					selectedEntry.setID((String)table.getValueAt(row, 4));
					for(int i = 0; i < entries.size(); i++) {
						if(entries.get(i).matchesID(selectedEntry)) {
							correctedRow = i;
							break;
						}
					}

					// Get the root of the tree for determining the existing categories
					DefaultTreeModel treeModel = (DefaultTreeModel) categoryTree.getModel();
					DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();

					// Create a linked list of categories
					LinkedList<String> categories = new LinkedList<String>();
					for(int i = 0; i < root.getChildCount(); i++) {
						if(root.getChildAt(i).getChildCount() != 0)
							categories.add(root.getChildAt(i).toString());
					}

					// Decode and decrypt the password
					byte[] decoded = Base64.getDecoder().decode(entries.get(correctedRow).getPassword(false).getBytes());
					byte[] decryptedBytes;
					String decrypted = "";
					try {
						decryptedBytes = AES.decrypt(decoded, hashedKey);
						decrypted = new String(decryptedBytes);
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}

					// Display the entry editor
					TableEntry newEntry = TableEntryEditor.display(
							DEFAULT_ECHO_CHAR, hashedKey, TableEntryEditor.EDIT, categories.toArray(), autoGeneratePassword, autoCheckDictionary,
							entries.get(correctedRow).getCategory(), entries.get(correctedRow).getTitle(),
							entries.get(correctedRow).getUsername(), decrypted,
							entries.get(correctedRow).getNotes());

					// Wipe the sensitive data
					decrypted = null;
					for(int i = 0; i < decryptedBytes.length; i++)
						decryptedBytes[i] = '\0';

					if(newEntry != null) {
						// Create a backup of the entry
						TableEntry oldEntry = new TableEntry();
						oldEntry.setCategory(entries.get(correctedRow).getCategory());
						oldEntry.setTitle(entries.get(correctedRow).getTitle());
						oldEntry.setUsername(entries.get(correctedRow).getUsername());
						oldEntry.setPassword(entries.get(correctedRow).getPassword(false));
						oldEntry.setNotes(entries.get(correctedRow).getNotes());
						oldEntry.setCreatedTimestamp(entries.get(correctedRow).getCreatedTimestamp());
						oldEntry.setModifiedTimestamp(entries.get(correctedRow).getModifiedTimestamp());

						// Find the old version in the list and remove it
						for(int i = 0; i < entries.size(); i++) {
							if(entries.get(i).equals(oldEntry)) {
								entries.remove(i);
								break;
							}
						}

						// Add the new entry to the list then table
						entries.add(newEntry);
						resetTable(table, entries);

						// Update the category tree
						root.removeAllChildren();
						createNodes(root, entries);
						treeModel.reload();

						// Update the table listeners
						refreshTableListeners(table, entries, information, popupMenu, selectionStatusLabel);
						
						changesMade = true;
						if(frame.getTitle().charAt(frame.getTitle().length()-1) != '*')
							frame.setTitle(frame.getTitle()+"*");
					}
				}
			}
		};
	}

	//**************************//
	//* Entry Removal Listener *//
	//**************************//
	
	/**
	 * Show the window to remove entries.
	 * 
	 * @param table The table to add the password to.
	 * @param entries The list of entries already on the table.
	 * @param information The information labels updated by the table. 
	 * @param popupMenu The popup menu for the table.
	 * @param menuItem The menu item this listener will associate with.
	 * @param selectionStatusLabel The selection status of the table.
	 * @param categoryTree The category tree.
	 * @param hashedKey The key to encrypt the password column.
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener removeEntry(JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, JMenuItem menuItem, JLabel selectionStatusLabel, JTree categoryTree) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				if(menuItem.isEnabled()) {
					// Get the selected rows
					int rows[] = table.getSelectedRows();
					int correctedRows[] = new int[rows.length];

					// Find the indices in the list that correspond to the table selection
					for(int i = 0; i < rows.length; i++) {
						TableEntry selectedEntry = new TableEntry();
						selectedEntry.setID((String)table.getValueAt(rows[i], 4));
						for(int j = 0; j < entries.size(); j++) {
							if(entries.get(j).matchesID(selectedEntry)) {
								correctedRows[i] = j;
								break;
							}
						}
					}
					
					// Get the selected table entries
					TableEntry[] selectedEntries = new TableEntry[correctedRows.length];
					for(int i = 0; i < correctedRows.length; i++)
						selectedEntries[i] = new TableEntry(entries.get(correctedRows[i]));
					
					boolean confirm = TableEntryRemover.display(selectedEntries);

					// The user confirmed removal
					if(confirm) {
						// Get the root of the tree for updating
						DefaultTreeModel treeModel = (DefaultTreeModel) categoryTree.getModel();
						DefaultMutableTreeNode root = (DefaultMutableTreeNode)treeModel.getRoot();

						// Remove all rows from the table
						clearTable(table);
						
						// Sort the corrected rows to be in descending order
						int tmp;
						for(int i = 0; i < correctedRows.length; i++) {
							for(int j = i + 1; j < correctedRows.length; j++) {
								if(correctedRows[i] < correctedRows[j]) {
									tmp = correctedRows[j];
									correctedRows[j] = correctedRows[i];
									correctedRows[i] = tmp;
								}
							}
						}
						
						// Move entries to the recycling bin, or remove them if they are already there
						int currIndex;
						for(int i = 0; i < correctedRows.length; i++) {
							currIndex = correctedRows[i];
							if(entries.get(currIndex).getCategory().equals("[Recycling Bin]"))
								entries.remove(currIndex);
							else
								entries.get(currIndex).setCategory("[Recycling Bin]");
						}
						
						
						// Add remaining entries back to the table if they are not in the recycling bin
						for (TableEntry entry : entries)
							if(!entry.getCategory().equals("[Recycling Bin]"))
								((PasswordTableModel) table.getModel()).addRow(entry);

						// Update the category tree
						root.removeAllChildren();
						createNodes(root, entries);
						treeModel.reload();

						// Update the table listeners
						refreshTableListeners(table, entries, information, popupMenu, selectionStatusLabel);
						
						changesMade = true;
						if(frame.getTitle().charAt(frame.getTitle().length()-1) != '*')
							frame.setTitle(frame.getTitle()+"*");
					}
				}
			}
		};
	}

	//****************//
	//* Save Methods *//
	//****************//
	
	/**
	 * Encrypt the table, save it, and encrypt the file.
	 * 
	 * @param entries The entries on the table.
	 * @param hashedKey The key for encrypting.
	 * @param menuItem The menu item this listener associates with.
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener saveFileAs(LinkedList<TableEntry> entries, JMenuItem menuItem, JTree categoryTree) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				if(menuItem.isEnabled()) {
					// Create an invisible JFrame to inherit the icon image 
					JFrame parent = new JFrame();
					parent.setIconImage(new ImageIcon("..\\resources\\windowIcon.png").getImage());
					
					// Display a file chooser
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File("..\\"));
					fileChooser.setFileFilter(new FileNameExtensionFilter("Vault Files (.vlt)", "vlt"));
					fileChooser.setDialogTitle("Select Vault");
					int option = fileChooser.showSaveDialog(parent);
					
					if(option == JFileChooser.APPROVE_OPTION) {
						// Get the destination for the file
						String filePath = fileChooser.getSelectedFile().getAbsolutePath();
						filePath = (getStringAfterLastOccurrence(filePath, '.').equals("vlt"))? filePath : filePath+".vlt";
						vaultName = filePath;
						
						// Create the file, if it does not already exist
						File file = Files.open(filePath);
						if(file == null)
							Files.create(filePath);
						
						saveToFile(entries, filePath);
						
						// Update the category tree
						String name = (getStringAfterLastOccurrence(filePath, '\\'));
						DefaultTreeModel treeModel = (DefaultTreeModel) categoryTree.getModel();
						DefaultMutableTreeNode root = new DefaultMutableTreeNode(name.substring(0, name.length()-4));
						treeModel.setRoot(root);
						createNodes(root, entries);
						treeModel.reload();
					}
					// Update the changes made variable
					changesMade = false;
					if(frame.getTitle().charAt(frame.getTitle().length()-1) == '*')
						frame.setTitle(frame.getTitle().substring(0, frame.getTitle().length()-1));
					
					// Update the settings for the most recent vault
					writeSettings();
				}
			}
		};
	}

	/**
	 * Encrypt the table, save it, and encrypt the file.
	 * 
	 * @param entries The entries on the table.
	 * @param hashedKey The key for encrypting.
	 * @param menuItem The menu item this listener associates with.
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener saveFile(LinkedList<TableEntry> entries, Component menuItem) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				if(menuItem.isEnabled()) {
					saveToFile(entries, vaultName);
					
					changesMade = false;
					if(frame.getTitle().charAt(frame.getTitle().length()-1) == '*')
						frame.setTitle(frame.getTitle().substring(0, frame.getTitle().length()-1));
				}
			}
		};
	}
	
	/**
	 * Save the entries in the list to a file at the specified path.
	 * 
	 * @param entries The list of TableEntries
	 * @param filePath The path of the file.
	 */
	public void saveToFile(LinkedList<TableEntry> entries, String filePath) {
		// Prepare to write
		String currLine = "";
		try {
			byte[] firstLine = new byte[32];
			new SecureRandom().nextBytes(firstLine);
			Files.write(filePath, new String(Base64.getEncoder().encode(AES.encrypt(firstLine, hashedKey))), false);
			Files.write(filePath, new String(Base64.getEncoder().encode(AES.encrypt(filePath.getBytes(), hashedKey))), true);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// Add each entry to the file
		for(TableEntry entry : entries) {
			// Reset currLine
			currLine = "";

			// Get all the information
			currLine += entry.getID() + "\n";
			currLine += entry.getCategory() + "\n";
			currLine += entry.getTitle() + "\n";
			currLine += entry.getUsername() + "\n";
			currLine += entry.getPassword(false) + "\n";
			currLine += entry.getNotes() + "\n";
			currLine += entry.getCreatedTimestamp() + "\n";
			currLine += entry.getModifiedTimestamp();

			// Encrypt the information
			try {
				currLine = new String(Base64.getEncoder().encode(AES.encrypt(currLine.getBytes(), hashedKey)));
			} catch (Exception e) {
				e.printStackTrace();
			}

			Files.write(vaultName, currLine, true);
			
			changesMade = false;
			if(frame.getTitle().charAt(frame.getTitle().length()-1) == '*')
				frame.setTitle(frame.getTitle().substring(0, frame.getTitle().length()-1));
		}
	}
	
	//************************//
	//* Open and New Methods *//
	//************************//

	/**
	 * Show the window for opening a new entry.
	 * <br>
	 * A check is made for any unsaved changes.
	 * 
	 * @param table The table to of entries.
	 * @param entries The list of entries already on the table.
	 * @param information The information labels updated by the table. 
	 * @param popupMenu The popup menu for the table.
	 * @param component The component this listener will associate with.
	 * @param selectionStatusLabel The selection status of the table.
	 * @param categoryTree The category tree.
	 * @param components The components to enable as a result of this action.
	 * @return
	 */
	public ActionListener openOrNewFile(int option, JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, Component component, JLabel selectionStatusLabel, JTree categoryTree, Component... components) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				if(component.isEnabled()) {
					if(changesMade) {
						int option = JOptionPane.showConfirmDialog(null, "There are unsaved changes. Would you like to save before entering a new vault?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
						if(option == JOptionPane.YES_OPTION)			
							saveToFile(entries, vaultName);
						
						else if(option == JOptionPane.NO_OPTION)
							;
						
						else
							return;
					}

					// Get input from the user for file and master password
					Object[] selectedInfo;
					if(rememberLastOpenedVault)
						selectedInfo = VaultOpener.display(DEFAULT_ECHO_CHAR, option, autoCheckDictionary, lastOpenedVault);
					else
						selectedInfo = VaultOpener.display(DEFAULT_ECHO_CHAR, option, autoCheckDictionary);
					if(selectedInfo != null) {
						// Update the hashedKey
						hashedKey = (byte[])selectedInfo[0];
						// Apply the password mask to the entryTable
						PaddedCellRenderer paddedCellRenderer = new PaddedCellRenderer();
						PasswordCellRenderer passwordCellRenderer = new PasswordCellRenderer();
						passwordCellRenderer.setHashedPassword(hashedKey);
						table.setDefaultRenderer(Object.class, paddedCellRenderer);
						table.getColumnModel().getColumn(2).setCellRenderer(passwordCellRenderer);
						table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

						// Remove all rows from the table
						clearTable(table);

						// Update the table entries and add them to the table
						LinkedList<TableEntry> newData = new LinkedList<TableEntry>(decryptLines(selectedInfo, components));
						entries.clear();
						for(TableEntry entry : newData) {
							entries.add(entry);
							((PasswordTableModel)table.getModel()).addRow(entry);
						}

						// Create the categories based on the entries
						String name = vaultName;
						DefaultTreeModel treeModel = (DefaultTreeModel) categoryTree.getModel();
						DefaultMutableTreeNode root = new DefaultMutableTreeNode(getStringAfterLastOccurrence(name.substring(0, name.length()-4), '\\'));
						treeModel.setRoot(root);
						createNodes(root, entries);
						treeModel.reload();

						// Update the table listeners
						refreshTableListeners(table, entries, information, popupMenu, selectionStatusLabel);

						// Update the trackers
						changesMade = false;
						lastOpenedVault = vaultName;
						if(frame.getTitle().charAt(frame.getTitle().length()-1) == '*')
							frame.setTitle(frame.getTitle().substring(0, frame.getTitle().length()-1));
					}
				}
			}
		};
	}

	/**
	 * Decrypt lines that are encoded and encrypted using the hashedKey
	 * 
	 * @param lines The Object[] of lines
	 * 
	 * @return A LinkedList<TableEntry> composed of information from lines.
	 */
	public LinkedList<TableEntry> decryptLines(Object[] lines, Component... components) {
		LinkedList<TableEntry> toReturn = new LinkedList<TableEntry>();
		
		if(lines.length < 2)
			return toReturn;
		
		// Convert lines to a String[], skipping the first two line
		String[] asString = new String[lines.length-2];
		for(int i = 2; i < lines.length; i++)
			asString[i-2] = (String)lines[i];
		
		// Prepare the data for decrypting
		BufferedReader lineReader;
		BufferedReader dataReader;
		String line;
		String id, category, title, username, password, notes, created, modified;
		TableEntry currEntry;
		
		// Decrypt each line of the file
		for(int i = 0; i < asString.length; i++) {
			try {
				lineReader = new BufferedReader(new StringReader(asString[i]));
				// Read the next line
				line = lineReader.readLine();

				// Decrypt the line
				line = new String(AES.decrypt(Base64.getDecoder().decode(line), hashedKey));

				// Insert a BufferedReader into the decrypted line
				dataReader = new BufferedReader(new StringReader(line));

				// Read the individual lines
				id = dataReader.readLine();
				category = dataReader.readLine();
				title = dataReader.readLine();
				username = dataReader.readLine();
				password = dataReader.readLine(); // This is still encrypted and encoded
				notes = dataReader.readLine();
				created = dataReader.readLine();
				modified = dataReader.readLine();

				// Create a table entry
				currEntry = new TableEntry(category, title, username, password, notes);
				currEntry.setID(id);
				currEntry.setCreatedTimestamp(created);
				currEntry.setModifiedTimestamp(modified);

				// Add the table entry to the list
				toReturn.add(currEntry);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Enable the buttons for saving, and update the last opened vault
		try {
			vaultName = new String(AES.decrypt(Base64.getDecoder().decode(((String)lines[1]).getBytes()), hashedKey));
			lastOpenedVault = vaultName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		fileOpened = true;
		toggleFunctionEnabled(true, components);
		writeSettings();
		
		return toReturn;
	}

	//***********************//
	//* Lock Vault Listener *//
	//***********************//
	
	/**
	 * Lock a vault and hide its contents.
	 * 
	 * @param table The table to of entries.
	 * @param entries The list of entries already on the table.
	 * @param information The information labels updated by the table. 
	 * @param popupMenu The popup menu for the table.
	 * @param component The component this listener will associate with.
	 * @param selectionStatusLabel The selection status of the table.
	 * @param categoryTree The category tree.
	 * @param components The components to enable as a result of this action.
	 * 
	 * @return An ActionListener.
	 */
	public ActionListener lockVault(JTable table, LinkedList<TableEntry> entries, JLabel[] information, JPopupMenu popupMenu, Component component, JLabel selectionStatusLabel, JTree categoryTree, Component... components) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(component.isEnabled() && doClosingSequence(entries)) {
					// Remove everything from the table
					clearTable(table);
					
					// Remove everything from entries
					entries.clear();
					
					// Clear the category tree
					DefaultTreeModel treeModel = (DefaultTreeModel) categoryTree.getModel();
					DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
					treeModel.setRoot(root);
					treeModel.reload();
					
					// Refresh the table listeners
					refreshTableListeners(table, entries, information, popupMenu, selectionStatusLabel);
					
					// Set the required components to be disabled
					setEnableComponents(false, components);
					
					// Programmatically close and deselect the menu
					try {
						Robot r = new Robot();
						r.keyPress(KeyEvent.VK_ESCAPE);
						r.keyRelease(KeyEvent.VK_ESCAPE);
						r.keyPress(KeyEvent.VK_ESCAPE);
						r.keyRelease(KeyEvent.VK_ESCAPE);
					} catch (AWTException e1) {
						e1.printStackTrace();
					}
					
				}
			}
		};
	}

	//******************//
	//* Print Listener *//
	//******************//
	
	/**
	 * Print a vault at a printer.
	 * 
	 * @param entries The entries to add on the PDF
	 * 
	 * @return A MouseAdapter.
	 */
	public ActionListener printVault(LinkedList<TableEntry> entries, JTable table) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent me) {
				try {
					// Filter out the entries in the recycling bin
					LinkedList<TableEntry> filtered = new LinkedList<TableEntry>();
					for(TableEntry entry : entries) {
						if(!entry.getCategory().equals("[Recycling Bin]"))
							filtered.add(entry);
					}
					
					// Convert the linked list to an array
					TableEntry[] sorted = filtered.toArray(new TableEntry[filtered.size()]);
					TableEntry tmp = new TableEntry();

					// Sort the array of TableEntry
					for(int i = 0; i < sorted.length; i++) {
						for(int j = i + 1; j < sorted.length; j++) {
							if(sorted[i].compareTo(sorted[j]) > 0) {
								tmp = new TableEntry(sorted[j]);
								sorted[j] = new TableEntry(sorted[i]);
								sorted[i] = new TableEntry(tmp);
							}
						}
					}

					// Create the printer job
					PrinterJob printerJob = PrinterJob.getPrinterJob();
					
					// Create the table and add it to the queue
					PrintableTable printableTable = new PrintableTable(
							getStringAfterLastOccurrence(vaultName.substring(0, vaultName.length()-4), '\\'),
							sorted,
							hashedKey);
					printerJob.setPrintable(printableTable);
					
					// Show the print dialog
					boolean confirm = printerJob.printDialog();
					
					// Do the printing
					if(confirm) {
						JOptionPane.showInternalOptionDialog(null, "Press Okay to create the document. This may take a while", "Print Process", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[] {"Okay"}, null);
						printerJob.print();
					}
					
					// Wipe the sensitive information
					printableTable = null;

				}
				catch (PrinterAbortException e) {
					;
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, "An error occured while printing.", "Printing Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				
			}
		};
	}

	//*************************//
	//* Open Website Listener *//
	//*************************//
	
	/**
	 * Open a link in this system's default tool for opening HTML files.
	 * 
	 * @param link The web address to connect to.
	 * 
	 * @return An ActionListener.
	 */
	public ActionListener openLink(String link) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					URL url = new URL(link);
					Desktop desktop = Desktop.isDesktopSupported()? Desktop.getDesktop() : null;
					if(desktop != null)
						desktop.browse(url.toURI());
				} catch (IOException | URISyntaxException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Link Error", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
			}
		};
	}
	
	//***********************//
	//* Show About Listener *//
	//***********************//
	
	/**
	 * Show the about window.
	 * 
	 * @return An ActionListener.
	 */
	public ActionListener showAbout() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				About.display();
			}
		};
	}

	//********************//
	//* Settings Methods *//
	//********************//
	
	/**
	 * Show the settings window.
	 * 
	 * @return An ActionListener.
	 */
	public ActionListener showSettings(JTree categoryTree, LinkedList<TableEntry> entries) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// Display the settings window with the current settings
				Object[] newSettings = Settings.display(clearClipboard, maxTime, autoGeneratePassword, autoCheckDictionary, rememberLastOpenedVault, showEntriesOnTree, onlySearchFieldsVisibleOnTable);
								
				// Update the settings
				if(newSettings != null) {
					try {
						int index = 0;
						clearClipboard = (boolean) newSettings[index++];
						maxTime = (int) newSettings[index++];
						autoGeneratePassword = (boolean) newSettings[index++];
						autoCheckDictionary = (boolean) newSettings[index++];
						rememberLastOpenedVault = (boolean) newSettings[index++];
						showEntriesOnTree = (boolean) newSettings[index++];
						onlySearchFieldsVisibleOnTable = (boolean) newSettings[index++];
						writeSettings();
						
						// Update the category tree
						String name = (getStringAfterLastOccurrence(vaultName, '\\'));
						DefaultTreeModel treeModel = (DefaultTreeModel) categoryTree.getModel();
						DefaultMutableTreeNode root = new DefaultMutableTreeNode(name.substring(0, name.length()-4));
						treeModel.setRoot(root);
						createNodes(root, entries);
						treeModel.reload();
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}
		};
	}
	
	/**
	 * Read the settings from a file formatted similar to .ini but it's really not.
	 */
	public void readSettings() {
		try {
			// Insert a Scanner into the file
			Scanner fileScanner = new Scanner(Files.open("..\\resources\\settings.ini"));
			String workingVaultName = "";
			String currLine = "";
			
			// Read all the numbers from the file
			LinkedList<Integer> nums = new LinkedList<Integer>();
			while(fileScanner.hasNext()) {
				// If it is an int, read
				if(fileScanner.hasNextInt())
					nums.add(fileScanner.nextInt());
				
				// Else check for the vault name
				else {
					currLine = fileScanner.next();
					// If there is a quote at the beginning of the token, start recording the name
					if(currLine.charAt(0) == '"') {
						// Append the first token
						workingVaultName = currLine.substring(1);
						
						// If there are no spaces, then break
						if(currLine.charAt(currLine.length()-1) == '"') {
							workingVaultName = workingVaultName.substring(0, workingVaultName.length()-1);
						}
						else {
							workingVaultName += " ";

							// Read the next token
							while(fileScanner.hasNext()) {
								currLine = fileScanner.next();

								// Append characters that are not a quote
								for(int i = 0; i < currLine.length(); i++) {
									if(currLine.charAt(i) != '"')
										workingVaultName += currLine.charAt(i)+"";
								}

								// Append a space or break
								if(currLine.charAt(currLine.length()-1) == '"')
									break;
								else
									workingVaultName+=" ";
							}
						}
					}
				}
			}
			
			// Apply what was read to the settings variables
			int index = 0;
			clearClipboard = (nums.get(index++)==1);
			maxTime = nums.get(index++);
			autoGeneratePassword = (nums.get(index++)==1);
			autoCheckDictionary = (nums.get(index++)==1);
			rememberLastOpenedVault = (nums.get(index++)==1);
			showEntriesOnTree = (nums.get(index++)==1);
			onlySearchFieldsVisibleOnTable = (nums.get(index++)==1);
			lastOpenedVault = workingVaultName;
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "An error occured while reading the settings.", "Settings error", JOptionPane.ERROR_MESSAGE);
			writeSettings();
			e.printStackTrace();
		}
	}

	/**
	 * Write the settings in a way similar to .ini but really isn't.
	 */
	public void writeSettings() {
		String filePath = "..\\resources\\settings.ini";
		
		Files.write(filePath, "[Clipboard]", false);
		Files.write(filePath, (clearClipboard)? "clearClipboard = 1" : "clearClipboard = 0", true);
		Files.write(filePath, "clipboardDelay = "+maxTime, true);
		
		Files.write(filePath, "\n[EntryCreation]", true);
		Files.write(filePath, (autoGeneratePassword)? "autoGenerate = 1" : "autoGenerate = 0", true);
		Files.write(filePath, (autoCheckDictionary)? "autoDictionary = 1" : "autoDictionary = 0", true);
		
		Files.write(filePath, "\n[Personalization]", true);
		Files.write(filePath, (rememberLastOpenedVault)? "rememberLastVault = 1" : "rememberLastVault = 0", true);
		Files.write(filePath, "lastVault = \""+vaultName+"\"", true);

		Files.write(filePath, "\n[CategoryTree]", true);
		Files.write(filePath, (showEntriesOnTree)? "showEntries = 1" : "showEntries = 0", true);

		Files.write(filePath, "\n[Search]", true);
		Files.write(filePath, (onlySearchFieldsVisibleOnTable)? "onlyIncludeVisibleFields = 1" : "onlyIncludeVisibleFields = 0", true);
	}

	//***********************************//
	//* Change Master Password Listener *//
	//***********************************//
	public ActionListener showPasswordChange(LinkedList<TableEntry> entries, PasswordCellRenderer passwordCellRenderer, JTable table, JLabel[] information, JPopupMenu popupMenu, JLabel selectionStatus) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				table.clearSelection();
				byte[] newHashedKey = PasswordChange.display(vaultName, DEFAULT_ECHO_CHAR, autoCheckDictionary);
				if(newHashedKey != null) {
					JOptionPane.showMessageDialog(null, "PassLolo will now re-encrypt and close. The master password will be updated once run again. This may take a while.", "Restart Required", JOptionPane.INFORMATION_MESSAGE);
					
					// Re-encrypt the entries with the new key
					LinkedList<TableEntry> updated = new LinkedList<TableEntry>();
					TableEntry tmp = new TableEntry();
					try {
						for(TableEntry entry : entries) {
							tmp = new TableEntry(entry);
							tmp.setPassword(Base64.getEncoder().encodeToString(AES.encrypt(AES.decrypt(Base64.getDecoder().decode(entry.getPassword(false)), hashedKey), newHashedKey)));
							updated.add(tmp);
						}
						// Update the hashed key
						hashedKey = newHashedKey;
						
						// Save the new table to the file
						saveToFile(updated, vaultName);
						
						// Tell the user that the program needs to be restart
						System.exit(0);
					} catch(Exception e) {
						JOptionPane.showMessageDialog(null, "An error occured while changing the master password.", "Encryption Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
	}
}
