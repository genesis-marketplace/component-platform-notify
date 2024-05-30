export interface Alert {
  ALERT_ID: string;
  ALERT_STATUS: AlertStatus;
  CREATED_AT: number;
  HEADER: string;
  MESSAGE: string;
  USER_NAME: string;
  EXPIRY: number;
  NOTIFY_SEVERITY: string;
  TABLE_ENTITY_ID: string;
  TOPIC: string;
}

export enum AlertStatus {
  NEW = 'NEW',
  DISMISSED = 'DISMISSED',
}

export interface Rule {
  ID: string;
  NAME: string;
  DESCRIPTION: string;
  RULE_EXPRESSION: string;
  RULE_EXECUTION_STRATEGY: string;
  RULE_STATUS: string;
  RULE_TABLE: string;
  PROCESS_NAME: string;
  TABLE_OPERATION: string;
}

export interface RuleParameterEntity {
  DYNAMIC_RULE_ID;
  PARAM_LABEL;
  PARAM_NAME;
  PARAM_OPERATOR;
  PARAM_SOURCE;
  PARAM_SOURCE_TYPE;
  PARAM_TYPE;
}

export interface RuleTemplate {
  ID: string;
  NAME: string;
  DESCRIPTION: string;
  USER_NAME: string;
  RULE_TABLE: string;
  RULE_STATUS: string;
  RULE_EXPRESSION: string;
  PROCESS_NAME: string;
  MESSAGE_TYPE: string;
  RESULT_EXPRESSION: string;
  TABLE_OPERATION: string;
  IS_TEMPLATE: boolean;
  RULE_EXECUTION_STRATEGY: string;
  RULE_TYPE: string;
}

export interface NotificationRuleTemplateReply {
  DYNAMIC_RULE_ID: string;
  RULE_NAME: string;
  RULE_DESCRIPTION: string;
  RULE_TABLE: string;
  RULE_EXECUTION_STRATEGY: string;
  RULE_EXPRESSION: {
    CONDITIONS: Array<any>;
  };
  RAW_RULE_EXPRESSION: string;
  RESULT_EXPRESSION: {
    ASSIGNMENTS: Array<any>;
  };
  PARAMETERS: any;
}

export enum RuleStatus {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED',
}

export enum InboxTab {
  AlertsNew,
  AlertHistory,
  MyAlerts,
  Subscribe,
}
