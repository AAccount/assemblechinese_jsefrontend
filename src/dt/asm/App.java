package dt.asm;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import dt.asm.ui.UiMain;

public class App 
{
	public static final String VERSION = "1.0";
	public static void main(String[] args) throws Exception 
	{
		loadFonts();
		
		javax.swing.SwingUtilities.invokeLater(() -> {
			try 
			{
				new UiMain().render();
			}
			catch (ClassNotFoundException | IOException | ParseException | SQLException e) 
			{
				e.printStackTrace();
			}
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
}
