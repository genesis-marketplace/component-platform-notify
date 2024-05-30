import { showNotification } from '@genesislcap/foundation-notifications';
import { UUID } from '@genesislcap/foundation-utils';
import {
  ConditionBuilderEntity,
  EmptyValue,
  ExpressionType,
  LogicalOperator,
  ParamSourceType,
  ParamType,
  RightCriteria,
  nullAndBlankLogicalOperatorValues,
} from '../../notify.types';
import { logger } from '../../utils';
import { ParameterBuilderEntity } from './components/templates/template-dialog/template-dialog.types';

export class UUIDGenerator {
  @UUID private static uuid: UUID;
  static createUUID = (): string => this.uuid.createId();
}

export const isEmpty = (value) =>
  value == null || (typeof value === 'string' && value.trim().length === 0);

// #region Rules and Templates
class ConditionUtils {
  static transformLogicalOperator(logicalOperator) {
    switch (logicalOperator) {
      case LogicalOperator.NULL:
      case LogicalOperator.BLANK:
        return LogicalOperator.EQUALS;
      case LogicalOperator.NOT_NULL:
      case LogicalOperator.NOT_BLANK:
        return LogicalOperator.NOT_EQUALS;
      default:
        return logicalOperator;
    }
  }

  static buildLogicalOperator(condition) {
    if (condition.RIGHT.TYPE === EmptyValue.NULL) {
      return condition.OPERATION === LogicalOperator.EQUALS
        ? LogicalOperator.NULL
        : LogicalOperator.NOT_NULL;
    } else if (condition.RIGHT.VALUE === EmptyValue.BLANK) {
      return condition.OPERATION === LogicalOperator.EQUALS
        ? LogicalOperator.BLANK
        : LogicalOperator.NOT_BLANK;
    } else {
      return condition.OPERATION;
    }
  }

  static getRightExpression(condition: ConditionBuilderEntity, parameters?) {
    // 1. Check LOGICAL_OPERATOR for null values
    if (
      condition.LOGICAL_OPERATOR === LogicalOperator.NULL ||
      condition.LOGICAL_OPERATOR === LogicalOperator.NOT_NULL
    ) {
      return {
        TYPE: EmptyValue.NULL,
      };
    }

    // 2. Check LOGICAL_OPERATOR for blank values
    if (
      condition.LOGICAL_OPERATOR === LogicalOperator.BLANK ||
      condition.LOGICAL_OPERATOR === LogicalOperator.NOT_BLANK
    ) {
      return {
        TYPE: 'STRING',
        VALUE: ConditionUtils.handleNullOrBlankLogicalOperator(condition.LOGICAL_OPERATOR),
      };
    }

    // 3. Handle by RIGHT_CRITERIA
    switch (condition.RIGHT_CRITERIA) {
      case 'USER_ENTRY':
        return {
          TYPE: 'PLACEHOLDER',
          KEY: parameters.find((parameter) => parameter.UUID === condition.RIGHT_VALUE).PARAM_NAME,
        };
      case 'FIELD': // Only for Rules
        return {
          TYPE: 'FIELD',
          NAME: condition.RIGHT_VALUE,
        };
      case 'VALUE':
        switch (condition.LEFT_VALUE.FIELD_TYPE) {
          case 'BOOLEAN':
            return {
              TYPE: 'BOOLEAN',
              VALUE: condition.RIGHT_VALUE,
            };
          case 'STRING':
          case 'ENUM':
            return {
              TYPE: 'STRING',
              VALUE: condition.RIGHT_VALUE,
            };
          case 'INT':
          case 'SHORT':
          case 'DOUBLE':
          case 'LONG':
          case 'BIGDECIMAL':
            return {
              TYPE: 'NUMBER',
              VALUE: condition.RIGHT_VALUE,
            };
          default:
            return {
              TYPE: 'FIELD',
              NAME: condition.RIGHT_VALUE,
            };
        }
      default:
        logger.error(`Unexpected RIGHT_CRITERIA: ${condition.RIGHT_CRITERIA}`);
        return {};
    }
  }

  static handleNullOrBlankLogicalOperator(logicalOperator) {
    switch (logicalOperator) {
      case LogicalOperator.NULL:
      case LogicalOperator.NOT_NULL:
        return EmptyValue.NULL;
      case LogicalOperator.BLANK:
      case LogicalOperator.NOT_BLANK:
        return EmptyValue.BLANK;
      default:
        logger.error(`Unexpected LogicalOperator: ${logicalOperator}`);
        break;
    }
  }
}

export class DynamicRuleUtils {
  static createEmptyCondition = (): ConditionBuilderEntity => ({
    ID: null,
    LEFT_VALUE: null,
    LOGICAL_OPERATOR: LogicalOperator.EQUALS,
    RIGHT_CRITERIA: RightCriteria.VALUE,
    RIGHT_VALUE: null,
  });

  static createAssignment = (field: string, value: string) => ({
    FIELD: {
      TYPE: 'FIELD',
      NAME: field,
    },
    VALUE: {
      TYPE: 'STRING',
      VALUE: value,
    },
  });

