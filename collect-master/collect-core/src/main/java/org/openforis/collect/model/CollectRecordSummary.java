package org.openforis.collect.model;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.idm.metamodel.ModelVersion;

public class CollectRecordSummary {

	public static CollectRecordSummary fromRecord(CollectRecord r) {
		CollectSurvey survey = (CollectSurvey) r.getSurvey();
		ModelVersion version = r.getVersion();
		CollectRecordSummary summary = new CollectRecordSummary();
		summary.setCreatedBy(r.getCreatedBy());
		summary.setCreationDate(r.getCreationDate());
		summary.setId(r.getId());
		summary.setModifiedBy(r.getModifiedBy());
		summary.setModifiedDate(r.getModifiedDate());
		summary.setSurvey(survey);
		summary.setVersion(version);
		summary.setOwner(r.getOwner());
		summary.setRootEntityDefinitionId(r.getRootEntityDefinitionId());
		summary.setStep(r.getStep());
		summary.setWorkflowSequenceNumber(r.getWorkflowSequenceNumber());
		
		StepSummary stepSummary = new StepSummary(r.getStep());
		stepSummary.setSequenceNumber(r.getDataWorkflowSequenceNumber());
		stepSummary.setState(r.getState());
		stepSummary.setCompletionPercent(r.calculateCompletionPercent());
		stepSummary.setEntityCounts(r.getEntityCounts());
		stepSummary.setTotalErrors(r.getTotalErrors());
		stepSummary.setErrors(r.getErrors());
		stepSummary.setFilledAttributesCount(r.countTotalFilledAttributes());
		stepSummary.setMissing(r.getMissing());
		stepSummary.setMissingErrors(r.getMissingErrors());
		stepSummary.setMissingWarnings(r.getMissingWarnings());
		stepSummary.setRootEntityKeyValues(r.getRootEntityKeyValues());
		stepSummary.setSkipped(r.getSkipped());
		stepSummary.setTotalErrors(r.getTotalErrors());
		stepSummary.setWarnings(r.getWarnings());
		
		summary.addStepSummary(stepSummary);
		return summary;
	}

	private Integer id;
	private CollectSurvey survey;
	private ModelVersion version;
	private Integer rootEntityDefinitionId;
	private User createdBy;
	private Date creationDate;
	private User modifiedBy;
	private Date modifiedDate;
	private Step step;
	private Integer workflowSequenceNumber;
	private User owner;
	private String lockedBy;
	private List<File> files;
	private Map<Step, StepSummary> stepSummaries = new LinkedHashMap<Step, StepSummary>();
	
	public StepSummary getCurrentStepSummary() {
		return stepSummaries.get(getStep());
	}
	
	public void addStepSummary(StepSummary stepSummary) {
		stepSummaries.put(stepSummary.getStep(), stepSummary);
	}
	
	public void clearStepSummaries() {
		stepSummaries.clear();
	}
	
	public StepSummary getSummaryByStep(Step step) {
		return stepSummaries.get(step);
	}
	
	public int getCompletionPercent() {
		return getCurrentStepSummary().getCompletionPercent();
	}

	public int getFilledAttributesCount() {
		return getCurrentStepSummary().getFilledAttributesCount();
	}
	
	public List<String> getRootEntityKeyValues() {
		return getCurrentStepSummary().getRootEntityKeyValues();
	}
	
	public List<Integer> getEntityCounts() {
		return getCurrentStepSummary().getEntityCounts();
	}

	public Map<Step, StepSummary> getStepSummaries() {
		return stepSummaries;
	}
	
	public void setStepSummaries(Map<Step, StepSummary> summaryByStep) {
		this.stepSummaries = summaryByStep;
	}
	
	public Step getStep() {
		return step;
	}
	
	public void setStep(Step step) {
		this.step = step;
	}

	public Integer getWorkflowSequenceNumber() {
		return workflowSequenceNumber;
	}
	
	public void setWorkflowSequenceNumber(Integer workflowSequenceNumber) {
		this.workflowSequenceNumber = workflowSequenceNumber;
	}
	
	public Integer getRootEntityDefinitionId() {
		return rootEntityDefinitionId;
	}
	
