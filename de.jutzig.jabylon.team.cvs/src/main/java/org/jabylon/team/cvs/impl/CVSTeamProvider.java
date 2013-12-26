/**
 *
 */
package org.jabylon.team.cvs.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.core.runtime.IProgressMonitor;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.commandLine.BasicListener;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.ConnectionFactory;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.connection.Scrambler;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.event.FileAddedEvent;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.event.FileRemovedEvent;
import org.netbeans.lib.cvsclient.event.FileToRemoveEvent;
import org.netbeans.lib.cvsclient.event.FileUpdatedEvent;
import org.netbeans.lib.cvsclient.event.ModuleExpansionEvent;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jabylon.common.team.TeamProvider;
import org.jabylon.common.team.TeamProviderException;
import org.jabylon.common.util.PreferencesUtil;
import org.jabylon.properties.DiffKind;
import org.jabylon.properties.Project;
import org.jabylon.properties.ProjectVersion;
import org.jabylon.properties.PropertiesFactory;
import org.jabylon.properties.PropertyFileDescriptor;
import org.jabylon.properties.PropertyFileDiff;

/**
 * @author Johannes Utzig (jutzig.dev@googlemail.com)
 *
 */
@Component(enabled=true,immediate=true)
@Service
public class CVSTeamProvider implements org.jabylon.common.team.TeamProvider {

	private static final Logger logger = LoggerFactory.getLogger(CVSTeamProvider.class);
	
	@Property(value="CVS")
	private static String KEY_KIND = TeamProvider.KEY_KIND;

	@Override
	public void checkout(ProjectVersion project, final IProgressMonitor monitor) {
		Client client = null;
		try {
			checkDirectories(project);
			final Client theClient = createClient(project);
			client = theClient;
			final String fullPath = project.absolutPath().toFileString();
			monitor.beginTask("Checkout", IProgressMonitor.UNKNOWN);
			// TODO: is there a way to get an estimate at least?
			final CheckoutCommand checkout = new CheckoutCommand();
			client.getEventManager().addCVSListener(new ProgressMonitorListener(monitor, client, fullPath));

			String module = PreferencesUtil.scopeFor(project.getParent()).get(CVSConstants.KEY_MODULE, "");
			checkout.setModule(module);
			checkout.setPruneDirectories(true);
			checkout.setNotShortenPaths(false);
			checkout.setCheckoutByRevision(project.getName());
			checkout.setCheckoutDirectory(project.getName());
			client.executeCommand(checkout, getGlobalOptions(project.getParent()));
		} catch (Exception e) {
			// delete directory if checkout fails, otherwise we present update / commit actions instead
			cleanupProjectVersionDirectory(project);
			throw new TeamProviderException("Checkout failed", e);
		} finally {
			if (client != null)
				try {
					client.getConnection().close();
				} catch (IOException e) {
					logger.error("Failed to close client connection", e);
				}
			if (monitor != null)
				monitor.done();
		}

	}

	private void cleanupProjectVersionDirectory(ProjectVersion project) {
		File versionDir = new File(project.absolutPath().toFileString());
		try {
			versionDir.delete();
		} catch (Exception e) {
			// ignore
		}
	}

	private void checkDirectories(ProjectVersion projectVersion) {
		File projectDir = new File(projectVersion.getParent().absoluteFilePath().toFileString());
		if (!projectDir.exists()) {
			if (!projectDir.mkdirs())
				throw new TeamProviderException("Checkout failed. Unable to create project directory");
		}
	}

