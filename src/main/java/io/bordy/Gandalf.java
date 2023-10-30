package io.bordy;

import io.bordy.kanban.workspaces.members.WorkspaceMembersService;
import io.bordy.kanban.workspaces.workspaces.WorkspacesService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class Gandalf {

    private final static Logger logger = Logger.getLogger(Gateway.class.getName());

    @Inject
    WorkspacesService workspacesService;

    @Inject
    WorkspaceMembersService workspaceMembersService;

    public boolean youShallNotPass(UUID workspaceId, String userId) {
        boolean isWorkspaceOwner  = workspacesService.isWorkspaceOwner(workspaceId, userId);
        boolean isWorkspaceMember = workspaceMembersService.isMemberOf(workspaceId, userId);
        boolean youShallNotPass = !isWorkspaceOwner && !isWorkspaceMember;

        logger.info(
                "User: " + userId + " requested access to workspace: " + workspaceId +
                        " isWorkspaceOwner: " + isWorkspaceOwner + " isWorkspaceMember: " + isWorkspaceMember +
                        "\nyouShallNotPass: " + youShallNotPass
        );
        return youShallNotPass;
    }

}
