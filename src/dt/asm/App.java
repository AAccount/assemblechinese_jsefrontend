package dt.asm;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import dt.asm.ui.UiMain;
import dt.asm.ui.UiUtils;

public class App 
{
	public static final String VERSION = "V1.0";
	public static void main(String[] args) throws Exception 
	{
		loadFonts();
		setupLogger();

		final Logger logger = Logger.getLogger(App.class.getName());
		Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
			logger.severe(thread.getName() + " " + UiUtils.printStackTrace(throwable));
		});
		
		logger.info("starting assemble chinese " + VERSION);
		javax.swing.SwingUtilities.invokeLater(() -> {
			new UiMain().render();
		});
	}

	private static void loadFonts()
	{
		try 
		{
			final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

			// 1. Explicitly list all 4 separate BabelStone font files
			final String[] fontFiles = {
				"BabelStoneHan.ttf",
				"BabelStoneHanPUA.ttf",
				"BabelStoneErjian1.ttf",
				"BabelStoneErjian2.ttf"
			};

			// 2. Load and register each font one by one directly from the JAR classpath stream
			for(String fileName : fontFiles) 
			{
				final String path = "/resources/fonts/" + fileName;
				final InputStream is = App.class.getResourceAsStream(path);
				
				if(is == null) 
				{
					throw new java.io.FileNotFoundException("Could not find font inside JAR path: " + path);
				}
				
				// Load the font into the JVM's runtime graphics memory cache
				final Font physicalFont = Font.createFont(Font.TRUETYPE_FONT, is);
				env.registerFont(physicalFont); 
			}

			// 3. Create the composite link using "Dialog"
			// When you register fonts to the environment above, Java automatically 
			// injects them into the fallback queue for its virtual "Dialog" font.
			final Font globalFont = new Font("Dialog", Font.PLAIN, 15);

			// 4. Overwrite all default Swing UI fonts with our fallback pipeline
			final java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
			while (keys.hasMoreElements()) 
			{
				final Object key = keys.nextElement();
				final Object value = UIManager.get(key);
				if(value instanceof FontUIResource) 
				{
					UIManager.put(key, new FontUIResource(globalFont));
				}
			}

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Font fallback chain mapping failed. Defaulting to system fonts.");
		}
	}

	private static void setupLogger()
	{
		System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT.%1$tL [%4$-7s] %2$s - %5$s%n");
		try 
		{
			final Logger rootLogger = Logger.getLogger("");
			final String tmpDir = System.getProperty("java.io.tmpdir");
			final String logPattern = tmpDir + File.separator + "asmchinese-%g.log";
			final FileHandler fileHandler = new FileHandler(logPattern, 5_000_000, 3, true);
			
			fileHandler.setFormatter(new SimpleFormatter());
			fileHandler.setLevel(Level.ALL);

			rootLogger.addHandler(fileHandler);
			rootLogger.setLevel(Level.INFO);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
