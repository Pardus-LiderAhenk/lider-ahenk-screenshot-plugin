package tr.org.liderahenk.screenshot.dialogs;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tr.org.liderahenk.liderconsole.core.dialogs.DefaultTaskDialog;
import tr.org.liderahenk.liderconsole.core.exceptions.ValidationException;
import tr.org.liderahenk.liderconsole.core.rest.utils.AgentRestUtils;
import tr.org.liderahenk.liderconsole.core.utils.SWTResourceManager;
import tr.org.liderahenk.liderconsole.core.widgets.Notifier;
import tr.org.liderahenk.liderconsole.core.xmpp.enums.ContentType;
import tr.org.liderahenk.liderconsole.core.xmpp.notifications.TaskStatusNotification;
import tr.org.liderahenk.screenshot.constants.ScreenshotConstants;
import tr.org.liderahenk.screenshot.i18n.Messages;
import tr.org.liderahenk.screenshot.widgets.OnlineUsersCombo;

/**
 * 
 * @author <a href="mailto:mine.dogan@agem.com.tr">Mine Dogan</a>
 * @author <a href="mailto:emre.akkaya@agem.com.tr">Emre Akkaya</a>
 *
 */
public class ScreenshotTaskDialog extends DefaultTaskDialog {

	private static final Logger logger = LoggerFactory.getLogger(ScreenshotTaskDialog.class);

	private List<OnlineUsersCombo> comboList;

	public ScreenshotTaskDialog(Shell parentShell, Set<String> dnSet) {
		super(parentShell, dnSet);
		subscribeEventHandler(taskStatusNotificationHandler);
	}

	@Override
	public String createTitle() {
		return Messages.getString("TAKE_SCREENSHOT");
	}

	@Override
	public Control createTaskDialogArea(Composite parent) {

		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, false));
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		comboList = new ArrayList<OnlineUsersCombo>();

		// For each DN, user may indicate whose screenshot should be taken:
		for (String dn : getDnSet()) {
			// DN
			Label lblDn = new Label(mainComposite, SWT.NONE);
			lblDn.setFont(SWTResourceManager.getFont("Sans", 9, SWT.BOLD));
			lblDn.setText((dn.length() > 100 ? dn.substring(0, 100) : dn) + ":");

			Composite innerComposite = new Composite(mainComposite, SWT.NONE);
			innerComposite.setLayout(new GridLayout(2, false));
			innerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			// Online users
			Label lblOnlineUsers = new Label(innerComposite, SWT.NONE);
			lblOnlineUsers.setText(Messages.getString("ONLINE_USERS"));

			// Find online users of agent specified by current DN
			List<String> onlineUsers = null;
			try {
				onlineUsers = AgentRestUtils.getOnlineUsers(dn);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			OnlineUsersCombo cmbOnlineUsers = new OnlineUsersCombo(innerComposite,
					SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY, dn);
			cmbOnlineUsers.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			comboList.add(cmbOnlineUsers);
			if (onlineUsers != null && !onlineUsers.isEmpty()) {
				onlineUsers.add(""); // User selection is optional!
				cmbOnlineUsers.setItems(onlineUsers.toArray(new String[onlineUsers.size()]));
			}
		}

		return mainComposite;
	}

	@Override
	public void validateBeforeExecution() throws ValidationException {
	}

	@Override
	public Map<String, Object> getParameterMap() {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		for (OnlineUsersCombo cmbOnlineUsers : comboList) {
			if (cmbOnlineUsers.getSelectionIndex() > -1) {
				String onlineUser = cmbOnlineUsers.getItem(cmbOnlineUsers.getSelectionIndex());
				if (onlineUser.isEmpty())
					continue;
				parameterMap.put(cmbOnlineUsers.getDn(), onlineUser);
			}
		}
		return parameterMap;
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

	private EventHandler taskStatusNotificationHandler = new EventHandler() {
		@Override
		public void handleEvent(final Event event) {
			Job job = new Job("TASK") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask("SCREENSHOT", 100);
					try {
						final TaskStatusNotification taskStatus = (TaskStatusNotification) event
								.getProperty("org.eclipse.e4.data");
						if (ContentType.getImageContentTypes().contains(taskStatus.getResult().getContentType())) {
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									// Agent DN
									String dn = taskStatus.getCommandExecution().getDn();
									for (OnlineUsersCombo cmbOnlineUsers : comboList) {
										// Find correct line to display the
										// image
										if (dn.equalsIgnoreCase(cmbOnlineUsers.getDn())) {
											// Draw image!
											byte[] responseData = taskStatus.getResult().getResponseData();
											Label lblImage = new Label(cmbOnlineUsers.getParent(), SWT.BORDER);
											lblImage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
											lblImage.setImage(createImage(responseData));
											// TODO download button!
											new Label(cmbOnlineUsers.getParent(), SWT.NONE);
											cmbOnlineUsers.getParent().layout(true);
											break;
										}
									}
								}
							});
						}
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

	/**
	 * Create image from given response data, resize if necessary.
	 * 
	 * @param responseData
	 * @return
	 */
	private Image createImage(byte[] responseData) {
		int width = 400;
		int height = 400;
		Image image = new Image(Display.getDefault(), new ByteArrayInputStream(responseData));
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
		image.dispose();
		return scaled;
	}

}
