package strms.tasks;

import strms.enums.TaskStatus;
import strms.exceptions.*;
import strms.users.Engineer;
import strms.users.User;

import java.util.*;

public class TaskManager {

    private final Map<String, Task> allTasks;
    private final Map<String, User> allUsers;
    private final PriorityQueue<Task> taskQueue;
    private final Set<Task> inProgressTasks;

    public TaskManager() {
        this.allTasks = new HashMap<>();
        this.allUsers = new HashMap<>();
        this.taskQueue = new PriorityQueue<>();
        this.inProgressTasks = new HashSet<>();
    }

    public void addUser(User user) {
        allUsers.put(user.getId(), user);
    }

    public User findUser(String userId) throws TaskNotFoundException {
        User user = allUsers.get(userId);
        if (user == null) {
            throw new TaskNotFoundException("Utilisateur introuvable : " + userId);
        }
        return user;
    }

    public void addTask(Task task, User requester)
            throws InvalidRoleException, DuplicateTaskException {

        if (!requester.canCreateTask()) {
            throw new InvalidRoleException(
                    requester.getName() + " n'a pas le droit de créer des tâches.");
        }
        if (allTasks.containsKey(task.getId())) {
            throw new DuplicateTaskException(
                    "Une tâche avec l'ID " + task.getId() + " existe déjà.");
        }

        allTasks.put(task.getId(), task);
        taskQueue.offer(task);
        task.addHistoryEntry("Tâche créée par " + requester.getName(), requester);

        System.out.println("[AJOUT] " + task);
    }

    public void deleteTask(String taskId, User requester)
            throws InvalidRoleException, TaskNotFoundException {

        if (!requester.canDeleteTask()) {
            throw new InvalidRoleException(
                    requester.getName() + " n'a pas le droit de supprimer des tâches.");
        }

        Task task = findTask(taskId);
        allTasks.remove(taskId);
        taskQueue.remove(task);
        inProgressTasks.remove(task);

        System.out.println("[SUPPRESSION] " + task + " supprimée par " + requester.getName());
    }

    public void assignTask(String taskId, Engineer engineer, User requester)
            throws InvalidRoleException, TaskNotFoundException, DependencyNotCompletedException {

        if (!requester.canAssignTask()) {
            throw new InvalidRoleException(
                    requester.getName() + " n'a pas le droit d'assigner des tâches.");
        }

        Task task = findTask(taskId);

        if (task.getStatus() == TaskStatus.DONE) {
            System.out.println("[INFO] La tâche " + taskId + " est déjà terminée.");
            return;
        }

        if (!task.areDependenciesDone()) {
            StringBuilder blockers = new StringBuilder();
            for (Task dep : task.getDependencies()) {
                if (dep.getStatus() != TaskStatus.DONE) {
                    blockers.append(dep.getId()).append(" ");
                }
            }
            task.addHistoryEntry(
                    "Tentative d'assignation refusée — dépendances non terminées : " + blockers,
                    requester);
            throw new DependencyNotCompletedException(
                    "Impossible d'assigner " + taskId + " : dépendances non terminées → " + blockers);
        }

        task.setAssignedEngineer(engineer);
        task.updateStatus(TaskStatus.IN_PROGRESS, requester);
        inProgressTasks.add(task);
        task.addHistoryEntry("Assignée à " + engineer.getName(), requester);

        System.out.println("[ASSIGNATION] " + task + " → " + engineer.getName());
    }

    public void completeTask(String taskId, User requester)
            throws InvalidRoleException, TaskNotFoundException, InvalidTaskStateException {

        Task task = findTask(taskId);
        if (task.getAssignedEngineer() == null ||
                !task.getAssignedEngineer().getId().equals(requester.getId())) {
            throw new InvalidRoleException(
                    requester.getName() + " n'est pas l'ingénieur assigné à cette tâche.");
        }

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new InvalidTaskStateException(
                    "La tâche " + taskId + " n'est pas en cours (statut : " + task.getStatus() + ").");
        }

