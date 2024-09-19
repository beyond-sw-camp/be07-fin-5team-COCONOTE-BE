package com.example.coconote.api.workspace.controller;

import com.example.coconote.api.workspace.service.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    @Autowired
    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }


}
