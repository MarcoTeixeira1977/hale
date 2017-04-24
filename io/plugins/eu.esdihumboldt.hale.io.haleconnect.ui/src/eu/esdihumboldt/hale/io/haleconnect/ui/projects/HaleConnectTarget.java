/*
 * Copyright (c) 2017 wetransform GmbH
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     wetransform GmbH <http://www.wetransform.to>
 */

package eu.esdihumboldt.hale.io.haleconnect.ui.projects;

import static eu.esdihumboldt.hale.io.haleconnect.HaleConnectService.PERMISSION_CREATE;
import static eu.esdihumboldt.hale.io.haleconnect.HaleConnectService.RESOURCE_TRANSFORMATION_PROJECT;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.fhg.igd.slf4jplus.ALogger;
import de.fhg.igd.slf4jplus.ALoggerFactory;
import eu.esdihumboldt.hale.common.core.io.Value;
import eu.esdihumboldt.hale.common.core.io.project.impl.ArchiveProjectWriter;
import eu.esdihumboldt.hale.common.core.io.supplier.LocatableOutputSupplier;
import eu.esdihumboldt.hale.io.haleconnect.HaleConnectService;
import eu.esdihumboldt.hale.io.haleconnect.OwnerType;
import eu.esdihumboldt.hale.io.haleconnect.project.HaleConnectProjectWriter;
import eu.esdihumboldt.hale.io.haleconnect.ui.HaleConnectLoginDialog;
import eu.esdihumboldt.hale.io.haleconnect.ui.HaleConnectLoginHandler;
import eu.esdihumboldt.hale.ui.HaleUI;
import eu.esdihumboldt.hale.ui.io.target.AbstractTarget;

/**
 * hale connect export target
 * 
 * @author Florian Esser
 */
public class HaleConnectTarget extends AbstractTarget<HaleConnectProjectWriter> {

	private static final ALogger log = ALoggerFactory.getLogger(HaleConnectTarget.class);

	private final HaleConnectService haleConnect;

	private Label loginStatusLabel;
	private Button loginButton;
	private Button enableVersioning;
	private Button publicAccess;
	private Group ownershipGroup;
	private Button ownerUser;
	private Button ownerOrg;
	private Button includeWebResources;
	private Button excludeData;

	/**
	 * Default constructor
	 */
	public HaleConnectTarget() {
		haleConnect = HaleUI.getServiceProvider().getService(HaleConnectService.class);
	}

