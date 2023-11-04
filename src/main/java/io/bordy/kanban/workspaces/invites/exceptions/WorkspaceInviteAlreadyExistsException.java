package io.bordy.kanban.workspaces.invites.exceptions;

import javax.annotation.Nonnull;

public class WorkspaceInviteAlreadyExistsException extends WorkspaceInviteException {

    public WorkspaceInviteAlreadyExistsException(@Nonnull String message) {
        super(message);
    }

}
