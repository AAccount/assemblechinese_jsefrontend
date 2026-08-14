package dt.asm.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;

import dt.asm.App;
import dt.asm.DbService;
import dt.asm.parser.IdsParser;
import dt.asm.ui.UiUtils.Expansion;
import dt.asm.ui.UiUtils.Neighbor;

public class UiMain
{
	private static final Logger logger = Logger.getLogger(UiMain.class.getName());

	private static final String UI_ROOT = "root";
	private static final String UI_ENTRY = "entry";
	private static final String UI_MODE = "entry mode";
	private static final String UI_MODE_DISASM = "disassemble";
	private static final String UI_MODE_ASM = "assemble";
	private static final String MENU_SQLITE_INIT = "initalize sqlite";
	private static final String FLAG_MENU_UI_PREFIX = "menu ui flag";
	private static final String JMENU_ITEM_UI_DELIM = ";";
	private static final String UI_BUTTON_PRESET_PREFIX = "preset part";
	private static final String UI_RESULTS = "results";

	private static final int UI_ROW_ENTRY = 0;
	private static final int UI_ROW_PREFIX = 1;
	private static final int UI_ROW_TOP = 2;
	private static final int UI_ROW_FEET_ROOT_SIDE = 3;
	private static final int UI_ROW_COVERING = 4;
	private static final int UI_ROW_RESULTS = 5;
	
	private static final List<String> PREFIXES = List.of("氵", "扌", "", "忄", "虫", "申", "糸", "彳", "亻", "礻", "禾(科)", "士(壤)");
	private static final List<String> TOPS = List.of("⺈", "𡭔", "爫", "𦥯", "", "𠂉", "", "冖", "覀", "𥫗", "宀");
	private static final List<String> FEET_ROOT_SIDE = List.of("𧘇", "八(真)", "灬", "尸", "疒", "广","廴", "辶");
	private static final List<String> COVERINGS = List.of("𠘨 (風)", "匚", "戊(戚)", "⺆(調)", "");

	private DbService db;
	private final JTextField uiEntry;
	private final JToggleButton uiMode;
	private final JMenu flagMenu;
	private final JTextArea results;
	private String asmText = "";
	private String disasmText = "";

	public UiMain()
	{
		try 
		{
			this.db = new DbService();
		}
		catch (Exception e) 
		{
			logger.severe(UiUtils.printStackTrace(e));
			UiUtils.exceptionPopup(e);
		}
		final int ENTRY_INITIAL_WIDTH = 20;
		this.uiEntry = new JTextField(ENTRY_INITIAL_WIDTH);
		this.uiMode = new JToggleButton(UI_MODE_ASM);
		this.flagMenu = new JMenu("Flags");
		this.results = new JTextArea();
	}

	public void render()
	{
		final JFrame window = new JFrame("Assemble Chinese " + App.VERSION);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final JPanel root = new JPanel(new GridBagLayout());
		root.setName(UI_ROOT);
		root.setBorder(UiConstants.TRACER());
		renderEntry(root);
		renderPreset(UI_ROW_PREFIX, PREFIXES, root);
		renderPreset(UI_ROW_TOP, TOPS, root);
		renderPreset(UI_ROW_FEET_ROOT_SIDE, FEET_ROOT_SIDE, root);
		renderPreset(UI_ROW_COVERING, COVERINGS, root);
		renderResults(root);

		window.add(root);
		window.setJMenuBar(renderMenu());
		window.pack();
		window.setVisible(true);
	}

	private void renderResults(JPanel root)
	{
		results.setName(UI_RESULTS);
		results.setEditable(false);
		results.setLineWrap(true);
		results.setFont(UiUtils.makeFont(results, UiConstants.FONT_MEDIUM));

		final Insets insets = UiUtils.makeInsets(Set.of(Neighbor.TOP));
		root.add(results, UiUtils.makeGridConstraint(UI_ROW_RESULTS, 0, Expansion.BOTH, true, insets));
	}

	private JMenuBar renderMenu()
	{
		final JMenuBar menuBar = new JMenuBar();
		final JMenu sqliteMenu = new JMenu("SQLite");
		sqliteMenu.setMnemonic(KeyEvent.VK_S);
		sqliteMenu.getAccessibleContext().setAccessibleDescription("Modify the underlying sqlite dictionary.");

		final JMenuItem sqliteInit = new JMenuItem("Initialize with IDS.txt");
		sqliteInit.setMnemonic(KeyEvent.VK_I);
		sqliteInit.setName(MENU_SQLITE_INIT);
		sqliteInit.addActionListener(e -> {
			handleMenuSqliteInit();
		});
		sqliteMenu.add(sqliteInit);

		flagMenu.setMnemonic(KeyEvent.VK_F);
		flagMenu.getAccessibleContext().setAccessibleDescription("Toggle behind the scenes flags.");
		renderFlagMenu();

		menuBar.add(sqliteMenu);
		menuBar.add(flagMenu);
		return menuBar;
	}

	private void renderFlagMenu()
	{
		flagMenu.removeAll();
		for(final String flagName : UiConstants.allFlags())
		{
			final String label = (UiConstants.getFlag(flagName) ? "Disable" : "Enable") + " " + flagName;
			final JMenuItem flagItem = new JMenuItem(label);
			
			flagItem.setName(FLAG_MENU_UI_PREFIX + JMENU_ITEM_UI_DELIM + flagName);
			flagItem.addActionListener(event -> {
				final JComponent source = (JComponent)event.getSource();
				final String flag = source.getName().substring(FLAG_MENU_UI_PREFIX.length()+JMENU_ITEM_UI_DELIM.length());
				UiConstants.toggleFlag(flag);
				renderFlagMenu();
			});
			flagMenu.add(flagItem);
		}
	}

