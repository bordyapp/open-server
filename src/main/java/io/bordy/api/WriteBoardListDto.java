package io.bordy.api;

import io.bordy.kanban.lists.BoardListResource;

import java.util.List;

public record WriteBoardListDto(
        String name,
        List<BoardListResource> resources
) {
}
