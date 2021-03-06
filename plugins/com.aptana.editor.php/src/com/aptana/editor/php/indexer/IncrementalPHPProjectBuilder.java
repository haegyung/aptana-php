/**
 * 
 */
package com.aptana.editor.php.indexer;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.aptana.editor.php.internal.contentAssist.ContentAssistUtils;

/**
 * An incremental project builder for PHP projects. This builder is here for clean operations.
 * 
 * @author Shalom Gibly
 * @since Aptana PHP 1.1
 */
public class IncrementalPHPProjectBuilder extends IncrementalProjectBuilder
{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException
	{
		PHPGlobalIndexer.getInstance().clean(getProject(), monitor);
		PHPGlobalIndexer.getInstance().cleanLibraries(monitor);
		ContentAssistUtils.cleanIndex();
	}

	/**
	 * Constructor
	 */
	public IncrementalPHPProjectBuilder()
	{
	}

	/**
	 * Returns null, as the PHP plugin still does not use the builders as it should.
	 * 
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException
	{
		// FIXME - SG: Convert from Indexer timer to the builder system.
		IProject project = getProject();
		if ((kind == CLEAN_BUILD || kind == FULL_BUILD) && project != null)
		{
			PHPGlobalIndexer.getInstance().clean(project, monitor);
			PHPGlobalIndexer.getInstance().build(project, monitor);
		}
		return null;
	}

}
