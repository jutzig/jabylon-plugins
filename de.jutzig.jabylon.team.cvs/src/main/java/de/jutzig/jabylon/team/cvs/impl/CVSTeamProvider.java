/**
 * 
 */
package de.jutzig.jabylon.team.cvs.impl;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.commandLine.BasicListener;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.ConnectionFactory;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.connection.Scrambler;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.event.FileAddedEvent;
import org.netbeans.lib.cvsclient.event.ModuleExpansionEvent;
import org.osgi.service.prefs.Preferences;

import de.jutzig.jabylon.properties.Project;
import de.jutzig.jabylon.properties.ProjectVersion;
import de.jutzig.jabylon.properties.PropertyFileDescriptor;
import de.jutzig.jabylon.ui.team.TeamProvider;
import de.jutzig.jabylon.ui.util.PreferencesUtil;

/**
 * @author Johannes Utzig (jutzig.dev@googlemail.com)
 * 
 */
public class CVSTeamProvider implements TeamProvider {

	@Override
	public Iterable<File> update(ProjectVersion project, IProgressMonitor monitor) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<File> update(PropertyFileDescriptor descriptor, IProgressMonitor monitor) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkout(ProjectVersion project, final IProgressMonitor monitor) throws IOException {
		Client client = null;
		try {
			final Client theClient = createClient(project);
			client = theClient;
			final String fullPath = project.absolutPath().toFileString();
			monitor.beginTask("Checkout", IProgressMonitor.UNKNOWN); 
			// TODO: is  there  a way  to get an estimate at least?
			final CheckoutCommand checkout = new CheckoutCommand();
			client.getEventManager().addCVSListener(new BasicListener() {
				@Override
				public void moduleExpanded(ModuleExpansionEvent arg0) {

					super.moduleExpanded(arg0);
					monitor.setTaskName(arg0.getModule());
					monitor.worked(1);
				}

				@Override
				public void fileAdded(FileAddedEvent arg0) {
					super.fileAdded(arg0);
					if (monitor.isCanceled())
						theClient.abort();
					String path = arg0.getFilePath().substring(fullPath.length());
					if (path.length() > 50)
						path = "..." + path.substring(path.length() - 50);
					monitor.subTask(path);
					monitor.worked(1);
				}

			});

			String module = PreferencesUtil.scopeFor(project.getProject()).get(CVSConstants.KEY_MODULE, "");
			checkout.setModule(module);
			checkout.setPruneDirectories(true);
			checkout.setNotShortenPaths(false);
			checkout.setCheckoutByRevision(project.getBranch());
			client.executeCommand(checkout, getGlobalOptions(project.getProject()));
		} catch (AuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommandAbortedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CommandException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (client != null)
				client.getConnection().close();
			if (monitor != null)
				monitor.done();
		}

	}

	@Override
	public void commit(ProjectVersion project, IProgressMonitor monitor) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void commit(PropertyFileDescriptor descriptor, IProgressMonitor monitor) throws IOException {
		// TODO Auto-generated method stub

	}

	private Client createClient(ProjectVersion projectVersion) throws AuthenticationException, CommandAbortedException {
		CVSRoot root = CVSRoot.parse(projectVersion.getProject().getRepositoryURI().toString());

		Connection connection = ConnectionFactory.getConnection(root);
		if (connection instanceof PServerConnection) {
			PServerConnection pserver = (PServerConnection) connection;
			Preferences prefs = PreferencesUtil.scopeFor(projectVersion.getProject());

			pserver.setUserName(prefs.get(CVSConstants.KEY_USERNAME, "anonymous"));
			Scrambler scrambler = StandardScrambler.getInstance();
			pserver.setEncodedPassword(scrambler.scramble(prefs.get(CVSConstants.KEY_PASSWORD, null)));

		}

		Client client = new Client(connection, new StandardAdminHandler());
		File file = new File(projectVersion.absolutPath().toFileString());
		client.setLocalPath(file.getAbsolutePath());
		return client;
	}

	private GlobalOptions getGlobalOptions(Project project) {
		GlobalOptions options = new GlobalOptions();
		options.setCVSRoot(project.getRepositoryURI().toString());
		return options;

	}

}
