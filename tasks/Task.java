package strms.tasks;

import strms.enums.PriorityLevel;
import strms.enums.TaskCategory;
import strms.enums.TaskStatus;
import strms.users.Engineer;
import strms.users.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Task {

    private final String       id;
    private String             title;
    private String             description;
    private PriorityLevel      priority;
    private TaskStatus         status;
    private TaskCategory       category;
    private LocalDate          deadline;
    private Engineer           assignedEngineer;

    private final List<Task>              dependencies;

    private final List<TaskHistoryEntry>  history;

    public Task(String id, String title, String description, PriorityLevel priority, TaskCategory category, LocalDate deadline) {
        this.id           = id;
        this.title        = title;
        this.description  = description;
        this.priority     = priority;
        this.status       = TaskStatus.TODO;
        this.category     = category;
        this.deadline     = deadline;
        this.dependencies = new ArrayList<>();
        this.history      = new ArrayList<>();
    }

    public void updateStatus(TaskStatus newStatus, User performedBy) {
        TaskStatus oldStatus = this.status;
        this.status = newStatus;
        addHistoryEntry("Statut changé : " + oldStatus + " → " + newStatus, performedBy);
    }

    public void addHistoryEntry(String action, User performedBy) {
        history.add(new TaskHistoryEntry(action, performedBy));
    }

    public boolean areDependenciesDone() {
        for (Task dep : dependencies) {
            if (dep.getStatus() != TaskStatus.DONE) {
                return false;
            }
        }
        return true;
    }

    public void addDependency(Task task) {
        if (!dependencies.contains(task)) {
            dependencies.add(task);
        }
    }

    public void removeDependency(Task task) {
        dependencies.remove(task);
    }

    public void displayTask() {
        System.out.println("┌─────────────────────────────────────────");
        System.out.println("│ Tâche      : [" + id + "] " + title);
        System.out.println("│ Statut     : " + status);
        System.out.println("│ Priorité   : " + priority);
        System.out.println("│ Catégorie  : " + category);
        System.out.println("│ Deadline   : " + (deadline != null ? deadline : "Non définie"));
        System.out.println("│ Assignée à : " +
                (assignedEngineer != null ? assignedEngineer.getName() : "Non assignée"));
        System.out.println("│ Dépendances: " + dependencies.size());
        System.out.println("└─────────────────────────────────────────");
    }

    public void displayHistory() {
        System.out.println("=== Historique de la tâche [" + id + "] " + title + " ===");
        if (history.isEmpty()) {
            System.out.println("  (aucune entrée)");
        } else {
            for (TaskHistoryEntry entry : history) {
                System.out.println("  " + entry);
            }
        }
    }

    public void changePriority(PriorityLevel newPriority, User performedBy) {
        PriorityLevel old = this.priority;
        this.priority = newPriority;
        addHistoryEntry("Priorité changée : " + old + " → " + newPriority, performedBy);
    }

    public void updateDescription(String newDescription, User performedBy) {
        this.description = newDescription;
        addHistoryEntry("Description mise à jour", performedBy);
    }

    public boolean isBlocked() {
        return !areDependenciesDone() && status != TaskStatus.DONE;
    }

    @Override
    public int compareTo(Task other) {
        return Integer.compare(other.priority.getValue(), this.priority.getValue());
    }

    public String             getId()               { return id; }
    public String             getTitle()            { return title; }
    public String             getDescription()      { return description; }
    public PriorityLevel      getPriority()         { return priority; }
    public TaskStatus         getStatus()           { return status; }
    public TaskCategory       getCategory()         { return category; }
    public LocalDate          getDeadline()         { return deadline; }
    public Engineer           getAssignedEngineer() { return assignedEngineer; }
    public List<Task>         getDependencies()     { return dependencies; }
    public List<TaskHistoryEntry> getHistory()      { return history; }

    public void setAssignedEngineer(Engineer engineer) { this.assignedEngineer = engineer; }
    public void setDeadline(LocalDate deadline)        { this.deadline = deadline; }
    public void setTitle(String title)                 { this.title = title; }

    @Override
    public String toString() {
        return "[" + id + "] " + title + " (" + status + " / " + priority + ")";
    }


}
