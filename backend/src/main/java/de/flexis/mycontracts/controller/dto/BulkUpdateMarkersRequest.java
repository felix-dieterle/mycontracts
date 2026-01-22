package de.flexis.mycontracts.controller.dto;

import java.util.List;

public record BulkUpdateMarkersRequest(List<Long> fileIds, List<String> markers) {}
