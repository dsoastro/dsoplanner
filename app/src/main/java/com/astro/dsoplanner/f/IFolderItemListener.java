package com.astro.dsoplanner.f;

import java.io.File;

public interface IFolderItemListener {
	
	void onCannotFileRead(File file);
	void onFileClicked(File file);

}
