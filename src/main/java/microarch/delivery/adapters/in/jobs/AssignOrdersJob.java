package microarch.delivery.adapters.in.jobs;

import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.application.commands.assignorder.AssignOrderCommand;
import microarch.delivery.core.application.commands.assignorder.AssignOrderCommandHandler;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
public class AssignOrdersJob implements Job {
    private final AssignOrderCommandHandler assignOrderCommandHandler;

    public AssignOrdersJob(AssignOrderCommandHandler assignOrderCommandHandler) {
        this.assignOrderCommandHandler = assignOrderCommandHandler;
    }

    @Override
    public void execute(JobExecutionContext context) {
        assignOrderCommandHandler.handle(AssignOrderCommand.create().getValue())
                .onFailure(error -> log.debug("Order was not assigned: {}", error.getMessage()));
    }
}
