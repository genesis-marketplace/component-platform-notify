module.exports = async (inquirer, prevAns = {}) => await prompts(inquirer, prevAns);

const parseEntityList = (notificationEntities) => {
    if (!notificationEntities){
        return [];
    }
    try {
        return JSON.parse(notificationEntities);
    } catch (error) {
        console.error("Error parsing `notificationEntities` parameter as JSON:", error.message);
        return [];
    }
}

const prompts = async (inquirer, prevAns) => {
    const {
        emailGatewayEnabled = prevAns.emailGatewayEnabled,
        screenGatewayEnabled = prevAns.screenGatewayEnabled,
        teamsGatewayEnabled = prevAns.teamsGatewayEnabled,
        sendGridEnabled = prevAns.sendGridEnabled,
        apiKey = prevAns.apiKey,
        defaultSender = prevAns.defaultSender,
        smtpHost = prevAns.smtpHost,
        smtpPort = prevAns.smtpPort,
        smtpUser = prevAns.smtpUser,
        smtpPw = prevAns.smtpPw,
        smtpProtocol = prevAns.smtpProtocol,
        systemDefaultUserName = prevAns.systemDefaultUserName,
        systemDefaultEmail = prevAns.systemDefaultEmail,
        notificationEntities = prevAns.notificationEntities
    } = await inquirer.prompt([
        {
            name: 'screenGatewayEnabled',
            type: 'confirm',
            message: 'Enable Screen Gateway',
            default: prevAns.screenGatewayEnabled || true,
            when: prevAns.screenGatewayEnabled === undefined,
        },
        {
            name: 'emailGatewayEnabled',
            type: 'confirm',
            message: 'Enable Email Gateway',
            default: prevAns.emailGatewayEnabled || true,
            when: prevAns.emailGatewayEnabled === undefined,
        },
        {
            name: 'teamsGatewayEnabled',
            type: 'confirm',
            message: 'Enable Teams Gateway',
            default: prevAns.teamsGatewayEnabled || true,
            when: prevAns.teamsGatewayEnabled === undefined,
        },
        {
            name: 'sendGridEnabled',
            type: 'confirm',
            message: 'Enable SendGrid Gateway',
            default: prevAns.sendGridEnabled || false,
            when: prevAns.sendGridEnabled === undefined,
        },
        {
            name: 'apiKey',
            type: 'input',
            message: 'SendGrid API Key',
            default: prevAns.apiKey || "",
            when: ({sendGridEnabled}) => sendGridEnabled === true && prevAns.apiKey === undefined,
        },
        {
            name: 'defaultSender',
            type: 'input',
            message: 'SendGrid Default Sender',
            default: prevAns.defaultSender || "",
            when: ({sendGridEnabled}) => sendGridEnabled === true && prevAns.defaultSender === undefined,
        },
        {
            name: 'smtpHost',
            type: 'input',
            message: 'SMTP Host',
            default: prevAns.smtpHost || "localhost",
            when: ({emailGatewayEnabled}) => emailGatewayEnabled === true && prevAns.smtpHost === undefined,
        },
        {
            name: 'smtpPort',
            type: 'input',
            message: 'SMTP Port',
            default: prevAns.smtpPort || 587,
            when: ({emailGatewayEnabled}) => emailGatewayEnabled === true && prevAns.smtpPort === undefined,
        },
        {
            name: 'smtpUser',
            type: 'input',
            message: 'SMTP User',
            default: prevAns.smtpUser || "*",
            when: ({emailGatewayEnabled}) => emailGatewayEnabled === true && prevAns.smtpUser === undefined,
        },
        {
            name: 'smtpPw',
            type: 'input',
            message: 'SMTP Password',
            default: prevAns.smtpPw || "*",
            when: ({emailGatewayEnabled}) => emailGatewayEnabled === true && prevAns.smtpPw === undefined,
        },
        {
            name: 'smtpProtocol',
            type: 'input',
            message: 'SMTP Protocol',
            default: prevAns.smtpProtocol || "TransportStrategy.SMTP",
            when: ({emailGatewayEnabled}) => emailGatewayEnabled === true && prevAns.smtpProtocol === undefined,
        },
        {
            name: 'systemDefaultUserName',
            type: 'input',
            message: 'System Default User Name',
            default: prevAns.systemDefaultUserName || "*",
            when: ({emailGatewayEnabled}) => emailGatewayEnabled === true && prevAns.systemDefaultUserName === undefined,
        },
        {
            name: 'systemDefaultEmail',
            type: 'input',
            message: 'System Default Email',
            default: prevAns.systemDefaultEmail || "*",
            when: ({emailGatewayEnabled}) => emailGatewayEnabled === true && prevAns.systemDefaultEmail === undefined,
        },
        {
            name: 'notificationEntities',
            type: 'input',
            message: 'Enter the entity names that users can use to create notification rules (config in json format)',
            default: '[]',
            when: !prevAns.notificationEntities
        }

    ]);
    return {
        screenGatewayEnabled,
        emailGatewayEnabled,
        teamsGatewayEnabled,
        sendGridEnabled,
        apiKey,
        defaultSender,
        smtpHost,
        smtpPort,
        smtpUser,
        smtpPw,
        smtpProtocol,
        systemDefaultUserName,
        systemDefaultEmail,
        notificationEntities: parseEntityList(notificationEntities)
    };
};
