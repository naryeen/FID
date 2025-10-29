package org.openforis.collect.web.ws;

import java.util.Timer;
import java.util.TimerTask;

import org.openforis.collect.web.ws.WebSocketMessageSender.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppWS {

	@Autowired
	private WebSocketMessageSender messageSender;

	public enum MessageType {
		SURVEYS_UPDATED, SURVEY_UPDATED, SURVEY_PUBLISHED, SURVEY_UNPUBLISHED, SURVEY_DELETED, RECORD_LOCKED,
		RECORD_UNLOCKED, RECORD_UPDATED, RECORD_UPDATE_ERROR
	}

	public void sendMessage(MessageType type) {
		sendMessage(type, 0);
	}

	public void sendMessage(MessageType type, int delay) {
		sendMessage(new Message(type.name()), delay);
	}

	public void sendMessage(Message message) {
		sendMessage(message, 0);
	}

	public void sendMessage(final Message message, int delay) {
		if (delay > 0) {
			new Timer().schedule(new TimerTask() {
				public void run() {
					messageSender.send(message);
				}
			}, delay);
		} else {
			messageSender.send(message);
		}
	}

	public static class SurveyMessage extends Message {

		private int surveyId;

		public SurveyMessage(MessageType messageType, int surveyId) {
			super(messageType.name());
			this.surveyId = surveyId;
		}

		public int getSurveyId() {
			return surveyId;
		}
	}

	private static abstract class RecordMessage extends Message {

		private Integer recordId;

		public RecordMessage(MessageType type, int recordId) {
			super(type.name());
			this.recordId = recordId;
		}

		public Integer getRecordId() {
			return recordId;
		}

	}

	public static class RecordLockedMessage extends RecordMessage {

		private String lockedBy;

		public RecordLockedMessage(int recordId, String lockedBy) {
			super(MessageType.RECORD_LOCKED, recordId);
			this.lockedBy = lockedBy;
		}

		public String getLockedBy() {
			return lockedBy;
		}
	}

	public static class RecordUnlockedMessage extends RecordMessage {

		public RecordUnlockedMessage(int recordId) {
			super(MessageType.RECORD_UNLOCKED, recordId);
		}
	}

	public static class RecordEventMessage extends Message {
		private Object event;

		public RecordEventMessage(Object event) {
			super(MessageType.RECORD_UPDATED.name());
			this.event = event;
		}

		public Object getEvent() {
			return event;
		}
	}

	public static class RecordUpdateErrorMessage extends Message {

		private String message;
		private String stackTrace;

		public RecordUpdateErrorMessage(String message, String stackTrace) {
			super(MessageType.RECORD_UPDATE_ERROR.name());
			this.message = message;
			this.stackTrace = stackTrace;
		}

		public String getMessage() {
			return message;
		}

		public String getStackTrace() {
			return stackTrace;
		}
	}
}
