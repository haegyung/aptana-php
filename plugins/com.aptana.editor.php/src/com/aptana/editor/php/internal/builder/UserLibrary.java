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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Pavel Petrochenko
 */
public class UserLibrary implements IPHPLibrary
{

	private String name;

	private List<String> directories;

	public UserLibrary(String serializedLibrary)
	{
		String[] split = serializedLibrary.split(File.pathSeparator);
		name = split[0].trim();
		directories = new ArrayList<String>();
		for (int a = 1; a < split.length; a++)
		{
			directories.add(split[a].trim());
		}
	}

	public UserLibrary(String text, String[] dirs)
	{
		this.name = text;
		directories = new ArrayList<String>(Arrays.asList(dirs));
	}

	public List<String> getDirectories()
	{
		return new ArrayList<String>(directories);
	}

	public String getName()
	{
		return name;
	}

	public String toString()
	{
		StringBuilder bld = new StringBuilder();
		bld.append(name);
		bld.append(File.pathSeparator);
		for (String s : directories)
		{
			bld.append(s);
			bld.append(File.pathSeparator);
		}
		bld.deleteCharAt(bld.length() - 1);
		return bld.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserLibrary other = (UserLibrary) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getId()
	{
		return name;
	}

	public boolean isTurnedOn()
	{
		return LibraryManager.getInstance().isTurnedOn(this);
	}

}
