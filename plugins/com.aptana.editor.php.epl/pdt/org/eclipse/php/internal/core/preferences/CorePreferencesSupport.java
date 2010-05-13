/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.php.internal.core.preferences;

import com.aptana.editor.php.epl.Activator;


public class CorePreferencesSupport extends PreferencesSupport {

	private static CorePreferencesSupport corePreferencesSupport;

	private CorePreferencesSupport() {
		super(Activator.PLUGIN_ID, Activator.getDefault() == null ? null
				: Activator.getDefault().getPluginPreferences());
	}

	public static CorePreferencesSupport getInstance() {
		if (corePreferencesSupport == null) {
			corePreferencesSupport = new CorePreferencesSupport();
		}

		return corePreferencesSupport;
	}
}