  static getAssignmentValue = (assignments: any[], field: string): string =>
    assignments.find((assignment) => assignment.FIELD.NAME === field).VALUE.VALUE;

  static createConditions(conditions: ConditionBuilderEntity[], parameters?): any {
    return conditions.map((condition) => ({
      LEFT: {
        TYPE: 'FIELD', // TODO: Use ParamType
        NAME: condition.LEFT_VALUE.FIELD_NAME,
      },
      OPERATION: ConditionUtils.transformLogicalOperator(condition.LOGICAL_OPERATOR),
      RIGHT: ConditionUtils.getRightExpression(condition, parameters),
    }));
  }
}

export class TemplateUtils {
  static createEmptyParameter = (): ParameterBuilderEntity => ({
    UUID: UUIDGenerator.createUUID(),
    PARAM_NAME: null,
    PARAM_LABEL: null,
    PARAM_TYPE: ParamType.STRING,
    PARAM_SOURCE_TYPE: ParamSourceType.USER_TEXT,
    PARAM_SOURCE: null,
  });

  static getParameters(parameters) {
    if (!parameters) {
      return [];
    }

    return Object.keys(parameters).map((key) => ({
      UUID: UUIDGenerator.createUUID(),
      PARAM_NAME: parameters[key].PARAM_NAME,
      PARAM_LABEL: parameters[key].PARAM_LABEL,
      PARAM_TYPE: parameters[key].PARAM_TYPE,
      PARAM_SOURCE_TYPE: parameters[key].PARAM_SOURCE_TYPE,
      PARAM_SOURCE: parameters[key].PARAM_SOURCE,
    }));
  }

  private static buildRightValue(logicalOperator, condition, parameters?) {
    // Is Null or Blank
    if (nullAndBlankLogicalOperatorValues.includes(logicalOperator)) {
      return EmptyValue.BLANK;
    }

    // Find parameter name by key
    if (condition.RIGHT.TYPE === ExpressionType.PLACEHOLDER) {
      return parameters.find((parameter) => parameter.PARAM_NAME === condition.RIGHT.KEY).UUID;
    }

    // If value is null, then NAME is filled
    return condition.RIGHT.VALUE ?? condition.RIGHT.NAME;
  }

  static getConditions(conditions, parameters?) {
    return conditions.map((condition) => {
      const logicalOperator = ConditionUtils.buildLogicalOperator(condition);
      return {
        ID: UUIDGenerator.createUUID(),
        LEFT_VALUE: condition.LEFT.VALUE ?? condition.LEFT.NAME,
        LOGICAL_OPERATOR: logicalOperator,
        RIGHT_CRITERIA:
          condition.RIGHT.TYPE === ExpressionType.PLACEHOLDER
            ? RightCriteria.USER_ENTRY
            : RightCriteria.VALUE,
        RIGHT_VALUE: this.buildRightValue(logicalOperator, condition, parameters),
      };
    });
  }

  static createParameterDetails(parameters) {
    if (!parameters || parameters.length === 0) {
      return {};
    }

    const map = {};

    parameters.forEach((parameter) => {
      map[parameter.PARAM_NAME] = {
        PARAM_LABEL: parameter.PARAM_LABEL,
        PARAM_TYPE: parameter.PARAM_TYPE,
        PARAM_SOURCE_TYPE: parameter.PARAM_SOURCE_TYPE,
        PARAM_SOURCE: parameter.PARAM_SOURCE ?? '',
        PARAM_OPERATOR: 'EQUALS', // TODO: Remove
      };
    });

    return map;
  }
}

export class RuleUtils {
  private static buildRightValue(logicalOperator, condition) {
    // Is Null or Blank
    if (nullAndBlankLogicalOperatorValues.includes(logicalOperator)) {
      return EmptyValue.BLANK;
    }

    // Get KEY
    if (condition.RIGHT.TYPE === ExpressionType.PLACEHOLDER) {
      return condition.RIGHT.KEY;
    }

    // If value is null, then NAME is filled
    return condition.RIGHT.VALUE ?? condition.RIGHT.NAME;
  }

  static getConditions(conditions) {
    return conditions.map((condition) => {
      const logicalOperator = ConditionUtils.buildLogicalOperator(condition);
      return {
        ID: UUIDGenerator.createUUID(),
        LEFT_VALUE: condition.LEFT.VALUE ?? condition.LEFT.NAME,
        LOGICAL_OPERATOR: logicalOperator,
        RIGHT_CRITERIA: condition.RIGHT.TYPE === 'FIELD' ? 'FIELD' : 'VALUE',
        RIGHT_VALUE: this.buildRightValue(logicalOperator, condition),
      };
    });
  }
}
// #endregion

// #region Screen
export function showNotificationError(error) {
  showNotification(
    {
      title: error[0]?.CODE,
      body: error[0]?.TEXT,
      config: {
        snackbar: {
          type: 'error',
        },
      },
    },
    'rapid',
  );
}
// #endregion
