package strms.users;

public class Manager extends User {

    public Manager(String id, String name, String email) {
        super(id, name, email);
    }

    public boolean canCreateTask()    { return false; }
    public boolean canDeleteTask()    { return false; }
    public boolean canAssignTask()    { return true; }
    public boolean canGenerateReport(){ return true; }
    public boolean canExecuteTask()   { return false; }
}
