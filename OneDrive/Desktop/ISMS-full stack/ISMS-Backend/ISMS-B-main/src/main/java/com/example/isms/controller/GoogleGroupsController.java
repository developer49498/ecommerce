package com.example.isms.controller;


import com.example.isms.model.Group;
import com.example.isms.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/groups")
public class GoogleGroupsController {

    @Autowired
    private GroupService groupService;

    @PostMapping("/upload")
    public String uploadGroup(@RequestParam String groupName,
                              @RequestParam String groupMail) throws ExecutionException, InterruptedException {
        Group group = new Group(groupName, groupMail);
        return groupService.uploadSingleGroup(group);
    }

    @PostMapping("/bulk-upload")
    public String bulkUpload(@RequestBody List<Group> groups) throws ExecutionException, InterruptedException {
        return groupService.uploadBulkGroups(groups);
    }

    @GetMapping("/{groupName}")
    public Group getGroup(@PathVariable String groupName) throws ExecutionException, InterruptedException {
        Group group = groupService.getGroupByName(groupName);
        if (group != null) {
            return group;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }
    }

}

