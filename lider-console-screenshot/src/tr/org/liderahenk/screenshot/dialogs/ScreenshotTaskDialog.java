package tr.org.liderahenk.screenshot.dialogs;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.constants.LiderConstants;
import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.model.TaskStatus;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.screenshot.i18n.Messages;
import tr.org.liderahenk.screenshot.constants.ScreenshotConstants;
import tr.org.liderahenk.screenshot.dialogs.ScreenshotTaskDialog;

/**
 * 
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Dogan</a>
 *
 */
public class ScreenshotTaskDialog extends DefaultTaskDialog {
	
	private static final Logger logger = LoggerFactory.getLogger(ScreenshotTaskDialog.class);

	private IEventBroker eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

	public ScreenshotTaskDialog(Shell parentShell, String dn) {
		super(parentShell, dn);
		// TODO improvement. (after XMPPClient fix) Instead of 'TASK' topic use
		// plugin name as event topic
		eventBroker.subscribe(LiderConstants.EVENT_TOPICS.TASK, eventHandler);
	}
	
	private EventHandler eventHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {

					monitor.beginTask("SCREENSHOT", 100);

					try {

						String body = (String) event.getProperty("org.eclipse.e4.data");
						TaskStatus taskStatus = new ObjectMapper().readValue(body, TaskStatus.class);
						Map<String, Object> responseData = taskStatus.getResponseData();

						Image screenshot = (Image) responseData.get("screenshot");

					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						Notifier.error("", Messages.getString("UNEXPECTED_ERROR_TAKING_SCREENSHOT"));
					}

					monitor.worked(100);
					monitor.done();

					return Status.OK_STATUS;
				}
			};

			job.setUser(true);
			job.schedule();
		}
	};
	
	@Override
	public boolean close() {
		eventBroker.unsubscribe(eventHandler);
		return super.close();
	}

	@Override
	public String createTitle() {
		return Messages.getString("TAKE_SCREENSHOT");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {
		return null;
	}

	@Override
	public boolean validateBeforeExecution() {
		return true;
	}

	@Override
	public Map<String, Object> getParameterMap() {
		return null;
	}

	@Override
	public String getCommandId() {
		return "TAKE-SCREENSHOT";
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
