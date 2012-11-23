package de.jutzig.jabylon.team.cvs.impl;

import java.io.File;
import java.io.FileFilter;

public class CVSFileFilter implements FileFilter{

	@Override
	public boolean accept(File pathname) {
		return !"CVS".equals(pathname.getName());
	}

}
