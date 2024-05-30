import { UpdateType } from '../../../../../notify.types';

export const TemplateDialogMode = {
  CREATE: 'create',
  EDIT: 'edit',
} as const;

export type TemplateDialogParams =
  | {
      mode: typeof TemplateDialogMode.CREATE;
    }
  | {
      mode: typeof TemplateDialogMode.EDIT;
      data: Template;
    };

export type Template = {
  DYNAMIC_RULE_ID?: string;
  RULE_NAME: string;
  RULE_DESCRIPTION: string;
  RULE_TABLE: string;
  RULE_EXPRESSION?: any;
  TABLE_OPERATIONS: UpdateType[];
  RAW_RULE_EXPRESSION?: string;
  RESULT_EXPRESSION?: {
    ASSIGNMENTS: Array<any>;
  };
  PARAMETER_DETAILS: any;
};

export type ParameterBuilderEntity = {
  UUID?: string;
  PARAM_NAME: string;
  PARAM_LABEL: string;
  PARAM_TYPE: string;
  PARAM_SOURCE_TYPE: string;
  PARAM_SOURCE: string;
};
