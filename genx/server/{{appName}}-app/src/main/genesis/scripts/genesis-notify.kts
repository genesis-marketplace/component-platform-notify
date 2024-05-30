notify {
    gateways {
        {{#if screenGatewayEnabled}}
        screen("Screen")

        {{/if}}
        {{#if emailGatewayEnabled}}
        email("Email") {
            smtpHost =  "{{smtpHost}}"
            smtpPort = {{smtpPort}}
            smtpUser = "{{smtpUser}}"
            smtpPw = "{{smtpPw}}"
            smtpProtocol = {{smtpProtocol}}
            systemDefaultUserName = "{{systemDefaultUserName}}"
            systemDefaultEmail = "{{systemDefaultEmail}}"
            sendFromUserAddress = false
        }

        {{/if}}
        {{#if teamsGatewayEnabled}}
        teams("Teams")
        {{/if}}
        {{#if sendGridEnabled}}
        sendGrid("SendGrid") {
            apiKey = "{{apiKey}}"
            defaultSender = "{{defaultSender}}"
        }
        {{/if}}
    }
}