	@Override
	public void commit(ProjectVersion project, final IProgressMonitor monitor) {
		Client client = null;
		try {
			Client theClient = createClient(project);
			client = theClient;
			final String fullPath = project.absolutPath().toFileString();
			monitor.beginTask("Committing Changes", IProgressMonitor.UNKNOWN);

			// TODO: is there a way to get an estimate at least?
			client.getEventManager().addCVSListener(new ProgressMonitorListener(monitor, client, fullPath));

			File[] filesToAdd = calculateMissingFiles(client,new File(fullPath));
			if(filesToAdd.length>0)
			{
				AddCommand add = new AddCommand();
				add.setMessage("Jabylon Auto-Sync Up");
				add.setFiles(filesToAdd);
				client.executeCommand(add, getGlobalOptions(project.getParent()));

				try {
					client.getConnection().close();
				} catch (IOException e) {
					logger.error("Failed to close client connection", e);
				}

				//must create a new client once the add succeeded
				theClient = createClient(project);
				client = theClient;

			}

			final CommitCommand commit = new CommitCommand();
			commit.setRecursive(true);
			commit.setMessage("Jabylon Auto-Sync Up");
			// commit.setToRevisionOrBranch(project.getName());
			commit.setFiles(new File(fullPath).listFiles(new CVSFileFilter()));
			client.executeCommand(commit, getGlobalOptions(project.getParent()));
		} catch (AuthenticationException e) {
			throw new TeamProviderException("Commit failed", e);
		} catch (CommandAbortedException e) {
			throw new TeamProviderException("Commit failed", e);
		} catch (CommandException e) {
			throw new TeamProviderException("Commit failed", e);
		} catch (Exception e) {
			throw new TeamProviderException("Commit failed", e);
		} finally {
			if (client != null)
				try {
					client.getConnection().close();
				} catch (IOException e) {
					logger.error("Failed to close client connection", e);
				}
			if (monitor != null)
				monitor.done();
		}

	}

	private File[] calculateMissingFiles(Client client, File parentDir) throws IOException {
		List<File> filesToAdd = new ArrayList<File>();
		addMissingFiles(parentDir,client,filesToAdd);
		return filesToAdd.toArray(new File[filesToAdd.size()]);
	}

	private void addMissingFiles(File parentDir, Client client, List<File> filesToAdd) throws IOException {
		Set<File> knownFiles = client.getAllFiles(parentDir);
		File[] files = parentDir.listFiles(new CVSFileFilter());
		for (File file : files) {
			if(file.isDirectory())
			{
				addMissingFiles(file, client, filesToAdd);
			}
			else
			{
				if(!knownFiles.contains(file))
				{
					logger.info("CVS ADD {}",file);
					filesToAdd.add(file);
				}
			}
		}

	}

	@Override
	public void commit(PropertyFileDescriptor descriptor, IProgressMonitor monitor) {

	}

	private Client createClient(ProjectVersion projectVersion) throws AuthenticationException, CommandAbortedException {
		CVSRoot root = CVSRoot.parse(projectVersion.getParent().getRepositoryURI().toString());

		Connection connection = ConnectionFactory.getConnection(root);
		if (connection instanceof PServerConnection) {
			PServerConnection pserver = (PServerConnection) connection;
			Preferences prefs = PreferencesUtil.scopeFor(projectVersion.getParent());

			pserver.setUserName(prefs.get(CVSConstants.KEY_USERNAME, "anonymous"));
			Scrambler scrambler = StandardScrambler.getInstance();
			pserver.setEncodedPassword(scrambler.scramble(prefs.get(CVSConstants.KEY_PASSWORD, null)));

		}

		Client client = new Client(connection, new StandardAdminHandler());
		File parentDir = new File(projectVersion.absoluteFilePath().toFileString());
		if(parentDir.exists())
		{
			//the checkout is already done. Use the module as the client local path
			client.setLocalPath(parentDir.getAbsolutePath());
		}
		else
		{
			// use the project dir as local path and set the version as checkout directory for the CVS command
			client.setLocalPath(parentDir.getParentFile().getAbsolutePath());
		}
		return client;
	}

	private GlobalOptions getGlobalOptions(Project project) {
		GlobalOptions options = new GlobalOptions();
		options.setCVSRoot(project.getRepositoryURI().toString());
		return options;

	}

