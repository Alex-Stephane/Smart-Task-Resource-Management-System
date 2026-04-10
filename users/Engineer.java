package strms.users;

public class Engineer extends User {

    public Engineer(String id, String name, String email) {
        super(id, name, email);
    }

    public boolean canCreateTask()    { return false; }
    public boolean canDeleteTask()    { return false; }
    public boolean canAssignTask()    { return false; }
    public boolean canGenerateReport(){ return false; }
    public boolean canExecuteTask()   { return true; }
}
