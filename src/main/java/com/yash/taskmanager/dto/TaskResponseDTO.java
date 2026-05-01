package com.yash.taskmanager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yash.taskmanager.entity.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private Long projectId;
    private Long assignedToId;
    private String assignedToName;
    private Long createdById;
    private String createdByName;
}