	@Override
	public Collection<PropertyFileDiff> update(ProjectVersion project, IProgressMonitor monitor) throws TeamProviderException {
		Client client = null;
		try {
			final Client theClient = createClient(project);
			client = theClient;
			final String fullPath = project.absolutPath().toFileString();
			monitor.beginTask("Updating", IProgressMonitor.UNKNOWN);
			// TODO: is there a way to get an estimate at least?
			UpdateCommand command = new UpdateCommand();

			DiffListener diffListener = new DiffListener(monitor, client, fullPath);
			client.getEventManager().addCVSListener(diffListener);
			command.setRecursive(true);
			command.setPruneDirectories(true);
			command.setFiles(new File(fullPath).listFiles(new CVSFileFilter()));
			command.setBuildDirectories(true);
			client.executeCommand(command, getGlobalOptions(project.getParent()));
			return diffListener.getDiff();
		} catch (AuthenticationException e) {
			throw new TeamProviderException("Update failed", e);
		} catch (CommandAbortedException e) {
			throw new TeamProviderException("Update failed", e);
		} catch (CommandException e) {
			throw new TeamProviderException("Update failed", e);
		} catch (Exception e) {
			throw new TeamProviderException("Update failed", e);
		} finally {
			if (client != null)
				try {
					client.getConnection().close();
				} catch (IOException e) {
					logger.error("Failed to close client connection", e);
				}
			if (monitor != null)
				monitor.done();
		}
	}

	@Override
	public Collection<PropertyFileDiff> update(PropertyFileDescriptor descriptor, IProgressMonitor monitor) throws TeamProviderException {
		// TODO Auto-generated method stub
		return null;
	}

}

class ProgressMonitorListener extends BasicListener {

	private static final long serialVersionUID = 1L;
	private final IProgressMonitor monitor;
	private final Client client;
	private String basePath;

	public ProgressMonitorListener(IProgressMonitor monitor, Client client, String basePath) {
		super();
		this.monitor = monitor;
		this.client = client;
		this.basePath = basePath;
	}

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
			client.abort();
		String path = truncatePath(arg0.getFilePath());
		monitor.subTask(path);
		monitor.worked(1);
	}

	@Override
	public void fileInfoGenerated(FileInfoEvent arg0) {
		super.fileInfoGenerated(arg0);
		if (monitor.isCanceled())
			client.abort();
	}

	@Override
	public void fileRemoved(FileRemovedEvent arg0) {
		super.fileRemoved(arg0);
		if (monitor.isCanceled())
			client.abort();
		String path = truncatePath(arg0.getFilePath());
		monitor.subTask(path);
		monitor.worked(1);
	}

	@Override
	public void fileToRemove(FileToRemoveEvent arg0) {
		super.fileToRemove(arg0);
		if (monitor.isCanceled())
			client.abort();
		String path = truncatePath(arg0.getFilePath());
		monitor.subTask(path);
		monitor.worked(1);
	}

	@Override
	public void fileUpdated(FileUpdatedEvent arg0) {
		super.fileUpdated(arg0);
		if (monitor.isCanceled())
			client.abort();
		String path = truncatePath(arg0.getFilePath());
		monitor.subTask(path);
		monitor.worked(1);
	}

	private String truncatePath(String path) {
		String result = path.substring(basePath.length());
		if (result.length() > 50)
			return "..." + result.substring(result.length() - 50);
		return result;
	}

	protected String deresolve(String path)
	{
		return path.substring(basePath.length());
	}

}

class DiffListener extends ProgressMonitorListener
{

	private static final long serialVersionUID = 1L;
	private List<PropertyFileDiff> diffs;


	public DiffListener(IProgressMonitor monitor, Client client, String basePath) {
		super(monitor, client, basePath);
		diffs = new ArrayList<PropertyFileDiff>();
	}

	public Collection<PropertyFileDiff> getDiff()
	{
		return diffs;
	}

	@Override
	public void fileAdded(FileAddedEvent arg0) {
		PropertyFileDiff diff = PropertiesFactory.eINSTANCE.createPropertyFileDiff();
		diff.setNewPath(deresolve(arg0.getFilePath()));
		diff.setKind(DiffKind.ADD);
		diffs.add(diff);
	}

	@Override
	public void fileRemoved(FileRemovedEvent arg0) {
		PropertyFileDiff diff = PropertiesFactory.eINSTANCE.createPropertyFileDiff();
		diff.setOldPath(deresolve(arg0.getFilePath()));
		diff.setKind(DiffKind.REMOVE);
		diffs.add(diff);
	}

	@Override
	public void fileUpdated(FileUpdatedEvent arg0) {

		PropertyFileDiff diff = PropertiesFactory.eINSTANCE.createPropertyFileDiff();
		diff.setNewPath(deresolve(arg0.getFilePath()));
		diff.setOldPath(deresolve(arg0.getFilePath()));
		diff.setKind(DiffKind.MODIFY);
		diffs.add(diff);
	}
}