	@Override
	public void createControls(Composite parent) {
		getPage().setDescription("Please select a destination file for the export");

		parent.setLayout(new GridLayout(3, false));

		/*
		 * Login status label
		 */
		loginStatusLabel = new Label(parent, SWT.NONE);
		loginStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		loginButton = new Button(parent, SWT.PUSH);
		loginButton.setText("Login");
		loginButton.addSelectionListener(new SelectionAdapter() {

			/**
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				HaleConnectLoginDialog loginDialog = HaleConnectLoginHandler
						.createLoginDialog(Display.getCurrent().getActiveShell());
				if (loginDialog.open() == Dialog.OK) {
					HaleConnectLoginHandler.performLogin(loginDialog);
					updateState();
				}
			}

		});

		ownershipGroup = new Group(parent, SWT.NONE);
		ownershipGroup.setText("Who should be the owner of the uploaded project?");
		GridLayout grid = new GridLayout(3, true);
		ownershipGroup.setLayout(grid);
		ownershipGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		ownerUser = new Button(ownershipGroup, SWT.RADIO);
		ownerUser.setText("You");

		ownerOrg = new Button(ownershipGroup, SWT.RADIO);
		ownerOrg.setText("Your organisation");

		enableVersioning = new Button(parent, SWT.CHECK);
		enableVersioning.setText("Enable versioning?");
		enableVersioning.setLayoutData(new GridData(SWT.LEAD, SWT.LEAD, true, false, 3, 1));

		publicAccess = new Button(parent, SWT.CHECK);
		publicAccess.setText("Allow public access?");
		publicAccess.setLayoutData(new GridData(SWT.LEAD, SWT.LEAD, true, false, 3, 1));

		includeWebResources = new Button(parent, SWT.CHECK);
		includeWebResources.setText("Include web resources?");
		includeWebResources.setLayoutData(new GridData(SWT.LEAD, SWT.LEAD, true, false, 3, 1));

		excludeData = new Button(parent, SWT.CHECK);
		excludeData.setText("Exclude source data?");
		excludeData.setLayoutData(new GridData(SWT.LEAD, SWT.LEAD, true, false, 3, 1));
		excludeData.setSelection(true);

		updateState();
	}

	/**
	 * Update the page state.
	 */
	protected void updateState() {
		updateLoginStatus();
		setValid(haleConnect.isLoggedIn() && (ownerUser.isEnabled() || ownerOrg.isEnabled()));
	}

	@Override
	public void onShowPage(boolean firstShow) {
		super.onShowPage(firstShow);
		updateState();
	}

	@Override
	public boolean updateConfiguration(HaleConnectProjectWriter provider) {
		provider.setParameter(HaleConnectProjectWriter.ENABLE_VERSIONING,
				Value.of(enableVersioning.getSelection()));
		provider.setParameter(HaleConnectProjectWriter.SHARING_PUBLIC,
				Value.of(publicAccess.getSelection()));
		Value ownerValue = Value.of(ownerUser.getSelection() ? OwnerType.USER.getJsonValue()
				: OwnerType.ORGANISATION.getJsonValue());
		provider.setParameter(HaleConnectProjectWriter.OWNER_TYPE, ownerValue);
		provider.setParameter(ArchiveProjectWriter.EXLUDE_DATA_FILES,
				Value.of(excludeData.getSelection()));
		provider.setParameter(ArchiveProjectWriter.INCLUDE_WEB_RESOURCES,
				Value.of(includeWebResources.getSelection()));

		provider.setTarget(new LocatableOutputSupplier<OutputStream>() {

			@Override
			public OutputStream getOutput() throws IOException {
				return null;
			}

			@Override
			public URI getLocation() {
				// Returning null will advise HaleConnectProjectWriter to create
				// a new hale connect transformation project.
				return null;
			}

		});
		return true;
	}

	private void updateLoginStatus() {
		HaleConnectService hcs = HaleUI.getServiceProvider().getService(HaleConnectService.class);
		boolean loggedIn = hcs.isLoggedIn();
		loginButton.setEnabled(!loggedIn);
		ownershipGroup.setEnabled(loggedIn);
		enableVersioning.setEnabled(loggedIn);
		publicAccess.setEnabled(loggedIn);
		ownerUser.setEnabled(loggedIn);
		includeWebResources.setEnabled(loggedIn);
		excludeData.setEnabled(loggedIn);

		if (loggedIn) {
			loginStatusLabel
					.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
			loginStatusLabel.setText(
					MessageFormat.format("Logged in as {0}", hcs.getSession().getUsername()));

			boolean orgAllowed;
			if (hcs.getSession().getOrganisationIds().isEmpty()) {
				orgAllowed = false;
			}
			else {
				try {
					orgAllowed = hcs.testUserPermission(RESOURCE_TRANSFORMATION_PROJECT,
							hcs.getSession().getOrganisationIds().iterator().next(),
							PERMISSION_CREATE);
				} catch (Throwable t) {
					log.userError(
							"A problem occurred while contacting hale connect. Functionality may be limited.",
							t);
					orgAllowed = false;
				}
			}

			ownerOrg.setEnabled(orgAllowed);
			ownerOrg.setSelection(orgAllowed);

			boolean userAllowed;
			try {
				userAllowed = hcs.testUserPermission(RESOURCE_TRANSFORMATION_PROJECT,
						OwnerType.USER.getJsonValue(), PERMISSION_CREATE);
			} catch (Throwable t) {
				log.userError(
						"A problem occurred while contacting hale connect. Functionality may be limited.",
						t);
				userAllowed = false;
			}
			ownerUser.setEnabled(userAllowed);
			ownerUser.setSelection(userAllowed);

			if (!userAllowed && !orgAllowed) {
				loginStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
				loginStatusLabel
						.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
				loginStatusLabel.setText(
						"You do not have sufficient permissions to upload transformation projects to hale connect.");
			}
		}
		else {
			loginStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			loginStatusLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
			loginStatusLabel.setText(
					"You are not logged in to hale connect. Please login before sharing a project.");

			ownerOrg.setEnabled(false);
		}
	}

}
