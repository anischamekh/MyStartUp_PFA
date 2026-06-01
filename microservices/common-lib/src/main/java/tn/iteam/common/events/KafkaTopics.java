package tn.iteam.common.events;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String NOTIFICATION_EVENTS = "notification-events";
    public static final String LEAVE_EVENTS = "leave-events";
    public static final String TASK_EVENTS = "task-events";
    public static final String PROJECT_EVENTS = "project-events";
    public static final String CHATBOT_LOGS = "chatbot-conversation-logs";
    public static final String TRAINING_REMINDERS = "training-reminders";
    public static final String USER_EVENTS = "user-events";
}
