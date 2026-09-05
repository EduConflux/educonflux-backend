package com.projects.EduConflux.search.dto;

import lombok.Getter;

@Getter
public class SearchResult {

    private String type;
    private Long id;
    private String title;
    private String description;

    public SearchResult() {
    }

    public SearchResult(
            String type,
            Long id,
            String title,
            String description
    ) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.description = description;
    }

}