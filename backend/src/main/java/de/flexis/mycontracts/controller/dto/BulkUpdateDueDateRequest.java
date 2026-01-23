package de.flexis.mycontracts.controller.dto;

import java.time.Instant;
import java.util.List;

public record BulkUpdateDueDateRequest(List<Long> fileIds, Instant dueDate) {}
