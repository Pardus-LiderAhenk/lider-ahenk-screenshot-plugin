package tr.org.liderahenk.screenshot.handlers;

import org.eclipse.swt.widgets.Display;

import tr.org.liderahenk.liderconsole.core.handlers.SingleSelectionHandler;
import tr.org.liderahenk.screenshot.dialogs.ScreenshotTaskDialog;

/**
 * Task execution handler for Screenshot plugin.
 * 
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Dogan</a>
 *
 */
public class ScreenshotTaskHandler extends SingleSelectionHandler {
	
	@Override
	public void executeWithDn(String dn) {
		ScreenshotTaskDialog dialog = new ScreenshotTaskDialog(Display.getDefault().getActiveShell(), dn);
		dialog.create();
		dialog.open();
	}
	
}
