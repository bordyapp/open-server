package io.bordy.kanban.workspaces.invites.exceptions;

import javax.annotation.Nonnull;

public class WorkspaceInviteWorkspaceNotFoundException extends WorkspaceInviteException {

    public WorkspaceInviteWorkspaceNotFoundException(@Nonnull String message) {
        super(message);
    }

}
