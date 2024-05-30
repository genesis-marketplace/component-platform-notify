import { UpdateType } from '../../../../../notify.types';

export const RuleDialogMode = {
  CREATE: 'create',
  EDIT: 'edit',
} as const;

export type RuleDialogParams =
  | {
      mode: typeof RuleDialogMode.CREATE;
    }
  | {
      mode: typeof RuleDialogMode.EDIT;
      data: Rule;
    };

export type Rule = {
  DYNAMIC_RULE_ID?: string;
  RULE_NAME: string;
  RULE_DESCRIPTION: string;
  RULE_TABLE: string;
  TABLE_OPERATIONS: UpdateType[];
  RULE_EXPRESSION?: any;
  RAW_RULE_EXPRESSION?: string;
  RESULT_EXPRESSION?: {
    ASSIGNMENTS: Array<any>;
  };
};
