package io.bordy;

import io.bordy.workspaces.WorkspaceMembersRepository;
import io.bordy.kanban.workspaces.workspaces.Workspace;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;
import java.util.logging.Logger;

@ApplicationScoped
public class Gandalf {

    private final static Logger logger = Logger.getLogger(Gateway.class.getName());

    @Inject
    WorkspaceMembersRepository workspaceMembersRepository;

    public boolean youShallNotPass(UUID workspaceId, String userId) {
        boolean isWorkspaceOwner = Workspace.count(
                "_id = ?1 and ownerId = ?2",
                workspaceId,
                userId
        ) > 0;
        boolean isWorkspaceMember = workspaceMembersRepository.count(
                "workspaceId = ?1 and userId = ?2",
                workspaceId,
                userId
        ) > 0;
        boolean youShallNotPass = !isWorkspaceOwner && !isWorkspaceMember;

        logger.info(
                "User: " + userId + " requested access to workspace: " + workspaceId +
                        " isWorkspaceOwner: " + isWorkspaceOwner + " isWorkspaceMember: " + isWorkspaceMember +
                        "\nyouShallNotPass: " + youShallNotPass
        );
        return youShallNotPass;
    }

}
