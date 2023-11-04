package io.bordy.kanban.workspaces.invites.exceptions;

import javax.annotation.Nonnull;

public abstract class WorkspaceInviteException extends Exception {

    public WorkspaceInviteException(@Nonnull String message) {
        super(message);
    }

}