        task.updateStatus(TaskStatus.DONE, requester);
        inProgressTasks.remove(task);
        taskQueue.remove(task);

        task.addHistoryEntry("Tâche complétée par " + requester.getName(), requester);
        System.out.println("[TERMINÉE] " + task);
    }

    public void addDependency(String taskId, String dependsOnId, User requester)
            throws TaskNotFoundException, CircularDependencyException {

        Task task = findTask(taskId);
        Task dependsOn = findTask(dependsOnId);

        if (taskId.equals(dependsOnId)) {
            throw new CircularDependencyException(
                    "Une tâche ne peut pas dépendre d'elle-même : " + taskId);
        }
        if (detectCircularDependency(dependsOn, task)) {
            task.addHistoryEntry(
                    "Dépendance refusée (cycle détecté) : " + taskId + " → " + dependsOnId,
                    requester);
            throw new CircularDependencyException(
                    "Ajout refusé : " + taskId + " → " + dependsOnId +
                            " créerait une dépendance circulaire.");
        }

        task.addDependency(dependsOn);

        if (task.getStatus() == TaskStatus.TODO && !task.areDependenciesDone()) {
            task.updateStatus(TaskStatus.BLOCKED, requester);
        }

        task.addHistoryEntry("Dépendance ajoutée : " + taskId + " dépend de " + dependsOnId, requester);
        System.out.println("[DÉPENDANCE] " + taskId + " dépend de " + dependsOnId);
    }

    public void removeDependency(String taskId, String dependsOnId, User requester)
            throws TaskNotFoundException {

        Task task = findTask(taskId);
        Task dependsOn = findTask(dependsOnId);

        task.removeDependency(dependsOn);
        task.addHistoryEntry("Dépendance supprimée : " + taskId + " ne dépend plus de " + dependsOnId, requester);

        if (task.getStatus() == TaskStatus.BLOCKED && task.areDependenciesDone()) {
            task.updateStatus(TaskStatus.TODO, requester);
        }

        System.out.println("[DÉPENDANCE] Supprimée : " + taskId + " → " + dependsOnId);
    }

    public boolean detectCircularDependency(Task current, Task target) {
        Set<String> visited = new HashSet<>();
        return dfs(current, target.getId(), visited);
    }

    private boolean dfs(Task current, String targetId, Set<String> visited) {
        if (current.getId().equals(targetId)) {
            return true;
        }
        if (visited.contains(current.getId())) {
            return false;
        }

        visited.add(current.getId());

        for (Task dep : current.getDependencies()) {
            if (dfs(dep, targetId, visited)) {
                return true;
            }
        }
        return false;
    }

    public Task findTask(String taskId) throws TaskNotFoundException {
        Task task = allTasks.get(taskId);
        if (task == null) {
            throw new TaskNotFoundException("Tâche introuvable : " + taskId);
        }
        return task;
    }

    public void printInProgressTasks() {
        System.out.println("=== Tâches en cours ===");
        if (inProgressTasks.isEmpty()) {
            System.out.println("  Aucune tâche en cours.");
        } else {
            for (Task t : inProgressTasks) {
                System.out.println("  " + t);
            }
        }
    }

    public void printAllTasks() {
        System.out.println("=== Toutes les tâches (" + allTasks.size() + ") ===");
        for (Task t : allTasks.values()) {
            System.out.println("  " + t);
        }
    }

    public Task getNextTask() {
        return taskQueue.peek();
    }

    public Map<String, Task> getAllTasks() {
        return Collections.unmodifiableMap(allTasks);
    }

    public Map<String, User> getAllUsers() {
        return Collections.unmodifiableMap(allUsers);
    }

    public Set<Task> getInProgressTasks() {
        return Collections.unmodifiableSet(inProgressTasks);
    }
}