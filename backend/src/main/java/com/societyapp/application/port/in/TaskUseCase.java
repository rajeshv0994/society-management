package com.societyapp.application.port.in;

import com.societyapp.application.model.task.TaskModels;
import java.util.List;
import java.util.UUID;

public interface TaskUseCase {
    List<TaskModels.View> list(TaskModels.Actor actor);

    TaskModels.Options options(TaskModels.Actor actor);

    TaskModels.Detail detail(TaskModels.Actor actor, UUID taskId);

    TaskModels.Status create(TaskModels.Actor actor, TaskModels.CreateCommand command);

    TaskModels.Status edit(TaskModels.Actor actor, UUID taskId, TaskModels.EditCommand command);

    TaskModels.Status progress(
            TaskModels.Actor actor, UUID taskId, TaskModels.ProgressCommand command);

    TaskModels.Commented comment(TaskModels.Actor actor, UUID taskId, String note);

    TaskModels.Status complete(TaskModels.Actor actor, UUID taskId, String note);
}
