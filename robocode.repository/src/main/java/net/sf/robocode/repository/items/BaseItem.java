/*******************************************************************************
 * Copyright (c) 2001, 2009 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://robocode.sourceforge.net/license/cpl-v10.html
 *
 * Contributors:
 *     Pavel Savara
 *     - Initial implementation
 *******************************************************************************/
package net.sf.robocode.repository.items;


import net.sf.robocode.repository.root.IRepositoryRoot;

import java.io.Serializable;
import java.net.URL;


/**
 * @author Pavel Savara (original)
 */
public abstract class BaseItem implements IItem, Serializable {
	private static final long serialVersionUID = 1L;

	protected URL url;
	protected IRepositoryRoot root;
	protected long lastModified;
	protected boolean isValid;

	public BaseItem(URL url, IRepositoryRoot root) {
		this.url = url;
		this.root = root;
		this.lastModified = 0;
	}

	public URL getFullUrl() {
		return url;
	}

	public IRepositoryRoot getRoot() {
		return root;
	}

	public long getLastModified() {
		return lastModified;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean value) {
		isValid = value;
	}
	
}