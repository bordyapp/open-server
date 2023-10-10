package io.bordy.mail;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/*
    https://cloud.google.com/compute/docs/tutorials/sending-mail
    https://support.google.com/a/answer/166852#zippy=%2Cconvert-to-a-paid-account%2Climits-increase-for-paid-accounts
    https://support.google.com/a/answer/2956491
    https://quarkus.io/guides/mailer#message-body-based-on-qute-templates
    https://quarkus.io/guides/mailer-reference#popular
 */
@ApplicationScoped
public class Postman {

    @Inject
    Mailer mailer;

    @Inject
    @Location("workspace-invitation-email-template")
    Template workspaceInvitationEmailTemplate;

    /**
     *
     * @param toEmailAddress where to send
     * @throws java.util.concurrent.CompletionException in case of SMTP error.
     */
    @Blocking
    public void sendInvitationEmail(
            String toEmailAddress,
            String workspaceOwnerName,
            String workspaceOwnerPhoto,
            String workspaceName,
            String workspaceInvite
    ) throws java.util.concurrent.CompletionException {
        mailer.send(
                Mail.withHtml(
                        toEmailAddress,
                        "Workspace invitation",
                        workspaceInvitationEmailTemplate
                                .data("user_name", workspaceOwnerName)
                                .data("user_photo", workspaceOwnerPhoto)
                                .data("workspace_name", workspaceName)
                                .data("invitation_url", workspaceInvite)
                                .render()
                ).setFrom("Bordy <app@bordy.io>").addHeader("Content-Type", "text/html; charset=utf-8")
        );
    }

}
