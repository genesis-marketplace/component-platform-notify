// #region Enums
export enum LogicalOperator {
  EQUALS = 'EQUALS',
  NOT_EQUALS = 'NOT_EQUALS',
  GREATER_THAN = 'GREATER_THAN',
  LESS_THAN = 'LESS_THAN',
  NULL = 'NULL',
  NOT_NULL = 'NOT_NULL',
  BLANK = 'BLANK',
  NOT_BLANK = 'NOT_BLANK',
}

export const nullAndBlankLogicalOperatorValues = [
  LogicalOperator.NULL,
  LogicalOperator.NOT_NULL,
  LogicalOperator.BLANK,
  LogicalOperator.NOT_BLANK,
];

export enum RightCriteria {
  VALUE = 'VALUE',
  USER_ENTRY = 'USER_ENTRY',
}

export enum ParamType {
  // FIELD = 'FIELD',
  STRING = 'STRING',
  // BOOLEAN = 'BOOLEAN',
  NUMBER = 'NUMBER',
}

export enum ParamSourceType {
  USER_TEXT = 'USER_TEXT',
  DEFINED_GROUP = 'DEFINED_GROUP',
  REQ_REP = 'REQ_REP',
}

export enum Severity {
  INFORMATION = 'INFORMATION',
  CRITICAL = 'CRITICAL',
  SERIOUS = 'SERIOUS',
  WARNING = 'WARNING',
}

export enum ExpressionType {
  FIELD = 'FIELD',
  NUMBER = 'NUMBER',
  STRING = 'STRING',
  PLACEHOLDER = 'PLACEHOLDER',
}

export enum Assignment {
  HEADER = 'HEADER',
  BODY = 'BODY',
  NOTIFY_SEVERITY = 'NOTIFY_SEVERITY',
  TOPIC = 'TOPIC',
}

export enum UpdateType {
  INSERT = 'INSERT',
  MODIFY = 'MODIFY',
  DELETE = 'DELETE',
}

export const defaultUpdateType = [UpdateType.INSERT];

export enum EmptyValue {
  BLANK = '',
  NULL = 'NULL',
}
// #endregion

// #region Types
export type ConditionBuilderEntity = {
  ID: string;
  LEFT_VALUE: any;
  LOGICAL_OPERATOR: string;
  RIGHT_CRITERIA: string;
  RIGHT_VALUE: string;
};
// #endregion
