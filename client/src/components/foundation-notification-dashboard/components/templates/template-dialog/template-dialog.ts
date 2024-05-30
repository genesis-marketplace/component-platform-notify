import type { Modal } from '@genesislcap/rapid-design-system';
import { customElement, GenesisElement, observable, volatile } from '@genesislcap/web-core';
import {
  Assignment,
  ConditionBuilderEntity,
  defaultUpdateType,
  Severity,
  UpdateType,
} from '../../../../../notify.types';
import { NotifyService } from '../../../../../services/notify.service';
import { Field, SystemService } from '../../../../../services/system.service';
import { TemplateService } from '../../../../../services/template.service';
import { humanize, logger } from '../../../../../utils';
import {
  DynamicRuleUtils,
  showNotificationError,
  TemplateUtils,
  isEmpty,
} from '../../../notification-dashboard.utils';
import { DynamicRuleDialogStyles } from '../../../styles/dynamic-rule.styles';
import { TemplateDialogTemplate } from './template-dialog.template';
import { ParameterBuilderEntity, Template, TemplateDialogMode } from './template-dialog.types';

@customElement({
  name: 'template-dialog',
  template: TemplateDialogTemplate,
  styles: DynamicRuleDialogStyles,
})
export class TemplateDialog extends GenesisElement {
  @TemplateService templateService: TemplateService;
  @SystemService systemService: SystemService;
  @NotifyService notifyService: NotifyService;

  dialog: Modal;
  @observable templateDialogMode: (typeof TemplateDialogMode)[keyof typeof TemplateDialogMode] =
    null;

  @observable templateId: string;
  @observable name: string;
  @observable description: string;
  @observable header;
  @observable message;
  @observable resource: string = ''; // TODO: Tables + Views
  @observable topic;
  @observable severity;
  @observable updateType: UpdateType[] = defaultUpdateType;

  @observable parameters: Array<ParameterBuilderEntity> = [];
  @observable conditions: Array<ConditionBuilderEntity> = [];

  @observable resources: Array<string> = [];
  @observable fields: Array<Field> = [];
  @observable topics: Array<string> = [];

  private requiredFields = () => [this.name, this.description, this.header, this.message];

  public async openDialog(params: any) {
    this.topics = await this.notifyService.getNotifyRouteTopics();
    this.resources = await this.systemService.getResources(); // TODO: Tables + Views
    this.templateDialogMode = params.mode;

    switch (params.mode) {
      case TemplateDialogMode.CREATE:
        await this.createTemplate();
        break;
      case TemplateDialogMode.EDIT:
        await this.editTemplate(params.data);
        break;
      default:
        logger.error('Error on TemplateDialogMode');
        break;
    }

    this.dialog.show();
  }

  close() {
    // Objects
    this.templateId = null;
    this.name = null;
    this.description = null;
    this.header = null;
    this.message = null;
    this.resource = null;
    this.topic = null;
    this.severity = null;
    this.templateDialogMode = null;

    // Arrays
    this.resources = [];
    this.topics = [];
    this.conditions = [];
    this.parameters = [];
    this.updateType = [];

    this.dialog.close();
    this.$emit('close');
  }

  private async createTemplate() {
    this.conditions.push(DynamicRuleUtils.createEmptyCondition());
    this.resource = this.resources[0];
    this.fields = await this.systemService.getFields(this.resource);
    this.topic = this.topics[0];
    this.severity = Severity.INFORMATION;
    this.updateType = defaultUpdateType;
  }

  private async editTemplate(data) {
    const template = data;
    this.templateId = template.DYNAMIC_RULE_ID;
    this.name = template.RULE_NAME;
    this.description = template.RULE_DESCRIPTION;
    this.resource = template.RULE_TABLE;
    this.updateType = template.TABLE_OPERATIONS;
    this.fields = await this.systemService.getFields(this.resource);
    this.parameters = TemplateUtils.getParameters(template.PARAMETERS);
    this.conditions = TemplateUtils.getConditions(
      template.RULE_EXPRESSION.CONDITIONS,
      this.parameters,
    );
    this.attributeAssignments(template.RESULT_EXPRESSION?.ASSIGNMENTS);
  }

