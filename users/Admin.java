package strms.users;

public class Admin extends User {

    public Admin(String id, String name, String email) {
        super(id, name, email);
    }

    public boolean canCreateTask()    { return true; }
    public boolean canDeleteTask()    { return true; }
    public boolean canAssignTask()    { return true; }
    public boolean canGenerateReport(){ return true; }
    public boolean canExecuteTask()   { return false; }
}
