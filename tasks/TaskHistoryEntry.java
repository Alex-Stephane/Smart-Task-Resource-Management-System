package strms.tasks;

import strms.users.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskHistoryEntry {

    private final String action;
    private final User performedBy;
    private final LocalDateTime timestamp;

    private static final meForma RMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public TaskHistoryEntry(String action, User performedBy) {
        this.action = action;
        this.performedBy = performedBy;
        this.timestamp = LocalDateTime.now();
    }

    public TaskHistoryEntry(String action, User performedBy, LocalDateTime timestamp) {
        this.action = action;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public User getPerformedBy() {
        return performedBy;
    }

    public LocalDateTi getTimestamp() {
        return timestamp;
    }

}