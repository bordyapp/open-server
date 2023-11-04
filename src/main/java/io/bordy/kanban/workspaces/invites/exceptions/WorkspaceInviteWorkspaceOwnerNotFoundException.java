package io.bordy.kanban.workspaces.invites.exceptions;

import javax.annotation.Nonnull;

public class WorkspaceInviteWorkspaceOwnerNotFoundException extends WorkspaceInviteException {

    public WorkspaceInviteWorkspaceOwnerNotFoundException(@Nonnull String message) {
        super(message);
    }

}