	private void renderPreset(int row, List<String> presets, JPanel root)
	{
		final Insets insets = UiUtils.makeInsets(Set.of(Neighbor.EVERYWHERE));

		final JPanel presetButtons = new JPanel(new GridBagLayout());
		presetButtons.setBorder(UiConstants.TRACER());
		final JScrollPane scrollPane = new JScrollPane(presetButtons);
		int col = 0;
		for(final String preset : presets)
		{
			final JButton presetButton = new JButton();
			presetButton.setText(preset);
			final String first = Character.toString(preset.codePointAt(0));
			presetButton.setName(UI_BUTTON_PRESET_PREFIX + first);
			presetButton.addActionListener(event -> {
				final JComponent source = (JComponent)event.getSource();
				final String part = Character.toString(source.getName().codePointAt(UI_BUTTON_PRESET_PREFIX.length()));
				final String currentText = uiEntry.getText();
				uiEntry.setText(currentText + part);
			});
			presetButton.setFont(UiUtils.makeFont(presetButton, UiConstants.FONT_PART));

			final GridBagConstraints buttonConstraints = UiUtils.makeGridConstraint(0, col, Expansion.NONE, true, insets);
			presetButtons.add(presetButton, buttonConstraints);
			col++;
		}
		final JLabel filler = new JLabel();
		final GridBagConstraints fillerConstraints = UiUtils.makeGridConstraint(0, col, Expansion.BOTH, true, insets);
		presetButtons.add(filler, fillerConstraints);

		final GridBagConstraints buttonRowConstraints = UiUtils.makeGridConstraint(row, 0, Expansion.HORIZONTAL, true, UiConstants.nopadding);
		root.add(scrollPane, buttonRowConstraints);
	}

	public void renderEntry(JPanel root)
	{
		final int COL_ENTRY = 0;
		final int COL_MODE = 1;

		final JPanel entryWrapper = new JPanel(new GridBagLayout());
		uiEntry.setName(UI_ENTRY);
		uiEntry.addActionListener(e -> {
			handleEntry();
		});
		uiEntry.setBorder(UiConstants.TRACER());
		uiEntry.setFont(UiUtils.makeFont(uiEntry, UiConstants.FONT_MEDIUM));
		entryWrapper.add(uiEntry, UiUtils.makeGridConstraint(0, COL_ENTRY, Expansion.HORIZONTAL, true, UiConstants.nopadding));

		uiMode.setName(UI_MODE);
		uiMode.addActionListener(e -> {
			handleModeButton();
		});
		entryWrapper.add(uiMode, UiUtils.makeGridConstraint(0, COL_MODE, Expansion.VERTICAL, true, new Insets(0, 10, 0, 0)));

		root.add(entryWrapper, UiUtils.makeGridConstraint(UI_ROW_ENTRY, 0, Expansion.HORIZONTAL, true, UiUtils.makeInsets(Set.of(Neighbor.BOTTOM))));
	}

	private void disableEntry(String message)
	{
		uiEntry.setEditable(false);
		uiEntry.setText(message);
	}
	
	private void enableEntry()
	{
		uiEntry.setText("");
		uiEntry.setEditable(true);
	}

	private void handleMenuSqliteInit()
	{
		final JFileChooser fc = new JFileChooser();
		final int returnVal = fc.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION) 
		{
			return;
		}

		final File file = fc.getSelectedFile();
		disableEntry("Importing " + file.getName());
		logger.info("got ids file " + file.getAbsolutePath());

		final SwingWorker<Void, Void> dbworker = new SwingWorker<>() {

			@Override
			protected Void doInBackground() throws Exception 
			{
				final Map<Integer, List<List<Integer>>> disasm = new IdsParser().parse(file.toPath());
				db.saveIdsParse(disasm);
				return null;
			}

			@Override
			protected void done()
			{
				try
				{
					enableEntry();
					get();
				}
				catch(Exception e)
				{
					logger.severe(UiUtils.printStackTrace(e));
					UiUtils.exceptionPopup(e);
				}
			}
		};
		dbworker.execute();
	}

	public void handleModeButton()
	{
		if(uiMode.isSelected())
		{
			uiMode.setText(UI_MODE_DISASM);
			if(UiConstants.getFlag(UiConstants.FLAG_SAVE_ENTRY))
			{
				final String currentText = uiEntry.getText();
				asmText = currentText;
				uiEntry.setText(disasmText);
			}
		}
		else
		{
			uiMode.setText(UI_MODE_ASM);
			if(UiConstants.getFlag(UiConstants.FLAG_SAVE_ENTRY))
			{
				final String currentText = uiEntry.getText();
				disasmText = currentText;
				uiEntry.setText(asmText);
			}
		}
	}

	public void handleEntry()
	{
		final String text = uiEntry.getText();
		final String mode = uiMode.isSelected() ? "disassembly" : "assembly";
		logger.info("got input " + text + " mode " + mode);

		final SwingWorker<String, Void> dbworker = new SwingWorker<>() {

			@Override
			protected String doInBackground() throws Exception 
			{
				final List<String> dbresults = uiMode.isSelected() ? db.getPartsFor(text) : db.lookupByParts(text);
				final String allResults = String.join("", dbresults);
				logger.info("got " + dbresults.size() + " results " + allResults);
				return allResults;
			}
			
			@Override
			protected void done()
			{
				try 
				{
					final String characters = get();
					results.setText(characters);
				}
				catch(Exception e) 
				{
					results.setText("error, check logs");
					logger.severe(UiUtils.printStackTrace(e));
					UiUtils.exceptionPopup(e);
				}
			}
		};
		dbworker.execute();
	}
}
