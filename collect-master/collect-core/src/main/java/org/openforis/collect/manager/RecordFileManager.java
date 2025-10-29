package org.openforis.collect.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.model.FileAttribute;

/**
 * 
 * @author S. Ricci
 * 
 */
public class RecordFileManager extends BaseStorageManager {
	
	private static final long serialVersionUID = 1L;
	private static final Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	protected static final Logger LOG = LogManager.getLogger(RecordFileManager.class);

	private static final String DEFAULT_RECORD_FILES_SUBFOLDER = "collect_upload";
	
	public RecordFileManager() {
		super(DEFAULT_RECORD_FILES_SUBFOLDER);
	}
	
	public void init() {
		initStorageDirectory();
	}

	protected void initStorageDirectory() {
		super.initStorageDirectory(ConfigurationItem.RECORD_FILE_UPLOAD_PATH);
	}
	
	public void deleteAllFiles(CollectRecord record) {
		List<java.io.File> files = getAllFiles(record);
		for (java.io.File file : files) {
			file.delete();
		}
	}
	
	public List<java.io.File> getAllFiles(CollectRecord record) {
		List<java.io.File> result = new ArrayList<java.io.File>();
		for (FileAttribute fileAttribute : record.getFileAttributes()) {
			java.io.File repositoryFile = getRepositoryFile(fileAttribute);
			if (repositoryFile != null ) {
				result.add(repositoryFile);
			}
		}
		return result;
	}
	
	/**
	 * Moves a file into the repository and associates the file name to the corresponding file attribute node 
	 * Returns true if the record is modified (file name or size different from the old one).
	 */
	public boolean moveFileIntoRepository(CollectRecord record, int nodeId, java.io.File newFile, String originalFileName) throws IOException {
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		return moveFileIntoRepository(fileAttribute, newFile, originalFileName);
	}
	
	public boolean moveFileIntoRepository(FileAttribute fileAttribute, java.io.File newFile, String originalFileName) throws IOException {
		org.openforis.idm.model.File value = moveFileIntoRepository(fileAttribute, newFile, originalFileName, true);
		return value != null;
	}
	
	public org.openforis.idm.model.File moveFileIntoRepository(FileAttribute fileAttribute, java.io.File newFile, String originalFileName, boolean updateRecord) throws IOException {
		String repositoryFileName = isUniqueFileName(originalFileName) ? originalFileName
				: generateUniqueRepositoryFileName(fileAttribute, newFile);
		
		FileAttributeDefinition defn = fileAttribute.getDefinition();
		File repositoryFile = new java.io.File(getRepositoryDir(defn), repositoryFileName);
		
		org.openforis.idm.model.File value = null;
		long repositoryFileSize = newFile.length();
		if ( ! repositoryFileName.equals(fileAttribute.getFilename() ) || 
				! Long.valueOf(repositoryFileSize).equals(fileAttribute.getSize()) ) {
			value = new org.openforis.idm.model.File(repositoryFileName, repositoryFileSize);
			if (updateRecord) {
				fileAttribute.setValue(value);
			}
		}
		
		FileUtils.moveFile(newFile, repositoryFile);
		
		return value;
	}
	
	public org.openforis.idm.model.File generateFileAttributeValue(FileAttribute fileAttribute, java.io.File file) {
		String repositoryFileName = generateUniqueRepositoryFileName(fileAttribute, file);
		return new org.openforis.idm.model.File(repositoryFileName, file.length());
	}
	
	private boolean isUniqueFileName(String fileName) {
		String baseName = FilenameUtils.getBaseName(fileName);
		return UUID_REGEX.matcher(baseName).matches(); 
	}
	
	private String generateUniqueRepositoryFileName(FileAttribute fileAttribute, java.io.File file) {
		java.io.File repositoryDir = getRepositoryDir(fileAttribute.getDefinition());
		String repositoryFileName;
		File repositoryFile;
		do {
			repositoryFileName = generateNewRepositoryFilename(fileAttribute, file.getName());
			repositoryFile = new java.io.File(repositoryDir, repositoryFileName);
		} while (repositoryFile.exists());
		
		return repositoryFileName;
	}

	private String generateNewRepositoryFilename(FileAttribute fileAttribute, String tempFileName) {
		String extension = FilenameUtils.getExtension(tempFileName);
		return String.format("%s.%s", UUID.randomUUID(), extension);
	}

	protected java.io.File getRepositoryDir(FileAttributeDefinition defn) {
		java.io.File baseDirectory = storageDirectory;
		String relativePath = getRepositoryRelativePath(defn);
		java.io.File file = new java.io.File(baseDirectory, relativePath);
		return file;
	}

	public static String getRepositoryRelativePath(FileAttributeDefinition defn) {
		return getRepositoryRelativePath(defn, java.io.File.separator, true);
	}

	public static String getRepositoryRelativePath(FileAttributeDefinition defn, 
			String directorySeparator, boolean surveyRelative) {
		Survey survey = defn.getSurvey();
		StringBuilder sb = new StringBuilder();
		if ( surveyRelative ) {
			sb.append(survey.getId());
			sb.append(directorySeparator);
		}
		sb.append(defn.getId());
		return sb.toString();
	}
	
	public java.io.File getRepositoryFile(CollectRecord record, int nodeId) {
		FileAttribute fileAttribute = (FileAttribute) record.getNodeByInternalId(nodeId);
		if ( fileAttribute == null ) {
			return null;
		} else {
			return getRepositoryFile(fileAttribute);
		}
	}
	
	public java.io.File getRepositoryFile(FileAttribute fileAttribute) {
		String filename = fileAttribute.getFilename();
		if ( StringUtils.isNotBlank(filename) ) {
			FileAttributeDefinition defn = fileAttribute.getDefinition();
			java.io.File file = getRepositoryFile(defn, filename);
			return file;
		} else {
			return null;
		}
	}
	
	public boolean deleteRepositoryFile(FileAttribute fileAttribute) {
		File file = getRepositoryFile(fileAttribute);
		return FileUtils.deleteQuietly(file);
	}

	protected java.io.File getRepositoryFile(FileAttributeDefinition fileAttributeDefn, String fileName) {
		java.io.File repositoryDir = getRepositoryDir(fileAttributeDefn);
		java.io.File file = new java.io.File(repositoryDir, fileName);
		return file;
	}

	public String getRepositoryFileAbsolutePath(FileAttribute fileAttribute) {
		FileAttributeDefinition defn = fileAttribute.getDefinition();
		String filename = fileAttribute.getFilename();
		if ( StringUtils.isNotBlank(filename) ) {
			String path = getRepositoryFileAbsolutePath(defn, filename);
			return path;
		} else {
			return null;
		}
	}
	
	public String getRepositoryFileAbsolutePath(FileAttributeDefinition fileAttributeDefn, String fileName) {
		java.io.File repositoryDir = getRepositoryDir(fileAttributeDefn);
		java.io.File file = new java.io.File(repositoryDir, fileName);
		return file.getAbsolutePath();
	}
		
}
