package io.bordy.api;

public record MoveOperation(
        String placeAfterElementWithId,
        String placeBeforeElementWithId,
        String listId,
        String folderId
) {
}
