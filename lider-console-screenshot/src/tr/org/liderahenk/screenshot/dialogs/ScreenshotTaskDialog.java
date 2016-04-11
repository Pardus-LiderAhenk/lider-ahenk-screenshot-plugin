package tr.org.liderahenk.screenshot.dialogs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.screenshot.constants.ScreenshotConstants;
import tr.org.liderahenk.screenshot.i18n.Messages;
import tr.org.liderahenk.screenshot.utils.ScreenshotUtils;

/**
 * Task execution dialog for Screenshot plugin.
 * 
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Dogan</a>
 *
 */
public class ScreenshotTaskDialog extends DefaultTaskDialog {
	
	private Combo cmbFormat;
	
	// Combo values & i18n labels
	private final String[] formatArr = new String[] { "PNG", "JPEG" };

	public ScreenshotTaskDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell, dnSet);
	}
	
	@Override
	public String createTitle() {
		return Messages.getString("TAKE_SCREENSHOT");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label lblFormat = new Label(composite, SWT.NONE);
		lblFormat.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		lblFormat.setText(Messages.getString("IMAGE_FORMAT"));
		
		cmbFormat = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbFormat.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (int i = 0; i < formatArr.length; i++) {
			String format = formatArr[i];
			if (format != null && !format.isEmpty()) {
				cmbFormat.add(format);
				cmbFormat.setData(i + "", formatArr[i]);
			}
		}
		cmbFormat.setEnabled(true);
		
		return null;
	}

	@Override
	public boolean validateBeforeExecution() {
		return true;
	}

	@Override
	public Map<String, Object> getParameterMap() {
		
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put(ScreenshotConstants.PARAMETERS.FORMAT, ScreenshotUtils.getSelectedValue(cmbFormat));
		return parameterMap;
	}

	@Override
	public String getCommandId() {
		return "RUN";
	}

	@Override
	public String getPluginName() {
		return ScreenshotConstants.PLUGIN_NAME;
	}

	@Override
	public String getPluginVersion() {
		return ScreenshotConstants.PLUGIN_VERSION;
	}



}
