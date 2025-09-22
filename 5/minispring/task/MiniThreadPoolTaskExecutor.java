package minispring.task;

public class MiniThreadPoolTaskExecutor implements TaskExecutor {
    @Override
    public void execute(Runnable task) {
        new Thread(task).start();
    }
}
