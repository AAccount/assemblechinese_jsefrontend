package dt.asm.ui;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

public class UiUtils 
{
	public enum Neighbor
	{
		TOP,
		BOTTOM,
		LEFT,
		RIGHT,
		EVERYWHERE
	}

	public enum Expansion
	{
		HORIZONTAL, VERTICAL, BOTH;

		public boolean horizontal()
		{
			return this == HORIZONTAL || this == BOTH;
		}

		public boolean vertical()
		{
			return this == VERTICAL || this == BOTH;
		}
	}

	private UiUtils(){}

	public static Font makeFont(Component target, int size)
	{
		final Font currentFont = target.getFont();
		return new Font(currentFont.getName(), currentFont.getStyle(), size);
	}

	public static GridBagConstraints makeGridConstraint(int row, int column, Expansion expansion, boolean topLeft, Insets insets)
	{
		final int weightx = expansion.horizontal() ? UiConstants.GRIDBAG_AUTOEXPAND : UiConstants.GRIDBAG_NO_AUTOEXPAND;
		final int weighty = expansion.vertical() ? UiConstants.GRIDBAG_AUTOEXPAND : UiConstants.GRIDBAG_NO_AUTOEXPAND;

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = column;
		constraints.gridy = row;
		constraints.weightx = weightx;
		constraints.weighty = weighty;
		constraints.anchor = topLeft ? GridBagConstraints.FIRST_LINE_START : GridBagConstraints.LINE_START;
		switch(expansion)
		{
			case Expansion.HORIZONTAL:
				constraints.fill = GridBagConstraints.HORIZONTAL;
				break;
			case Expansion.VERTICAL:
				constraints.fill = GridBagConstraints.VERTICAL;
				break;
			case Expansion.BOTH:
				constraints.fill = GridBagConstraints.BOTH;
				break;
		}
		constraints.insets = insets;
		return constraints;
	}

	public static Insets makeInsets(Set<Neighbor> neighbors)
	{
		if(neighbors.contains(Neighbor.EVERYWHERE))
		{
			neighbors = Set.of(Neighbor.LEFT, Neighbor.RIGHT, Neighbor.TOP, Neighbor.BOTTOM);
		}

		final int PADDING = 10;
		return new Insets(
			neighbors.contains(Neighbor.TOP) ? PADDING / 2 : PADDING, 
			neighbors.contains(Neighbor.LEFT) ? PADDING / 2 : PADDING, 
			neighbors.contains(Neighbor.BOTTOM)? PADDING / 2 : PADDING, 
			neighbors.contains(Neighbor.RIGHT) ? PADDING / 2 : PADDING
		);
	}

	public static void printException(Exception e)
	{
		System.out.print(e);
		final String title = e.getClass().getName();
		final String errorMessage = e.getMessage();
		final String stackTrace = printStackTrace(e.getStackTrace());
		final String popupMessage = errorMessage + "\n" + stackTrace;

		JOptionPane.showMessageDialog(null, popupMessage, title, JOptionPane.ERROR_MESSAGE);
		System.err.println(printStackTrace(e.getStackTrace()));
	}

	private static String printStackTrace(StackTraceElement[] stack)
	{
		return Arrays.asList(stack).stream()
			.filter(element -> element.getClassName().contains("dt.jdictionary"))
			.map(element -> element.toString())
			.collect(Collectors.joining("\n\t"));
	}
}
