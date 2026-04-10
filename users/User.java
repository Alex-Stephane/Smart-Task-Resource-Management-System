package strms.users;

public abstract class User {

    private String id;
    private String name;
    private String email;

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public abstract boolean canCreateTask();

    public abstract boolean canDeleteTask();

    public abstract boolean canAssignTask();

    public abstract boolean canGenerateReport();

    public abstract boolean canExecuteTask();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + id + " - " + name + "]";
    }
}
