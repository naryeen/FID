package org.openforis.collect.designer.viewmodel;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.designer.util.MediaUtil;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.utils.MediaTypes;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Filedownload;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BaseSurveyFileImportVM extends SurveyBaseVM {

	protected File uploadedFile;
	protected String uploadedFileName;
	private String[] allowedFileExtensions;
	private String exampleFileName;
	
	public BaseSurveyFileImportVM(String[] allowedFileExtensions, String exampleFileName) {
		this.allowedFileExtensions = allowedFileExtensions;
		this.exampleFileName = exampleFileName;
		reset();
	}
	
	@Override
	@Init(superclass=false)
	public void init() {
		super.init();
	}

	protected void reset() {
		if ( uploadedFile != null ) {
			uploadedFile.delete();
			uploadedFile = null;
		}
		uploadedFileName = null;
		notifyChange("uploadedFileName");
	}
	
	@Command
	public void fileUploaded(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
 		Media media = event.getMedia();
 		checkCanImportFile(media);
		this.uploadedFile = MediaUtil.copyToTempFile(media);
		this.uploadedFileName = media.getName();
		notifyChange("uploadedFileName");
	}
	
	@Command
	public void downloadExample() {
		ServletContext context = getSession().getWebApp().getServletContext();
		InputStream is = context.getResourceAsStream("/WEB-INF/resources/io/" + exampleFileName);
		Filedownload.save(is, MediaTypes.XLSX_CONTENT_TYPE, exampleFileName);
	}
	
	private void checkCanImportFile(Media media) {
		String fileName = media.getName();
		String extension = FilenameUtils.getExtension(fileName).toLowerCase(Locale.ENGLISH);
		if (!Arrays.asList(allowedFileExtensions).contains(extension)) {
			throw new RuntimeException(String.format("Only %s file upload is supported, found: %s", 
					String.join(",", allowedFileExtensions), extension));
		}
	}

	protected boolean validateForm(BindContext ctx) {
		String messageKey = null;
		if ( uploadedFile == null ) {
			messageKey = "global.file_not_selected";
		}
		if ( messageKey == null ) {
			return true;
		} else {
			MessageUtil.showWarning(messageKey);
			return false;
		}
	}
	
	public String getUploadedFileName() {
		return uploadedFileName;
	}
	
}
