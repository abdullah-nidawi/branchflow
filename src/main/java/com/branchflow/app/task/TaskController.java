package com.branchflow.app.task;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
public class TaskController {
    private final TaskService service;
    public TaskController(TaskService service) { this.service = service; }

    @PostMapping("/api/branches/{branchId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@PathVariable Long branchId, @Valid @RequestBody CreateTaskRequest request) {
        return service.create(branchId, request);
    }

    @GetMapping("/api/branches/{branchId}/tasks")
    public Page<TaskResponse> search(@PathVariable Long branchId,
                                     @RequestParam(required = false) TaskStatus status,
                                     @RequestParam(required = false) TaskPriority priority,
                                     @PageableDefault(size = 20, sort = "createdAt", direction = DESC) Pageable pageable) {
        return service.search(branchId, status, priority, pageable);
    }

    @PatchMapping("/api/tasks/{taskId}/status")
    public TaskResponse updateStatus(@PathVariable Long taskId, @Valid @RequestBody UpdateTaskStatusRequest request) {
        return service.updateStatus(taskId, request.status());
    }
}
