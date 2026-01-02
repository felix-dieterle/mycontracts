package de.flexis.mycontracts.controller.dto;

import java.util.List;

public record ContractOptimizationResponse(
    List<String> suggestions,
    List<String> risks,
    List<String> improvements,
    String summary
) {}
