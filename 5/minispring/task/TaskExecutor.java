package minispring.task;

@FunctionalInterface
public interface TaskExecutor {
    void execute(Runnable task);
}
