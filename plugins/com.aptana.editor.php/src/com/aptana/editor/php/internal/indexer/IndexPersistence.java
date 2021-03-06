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
package com.aptana.editor.php.internal.indexer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IEntryValueFactory;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.IReportable;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;

public final class IndexPersistence
{

	private static HashMap<Integer, IEntryValueFactory> factories = new HashMap<Integer, IEntryValueFactory>();

	static
	{
		IConfigurationElement[] configurationElementsFor = Platform.getExtensionRegistry().getConfigurationElementsFor(
				"com.aptana.editor.php.indexerEntryValue"); //$NON-NLS-1$
		for (IConfigurationElement e : configurationElementsFor)
		{
			String attribute = e.getAttribute("id"); //$NON-NLS-1$
			try
			{
				IEntryValueFactory vf = (IEntryValueFactory) e.createExecutableExtension("creator"); //$NON-NLS-1$
				factories.put(Integer.parseInt(attribute), vf);
			}
			catch (CoreException e1)
			{
				PHPEditorPlugin.logError(e1);
			}
		}
	}

	private IndexPersistence()
	{

	}

	public static void load(UnpackedElementIndex index, DataInputStream di, IBuildPath pb) throws IOException
	{
		int readInt = di.readInt();
		// long l0=System.currentTimeMillis();
		for (int a = 0; a < readInt; a++)
		{
			IModule m = readModule(di, pb);
			if (m != null)
			{
				int sz = di.readInt();
				long ts = di.readLong();
				index.recordTimeStamp(m, ts);
				for (int b = 0; b < sz; b++)
				{
					int category = di.readInt();
					String path = di.readUTF();
					Object value = readValue(di);
					index.addEntry(category, path, value, m);
				}
			}
		}
		// long l1=System.currentTimeMillis();
		// System.out.println("Index load:"+(l1-l0));
	}

	public static void store(UnpackedElementIndex index, DataOutputStream da, IBuildPath pb) throws IOException
	{
		IModule[] array = index.getAllModules();
		da.writeInt(array.length);
		int pos = 0;
		for (IModule m : array)
		{

			List<UnpackedEntry> list = index.entries.get(m);
			if (list == null)
				list = Collections.emptyList();
			writeModule(da, m, pb);

			da.writeInt(list.size());
			da.writeLong(index.getTimeStamp(m));
			int k = 0;
			for (UnpackedEntry e : new ArrayList<UnpackedEntry>(list))
			{

				store(e, da);
				k++;
			}
			pos++;
		}
	}

	private static void store(UnpackedEntry entry, DataOutputStream da) throws IOException
	{
		String entryPath = entry.getEntryPath();
		int category = entry.getCategory();
		Object value = entry.getValue();
		da.writeInt(category);
		da.writeUTF(entryPath);
		writeValue(da, value);
	}

	private static void writeValue(DataOutputStream da, Object value) throws IOException
	{
		if (value instanceof IReportable)
		{
			IReportable p = (IReportable) value;
			p.store(da);
			return;
		}
		throw new IllegalStateException("Illegal value:" + value); //$NON-NLS-1$
	}

	private static Object readValue(DataInputStream di) throws IOException
	{
		int cat = di.readInt();
		if (cat == IPHPIndexConstants.CLASS_CATEGORY)
		{
			return new ClassPHPEntryValue(di);
		}
		else if (cat == IPHPIndexConstants.VAR_CATEGORY)
		{
			return new VariablePHPEntryValue(di);
		}
		else if (cat == IPHPIndexConstants.FUNCTION_CATEGORY)
		{
			return new FunctionPHPEntryValue(di);
		}
		else if (cat == IPHPIndexConstants.IMPORT_CATEGORY)
		{
			return new IncludePHPEntryValue(di);
		}
		else if (cat == IPHPIndexConstants.LAMBDA_FUNCTION_CATEGORY)
		{
			return new LambdaFunctionPHPEntryValue(di);
		}
		IEntryValueFactory entryValueFactory = factories.get(cat);
		if (entryValueFactory != null)
		{
			return entryValueFactory.createValue(di);
		}
		else
		{
			throw new IOException("Index corrupted"); //$NON-NLS-1$
		}
	}

	private static IModule readModule(DataInputStream di, IBuildPath pb) throws IOException
	{
		String readUTF = di.readUTF();
		Path pa = new Path(readUTF);
		return pb.getModuleByPath(pa);
	}

	private static void writeModule(DataOutputStream da, IModule module, IBuildPath pb) throws IOException
	{
		String portableString = module.getPath().toPortableString();

		da.writeUTF(portableString);
	}

	public static void writeTypeSet(Set<Object> types, DataOutputStream da) throws IOException
	{
		if (types == null)
		{
			da.writeInt(0);
			return;
		}
		da.writeInt(types.size());
		for (Object o : types)
		{
			writeType(o, da);
		}
	}

	public static Set<Object> readTypeSet(DataInputStream di) throws IOException
	{
		int readInt = di.readInt();
		if (readInt == 0)
		{
			return Collections.emptySet();
		}
		if (readInt == 1)
		{
			return Collections.singleton(readType(di));
		}
		HashSet<Object> t = new HashSet<Object>(readInt);
		for (int a = 0; a < readInt; a++)
		{
			t.add(readType(di));
		}
		return t;
	}

	public static Object readType(DataInputStream di) throws IOException
	{
		int readInt = di.readInt();
		if (readInt == -1)
		{
			return null;
		}
		if (readInt == 0)
		{
			return di.readUTF().intern();
		}
		if (readInt == 1)
		{
			return AbstractPathReference.read(di);
		}
		if (readInt == 2)
		{
			int len = di.readInt();
			Object[] result = new Object[len];
			for (int a = 0; a < len; a++)
			{
				result[a] = readType(di);
			}
			return result;
		}
		throw new IllegalArgumentException("Unknown type " + readInt); //$NON-NLS-1$
	}

	public static void writeType(Object type, DataOutputStream da) throws IOException
	{
		if (type == null)
		{
			da.writeInt(-1);
			return;
		}
		if (type instanceof String)
		{
			da.writeInt(0);
			da.writeUTF((String) type);
			return;
		}
		if (type instanceof AbstractPathReference)
		{
			da.writeInt(1);
			AbstractPathReference p = (AbstractPathReference) type;
			p.write(da);
			return;
		}
		if (type instanceof Object[])
		{
			Object[] tps = (Object[]) type;
			da.writeInt(2);
			da.writeInt(tps.length);
			for (int a = 0; a < tps.length; a++)
			{
				writeType(tps[a], da);
			}
			return;
		}
		if (type instanceof Collection<?>)
		{
			Collection<?> col = (Collection<?>) type;
			da.writeInt(2);
			da.writeInt(col.size());
			for (Object obj : col)
			{
				writeType(obj, da);
			}
			return;
		}
		throw new IllegalArgumentException("Unknown type " + type); //$NON-NLS-1$
	}
}
