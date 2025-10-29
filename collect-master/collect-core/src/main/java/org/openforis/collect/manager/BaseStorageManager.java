package org.openforis.collect.manager;

import static org.openforis.collect.utils.Files.TEMP_FOLDER;
import static org.openforis.collect.utils.Files.getReadableSysPropLocation;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openforis.collect.model.Configuration;
import org.openforis.collect.model.Configuration.ConfigurationItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class BaseStorageManager implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LogManager.getLogger(BaseStorageManager.class);
	
	protected static final String DATA_FOLDER_NAME = "data";

	private static final String OPENFORIS_FOLDER_NAME = "OpenForis";
	private static final String COLLECT_FOLDER_NAME = "Collect";

	private static final String DATA_SUBDIR = OPENFORIS_FOLDER_NAME + File.separator + COLLECT_FOLDER_NAME + File.separator + DATA_FOLDER_NAME;
	
	private static final String USER_HOME = "user.home";

	private static final File USER_HOME_DATA_FOLDER = getReadableSysPropLocation(USER_HOME, DATA_SUBDIR);
	
	@Autowired
	protected transient ConfigurationManager configurationManager;

	/**
	 * Directory in which files will be stored
	 */
	protected File storageDirectory;
	
	/**
	 * Default path in which files will be stored.
	 * If not specified, java temp folder or catalina base temp folder
	 * will be used as storage directory.
	 */
	private String defaultRootStoragePath;
	
	/**
	 * Default subfolder used together with the default storage path to determine the default storage folder
	 */
	private String defaultSubFolder;
	
	public BaseStorageManager() {
		this(null, null);
	}
	
	public BaseStorageManager(String defaultSubFolder) {
		this(null, defaultSubFolder);
	}
	
	public BaseStorageManager(String defaultRootStoragePath, String defaultSubFolder) {
		this.defaultRootStoragePath = defaultRootStoragePath;
		this.defaultSubFolder = defaultSubFolder;
	}
	
	protected void initStorageDirectory(ConfigurationItem configurationItem) {
		initStorageDirectory(configurationItem, true);
	}
	
	protected boolean initStorageDirectory(ConfigurationItem configurationItem, boolean createIfNotExists) {
		Configuration configuration = configurationManager.getConfiguration();
		
		String customStoragePath = configuration.get(configurationItem);
		
		this.storageDirectory = StringUtils.isBlank(customStoragePath) ? getDefaultStorageDirectory() : new File(customStoragePath);

		boolean storageDirectoryAccessible = storageDirectory.exists();
		if (! storageDirectoryAccessible) {
			if (createIfNotExists) {
				storageDirectoryAccessible = storageDirectory.mkdirs();
			}
		}
		
		if (LOG.isInfoEnabled() ) {
			if (storageDirectoryAccessible) {
				LOG.info(String.format("Using %s directory: %s", configurationItem.getLabel(), storageDirectory.getAbsolutePath()));
			} else {
				LOG.info(String.format("%s directory %s does not exist or it's not accessible", configurationItem.getLabel(), storageDirectory.getAbsolutePath()));
			}
		}
		return storageDirectoryAccessible;
	}

	protected File getDefaultStorageRootDirectory() {
		if ( defaultRootStoragePath == null ) {
			//try to use user home data folder
			File result = USER_HOME_DATA_FOLDER;
			if ( result == null ) {
				//try to use data folder in catalina.base path
				result = getReadableSysPropLocation("catalina.base", DATA_FOLDER_NAME);
				if (result == null) {
					//try to use collect.root system property
					String rootPath = System.getProperty("collect.root");
					if (rootPath != null) {
						File webappsFolder = new File(rootPath).getParentFile();
						File baseFolder = webappsFolder.getParentFile();
						result = new File(baseFolder, DATA_FOLDER_NAME);
					}
				}
			}
			return result == null || !result.exists() ? TEMP_FOLDER : result;
		} else {
			return new File(defaultRootStoragePath);
		}
	}
	
	public String getDefaultRootStoragePath() {
		return defaultRootStoragePath;
	}
	
	public void setDefaultRootStoragePath(String defaultStoragePath) {
		this.defaultRootStoragePath = defaultStoragePath;
	}
	
	public File getDefaultStorageDirectory() {
		File rootDir = getDefaultStorageRootDirectory();
		if ( rootDir == null ) {
			return null;
		} else if ( defaultSubFolder == null ) {
			return rootDir;
		} else {
			return new File(rootDir, defaultSubFolder);
		}
	}
	
	public File getStorageDirectory() {
		return storageDirectory;
	}
	
	protected void setDefaultSubFolder(String defaultSubFolder) {
		this.defaultSubFolder = defaultSubFolder;
	}
}
