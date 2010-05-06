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
import java.util.Collections;
import java.util.List;

import com.aptana.editor.php.indexer.IPHPIndexConstants;

/**
 * PHP entry value for classes.
 * 
 * @author Denis Denisenko
 */
public class ClassPHPEntryValue extends AbstractPHPEntryValue {

	/**
	 * Name of the superclass.
	 */
	private String superClassName;

	/**
	 * Interface names.
	 */
	private List<String> interfaces;

	/**
	 * Class end offset.
	 */
	private int endOffset;

	/**
	 * ClassPHPEntryValue constructor.
	 * 
	 * @param modifiers
	 *            - modifiers.
	 */
	public ClassPHPEntryValue(int modifiers,String namespace) {
		super(modifiers,namespace);
	}

	/**
	 * ClassPHPEntryValue constructor.
	 * 
	 * @param modifiers
	 *            - modifiers.
	 * @param superClassName
	 *            - superclass name.
	 * @param interfaces
	 *            - interface names.
	 */
	public ClassPHPEntryValue(int modifiers, String superClassName,
			List<String> interfaces,String namespace) {
		super(modifiers,namespace);
		this.superClassName = superClassName;
		this.interfaces = interfaces;
	}

	public ClassPHPEntryValue(DataInputStream di) throws IOException {
		super(di);
		internalRead(di);
	}

	/**
	 * Sets interface names.
	 * 
	 * @param interfaces
	 *            - interfaces.
	 */
	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
	}

	/**
	 * Gets superclass name.
	 * 
	 * @return superclass name.
	 */
	public String getSuperClassname() {
		return superClassName;
	}

	/**
	 * Sets superclass name.
	 * 
	 * @param name
	 *            - superclass name.
	 */
	public void setSuperClassName(String superClassname) {
		this.superClassName = superClassname;
	}

	/**
	 * Gets interfaces.
	 * 
	 * @return interfaces. might be null.
	 */
	public List<String> getInterfaces() {
		return interfaces;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((interfaces == null) ? 0 : interfaces.hashCode());
		result = prime * result
				+ ((superClassName == null) ? 0 : superClassName.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ClassPHPEntryValue other = (ClassPHPEntryValue) obj;
		if (interfaces == null) {
			if (other.interfaces != null)
				return false;
		} else if (!listsEquals(interfaces, other.interfaces))
			return false;
		if (superClassName == null) {
			if (other.superClassName != null)
				return false;
		} else if (!superClassName.equals(other.superClassName))
			return false;
		return true;
	}

	/**
	 * Gets class end offset.
	 * 
	 * @return class end offset.
	 */
	public int getEndOffset() {
		return endOffset;
	}

	/**
	 * Sets class end offset.
	 * 
	 * @param startOffset
	 *            - offset.
	 */
	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

	/**
	 * Compares two string lists by value.
	 * 
	 * @param list1
	 *            - list1.
	 * @param list2
	 *            - list2.
	 * @return true if equals, false otherwise.
	 */
	private boolean listsEquals(List<String> list1, List<String> list2) {
		if (list1.size() != list2.size()) {
			return false;
		}

		for (int i = 0; i < list1.size(); i++) {
			String str1 = list1.get(i);
			String str2 = list2.get(i);
			if (!str1.equals(str2)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int getKind() {
		return IPHPIndexConstants.CLASS_CATEGORY;
	}

	@Override
	protected void internalWrite(DataOutputStream da) throws IOException {
		da.writeInt(endOffset);
		String s = superClassName != null ? superClassName : "";
		da.writeUTF(s);
		List<String> emptyList = Collections.emptyList();
		List<String> inter = interfaces != null ? interfaces : emptyList;
		da.writeInt(inter.size());
		for (String i : inter) {
			da.writeUTF(i);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void internalRead(DataInputStream di) throws IOException {
		endOffset = di.readInt();
		superClassName = di.readUTF().intern();
		int sz = di.readInt();
		if (sz == 0) {
			interfaces = Collections.emptyList();
		}
		if (sz == 1) {
			interfaces = (List<String>) Collections.singletonList(di.readUTF());
		} else {
			ArrayList<String> s = new ArrayList<String>(sz);
			for (int a = 0; a < sz; a++) {
				s.add(di.readUTF());
			}
			interfaces = s;
		}
	}

}