  private attributeAssignments(assignments) {
    if (!assignments || !assignments.length) {
      return;
    }

    this.header = DynamicRuleUtils.getAssignmentValue(assignments, Assignment.HEADER);
    this.message = DynamicRuleUtils.getAssignmentValue(assignments, Assignment.BODY);
    this.severity = DynamicRuleUtils.getAssignmentValue(
      assignments,
      Assignment.NOTIFY_SEVERITY,
    )?.toUpperCase();
    this.topic = DynamicRuleUtils.getAssignmentValue(assignments, Assignment.TOPIC);
  }

  get templateDialogTitle(): string {
    return `${this.templateDialogMode === TemplateDialogMode.CREATE ? 'Create' : 'Edit'} Template`;
  }

  submit() {
    if (this.requiredFields().some((value) => isEmpty(value))) {
      return;
    }

    const template: Template = this.createTemplateObject();

    switch (this.templateDialogMode) {
      case TemplateDialogMode.CREATE:
        this.templateService
          .createRuleTemplate(template)
          .then((response) => this.validateResponse(response));
        break;
      case TemplateDialogMode.EDIT:
        this.templateService
          .editRuleTemplate(template)
          .then((response) => this.validateResponse(response));
        break;
      default:
        logger.error('Error on TemplateDialogMode');
        break;
    }
  }

  private createTemplateObject(): Template {
    return {
      ...(this.templateDialogMode === TemplateDialogMode.EDIT && {
        DYNAMIC_RULE_ID: this.templateId,
      }),
      RULE_NAME: this.name,
      RULE_DESCRIPTION: this.description,
      RULE_TABLE: this.resource,
      TABLE_OPERATIONS: this.updateType,
      RULE_EXPRESSION: {
        CONDITIONS: DynamicRuleUtils.createConditions(this.conditions, this.parameters),
      },
      RAW_RULE_EXPRESSION: null,
      RESULT_EXPRESSION: {
        ASSIGNMENTS: [
          DynamicRuleUtils.createAssignment(Assignment.TOPIC, this.topic),
          DynamicRuleUtils.createAssignment(Assignment.HEADER, this.header),
          DynamicRuleUtils.createAssignment(Assignment.BODY, this.message),
          DynamicRuleUtils.createAssignment(Assignment.NOTIFY_SEVERITY, humanize(this.severity)),
        ],
      },
      PARAMETER_DETAILS: TemplateUtils.createParameterDetails(this.parameters),
    };
  }

  validateResponse(response) {
    if (response.MESSAGE_TYPE === 'EVENT_ACK') {
      this.close();
      return;
    }

    showNotificationError(response.ERROR);
  }

  async resourceChanged() {
    this.conditions = [DynamicRuleUtils.createEmptyCondition()];
    this.fields = await this.systemService.getFields(this.resource);
  }

  // #region Condition
  newCondition() {
    this.conditions.push(DynamicRuleUtils.createEmptyCondition());
  }

  editCondition(editedCondition: ConditionBuilderEntity) {}

  deleteCondition(deletedCondition: ConditionBuilderEntity) {
    if (this.conditions.length <= 1) {
      return;
    }

    this.conditions = this.conditions.filter((condition) => condition !== deletedCondition);
  }
  // #endregion

  // #region Parameter
  newParameter() {
    this.parameters = [...this.parameters, TemplateUtils.createEmptyParameter()];
  }

  editParameter(editedParameter: ParameterBuilderEntity) {
    this.parameters = [...this.parameters];
  }

  deleteParameter(deletedParameter: ParameterBuilderEntity) {
    if (this.conditions.find((condition) => condition.RIGHT_VALUE === deletedParameter.UUID)) {
      return;
    }

    this.parameters = this.parameters.filter(
      (parameter) => parameter.UUID !== deletedParameter.UUID,
    );
  }
  // #endregion

  @volatile
  public get validateRequiredFields(): boolean {
    return this.requiredFields().some((value) => isEmpty(value));
  }
}
