/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.core.builder.IBuildPathResource;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;

/**
 * Workspace folder build-path. As this build-path is mostly used as dependency and never for supporting modules in
 * consistent and parsed state, modules are not cached.
 * 
 * @author Denis Denisenko
 */
public class WorkspaceFolderBuildpath extends AbstractBuildPath
{
	/**
	 * Folder.
	 */
	private IFolder folder;

	/**
	 * WorkspaceFolderBuildpath constructor.
	 * 
	 * @param folder
	 *            - folder to create build path from.
	 */
	public WorkspaceFolderBuildpath(IFolder folder)
	{
		this.folder = folder;
	}

	/**
	 * Gets folder.
	 * 
	 * @return folder.
	 */
	public IFolder getFolder()
	{
		return folder;
	}

	/**
	 * {@inheritDoc}
	 */
	public IPath getResourcePath(IBuildPathResource resource)
	{
		if (resource instanceof LocalModule)
		{
			IFile moduleFile = ((LocalModule) resource).getFile();
			IPath folderPath = folder.getFullPath();
			IPath filePath = moduleFile.getFullPath();
			if (!folderPath.isPrefixOf(filePath))
			{
				return null;
			}

			return filePath.removeFirstSegments(folderPath.segmentCount()).makeAbsolute();
		}
		else if (resource instanceof LocalDirectory)
		{
			IResource fld = ((LocalDirectory) resource).getContainer();
			IPath folderPath = folder.getFullPath();
			IPath fldPath = fld.getFullPath();
			if (!folderPath.isPrefixOf(fldPath))
			{
				return null;
			}

			return fldPath.removeFirstSegments(folderPath.segmentCount()).makeAbsolute();
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void close()
	{
		// doing nothing.
	}

	/**
	 * {@inheritDoc}
	 */
	public IModule getModule(Object moduleResource)
	{
		if (!(moduleResource instanceof IFile))
		{
			return null;
		}
		IFile moduleFile = (IFile) moduleResource;
		if (!moduleFile.getProject().equals(folder.getProject()))
		{
			return null;
		}

		IPath modulePath = moduleFile.getProjectRelativePath();
		IPath folderPath = folder.getProjectRelativePath();

		if (!folderPath.isPrefixOf(modulePath))
		{
			return null;
		}

		return PHPLocalModuleFactory.getModule((IResource) moduleResource, WorkspaceFolderBuildpath.this);
	}

	/**
	 * {@inheritDoc}
	 */
	public IModule getModuleByPath(IPath path)
	{
		IFile file = folder.getFile(path);
		if (file == null || !file.exists())
		{
			return null;
		}

		return PHPLocalModuleFactory.getModule(file, WorkspaceFolderBuildpath.this);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IModule> getModules()
	{
		return indexLocalModules();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPassive()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IModule> getModulesByPath(IPath path)
	{
		IFolder pathFolder = folder.getFolder(path);
		if (pathFolder == null || !pathFolder.exists())
		{
			return null;
		}

		IResource[] innerResources = null;
		try
		{
			innerResources = pathFolder.members();
		}
		catch (CoreException e)
		{
			PHPEditorPlugin.logError(e);
			return null;
		}

		List<IFile> innerFiles = new ArrayList<IFile>();
		for (IResource innerResource : innerResources)
		{
			if (innerResource instanceof IFile && ((IFile) innerResource).exists())
			{
				innerFiles.add((IFile) innerResource);
			}
		}

		List<IModule> result = new ArrayList<IModule>();
		for (IFile innerFile : innerFiles)
		{
			IModule currentModule = getModule(innerFile);
			if (currentModule != null)
			{
				result.add(currentModule);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IDirectory> getSubdirectoriesByPath(IPath path)
	{
		IFolder pathFolder = folder.getFolder(path);
		if (pathFolder == null || !pathFolder.exists())
		{
			return null;
		}

		IResource[] innerResources = null;
		try
		{
			innerResources = pathFolder.members();
		}
		catch (CoreException e)
		{
			PHPEditorPlugin.logError(e);
			return null;
		}
		List<IFolder> innerFolders = new ArrayList<IFolder>();
		for (IResource innerResource : innerResources)
		{
			if (innerResource instanceof IFolder && ((IFolder) innerResource).exists())
			{
				innerFolders.add((IFolder) innerResource);
			}
		}

		List<IDirectory> result = new ArrayList<IDirectory>();
		for (IFolder innerFolder : innerFolders)
		{
			// ignoring inaccessible resources
			if (!innerFolder.isAccessible())
			{
				continue;
			}

			IDirectory currentDir = new LocalDirectory(innerFolder, this);
			result.add(currentDir);
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public IDirectory getDirectory(Object directoryResource)
	{
		if (!(directoryResource instanceof IFolder))
		{
			return null;
		}

		if (!((IFolder) directoryResource).isAccessible())
		{
			return null;
		}

		return new LocalDirectory((IFolder) directoryResource, WorkspaceFolderBuildpath.this);
	}

	/**
	 * {@inheritDoc}
	 */
	public IDirectory getDirectoryByPath(IPath path)
	{
		if (path.segmentCount() == 0)
		{
			return null;
		}

		IFolder fld = folder.getFolder(path);
		if (fld == null || !fld.isAccessible())
		{
			return null;
		}

		return new LocalDirectory(fld, WorkspaceFolderBuildpath.this);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(IModule module)
	{
		if (!(module instanceof LocalModule))
		{
			return false;
		}

		return getModule(((LocalModule) module).getFile()) != null;
	}

	/**
	 * Lists local modules.
	 */
	private List<IModule> indexLocalModules()
	{
		final List<IModule> toReturn = new ArrayList<IModule>();

		// skipping unsynchronized folders.
		if (!folder.isSynchronized(1))
		{
			return toReturn;
		}
		try
		{
			folder.accept(new IResourceVisitor()
			{
				public boolean visit(IResource resource) throws CoreException
				{
					// ignoring inaccessible resources
					if (!resource.isAccessible())
					{
						return false;
					}

					if (resource instanceof IFolder)
					{
						// skipping unsynchronized resources.
						if (!resource.isSynchronized(1))
						{
							return false;
						}

						if (!FolderFilteringManager.acceptFolder((IFolder) resource))
						{
							return false;
						}
					}

					if (!(resource instanceof IFile))
					{
						return true;
					}

					IModule module = PHPLocalModuleFactory.getModule(resource, WorkspaceFolderBuildpath.this);
					if (module != null)
					{
						toReturn.add(module);
					}

					return true;
				}

			});
		}
		catch (CoreException e)
		{
			PHPEditorPlugin.logError(e);
			return null;
		}

		return toReturn;
	}

	public String getHandleIdentifier()
	{
		return 'W' + folder.getFullPath().toPortableString();
	}
}
