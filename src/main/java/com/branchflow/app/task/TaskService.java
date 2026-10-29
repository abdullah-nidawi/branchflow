package com.branchflow.app.task;

import com.branchflow.app.branch.Branch;
import com.branchflow.app.branch.BranchRepository;
import com.branchflow.app.common.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private final TaskRepository tasks;
    private final BranchRepository branches;
    public TaskService(TaskRepository tasks, BranchRepository branches) { this.tasks = tasks; this.branches = branches; }

    @Transactional
    public TaskResponse create(Long branchId, CreateTaskRequest request) {
        Branch branch = branches.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
        Task task = new Task(branch, request.title().trim(), request.description(), request.priority(), request.dueAt());
        return TaskResponse.from(tasks.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> search(Long branchId, TaskStatus status, TaskPriority priority, Pageable pageable) {
        if (!branches.existsById(branchId)) throw new NotFoundException("Branch not found: " + branchId);
        return tasks.search(branchId, status, priority, pageable).map(TaskResponse::from);
    }

    @Transactional
    public TaskResponse updateStatus(Long taskId, TaskStatus status) {
        Task task = tasks.findById(taskId).orElseThrow(() -> new NotFoundException("Task not found: " + taskId));
        task.changeStatus(status);
        return TaskResponse.from(task);
    }
}
