package de.flexis.mycontracts.controller.dto;

import java.util.List;

public record BulkUpdateNoteRequest(List<Long> fileIds, String note) {}
