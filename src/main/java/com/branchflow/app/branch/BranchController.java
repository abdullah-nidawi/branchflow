package com.branchflow.app.branch;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {
    private final BranchService branchService;
    public BranchController(BranchService branchService) { this.branchService = branchService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse create(@Valid @RequestBody CreateBranchRequest request) { return branchService.create(request); }

    @GetMapping
    public List<BranchResponse> findAll(@RequestParam(required = false) Long companyId) { return branchService.findAll(companyId); }

    @GetMapping("/{id}")
    public BranchResponse findById(@PathVariable Long id) { return branchService.findById(id); }
}
