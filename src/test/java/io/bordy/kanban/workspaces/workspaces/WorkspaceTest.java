package io.bordy.kanban.workspaces.workspaces;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

@QuarkusTest
@TestTransaction
public class WorkspaceTest {

    @Test
    @DisplayName("Workspace must be created exactly once and found by it's id")
    public void createExactlyOneAndFind() {
        var expectedWorkspace = new Workspace(
                UUID.randomUUID(),
                "Workspace to test",
                "",
                UUID.randomUUID().toString(),
                new Date(),
                new Date()
        );

        Workspace.persist(expectedWorkspace);
        var persistedWorkspace = Workspace.findById(expectedWorkspace.getId());

        Assertions.assertEquals(
                1, Workspace.count(),
                "Only 1 workspace must be created"
        );
        Assertions.assertEquals(
                expectedWorkspace, persistedWorkspace,
                "Created workspace and persisted workspace must be equals"
        );
    }

    public static Stream<Arguments> notPresentIds() {
        return Stream.of(
                Arguments.of(-1),
                Arguments.of(1),
                Arguments.of(1.0D),
                Arguments.of(1.0F),
                Arguments.of(' '),
                Arguments.of(""),
                Arguments.of("uuid")
        );
    }

    @ParameterizedTest
    @MethodSource("notPresentIds")
    @DisplayName("Return null instead of workspace when id is not belongs to workspace or invalid")
    public void returnNullWhenIdNotBelongsToWorkspaceOrInvalid(Object notPresentId) {
        Assertions.assertNull(
                Workspace.findById(notPresentId),
                String.format("Must return with given id - %s", notPresentId)
        );
    }

}