	public void setRootEntityDefinitionId(Integer rootEntityDefinitionId) {
		this.rootEntityDefinitionId = rootEntityDefinitionId;
	}
	
	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(User modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

	public ModelVersion getVersion() {
		return version;
	}

	public void setVersion(ModelVersion version) {
		this.version = version;
	}
	
	public User getOwner() {
		return owner;
	}
	
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	public String getLockedBy() {
		return lockedBy;
	}
	
	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}

	public List<File> getFiles() {
		return files;
	}
	
	public void setFiles(List<File> files) {
		this.files = files;
	}

	public static class StepSummary {
		
		private Integer sequenceNumber;
		private Step step;
		private State state;
		private User createdBy;
		private Date creationDate;
		private User modifiedBy;
		private Date modifiedDate;
		private int completionPercent;
		private List<Integer> entityCounts;
		private Integer errors;
		private Integer warnings;
		private int filledAttributesCount;
		private Integer missing;
		private Integer missingErrors;
		private Integer missingWarnings;
		private List<String> rootEntityKeyValues;
		private List<String> qualifierValues;
		private List<String> summaryValues;
		private Integer skipped;
		private Integer totalErrors;
		
		public StepSummary(Step step) {
			this.step = step;
		}
		
		public Integer getSequenceNumber() {
			return sequenceNumber;
		}
		
		public void setSequenceNumber(Integer sequenceNumber) {
			this.sequenceNumber = sequenceNumber;
		}
		
		public Step getStep() {
			return step;
		}
		
		public State getState() {
			return state;
		}
		
		public void setState(State state) {
			this.state = state;
		}
		
		public List<String> getRootEntityKeyValues() {
			return rootEntityKeyValues;
		}
		
		public void setRootEntityKeyValues(List<String> rootEntityKeyValues) {
			this.rootEntityKeyValues = rootEntityKeyValues;
		}

		public List<String> getQualifierValues() {
			return qualifierValues;
		}
		
		public void setQualifierValues(List<String> qualifierValues) {
			this.qualifierValues = qualifierValues;
		}
		
		public List<String> getSummaryValues() {
			return summaryValues;
		}
		
		public void setSummaryValues(List<String> summaryValues) {
			this.summaryValues = summaryValues;
		}
		
		public int getCompletionPercent() {
			return completionPercent;
		}
		
		public void setCompletionPercent(int completionPercent) {
			this.completionPercent = completionPercent;
		}
		
		public List<Integer> getEntityCounts() {
			return entityCounts;
		}

		public void setEntityCounts(List<Integer> entityCounts) {
			this.entityCounts = entityCounts;
		}

		public Integer getErrors() {
			return errors;
		}

		public void setErrors(Integer errors) {
			this.errors = errors;
		}
		
		public Integer getWarnings() {
			return warnings;
		}
		
		public void setWarnings(Integer warnings) {
			this.warnings = warnings;
		}
		
		public Integer getMissing() {
			return missing;
		}

		public void setMissing(Integer missing) {
			this.missing = missing;
		}

		public Integer getMissingErrors() {
			return missingErrors;
		}
		
		public void setMissingErrors(Integer missingErrors) {
			this.missingErrors = missingErrors;
		}
		
		public Integer getMissingWarnings() {
			return missingWarnings;
		}
		
		public void setMissingWarnings(Integer missingWarnings) {
			this.missingWarnings = missingWarnings;
		}
		
		public Integer getSkipped() {
			return skipped;
		}

		public void setSkipped(Integer skipped) {
			this.skipped = skipped;
		}

		public int getFilledAttributesCount() {
			return filledAttributesCount;
		}

		public void setFilledAttributesCount(int filledAttributesCount) {
			this.filledAttributesCount = filledAttributesCount;
		}

		public Integer getTotalErrors() {
			return totalErrors;
		}

		public void setTotalErrors(Integer totalErrors) {
			this.totalErrors = totalErrors;
		}

		public User getCreatedBy() {
			return createdBy;
		}

		public void setCreatedBy(User createdBy) {
			this.createdBy = createdBy;
		}

		public Date getCreationDate() {
			return creationDate;
		}

		public void setCreationDate(Date creationDate) {
			this.creationDate = creationDate;
		}

		public User getModifiedBy() {
			return modifiedBy;
		}

		public void setModifiedBy(User modifiedBy) {
			this.modifiedBy = modifiedBy;
		}

		public Date getModifiedDate() {
			return modifiedDate;
		}

		public void setModifiedDate(Date modifiedDate) {
			this.modifiedDate = modifiedDate;
		}
	}

}
