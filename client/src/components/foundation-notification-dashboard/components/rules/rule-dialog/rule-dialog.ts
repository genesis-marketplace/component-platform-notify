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
import { RuleService } from '../../../../../services/rule.service';
import { Field, SystemService } from '../../../../../services/system.service';
import { humanize } from '../../../../../utils';
import {
  DynamicRuleUtils,
  RuleUtils,
  showNotificationError,
  isEmpty,
} from '../../../notification-dashboard.utils';
import { DynamicRuleDialogStyles } from '../../../styles/dynamic-rule.styles';
import { RuleDialogTemplate } from './rule-dialog.template';
import { Rule, RuleDialogMode, RuleDialogParams } from './rule-dialog.types';

@customElement({
  name: 'rule-dialog',
  template: RuleDialogTemplate,
  styles: DynamicRuleDialogStyles,
})
export class RuleDialog extends GenesisElement {
  @RuleService ruleService: RuleService;
  @SystemService systemService: SystemService;
  @NotifyService notifyService: NotifyService;

  dialog: Modal;
  @observable ruleDialogMode: (typeof RuleDialogMode)[keyof typeof RuleDialogMode];

  private ruleId: string;
  @observable name: string;
  @observable description: string;
  @observable resource: string = ''; // TODO: Tables + Views
  @observable topic: string = '';
  @observable severity: string = '';
  @observable updateType: UpdateType[] = defaultUpdateType;
  @observable header: string;
  @observable message: string;
  @observable conditions: Array<ConditionBuilderEntity> = [];

  @observable resources: Array<string> = [];
  @observable fields: Array<Field> = [];
  @observable topics: string[];

  private requiredFields = () => [this.name, this.description, this.header, this.message];

  public async openDialog(params: RuleDialogParams) {
    this.topics = await this.notifyService.getNotifyRouteTopics();
    this.resources = await this.systemService.getResources();
    this.ruleDialogMode = params.mode;

    switch (params.mode) {
      case RuleDialogMode.CREATE:
        await this.createRule();
        break;
      case RuleDialogMode.EDIT:
        await this.editRule(params.data);
        break;
    }

    this.dialog.show();
  }

  close() {
    // Objects
    this.ruleId = null;
    this.name = null;
    this.description = null;
    this.header = null;
    this.message = null;
    this.resource = null;
    this.topic = null;
    this.severity = null;
    this.ruleDialogMode = null;

    // Arrays
    this.resources = [];
    this.topics = [];
    this.conditions = [];
    this.updateType = [];

    this.dialog.close();
    this.$emit('close');
  }

  private async createRule() {
    this.conditions.push(DynamicRuleUtils.createEmptyCondition());
    this.resource = this.resources[0];
    this.fields = await this.systemService.getFields(this.resource);
    this.topic = this.topics[0];
    this.severity = Severity.INFORMATION;
    this.updateType = defaultUpdateType;
  }

  private async editRule(data) {
    const rule = data;
    this.ruleId = rule.DYNAMIC_RULE_ID;
    this.name = rule.RULE_NAME;
    this.description = rule.RULE_DESCRIPTION;
    this.resource = rule.RULE_TABLE;
    this.updateType = rule.TABLE_OPERATIONS;
    this.fields = await this.systemService.getFields(this.resource);
    this.conditions = RuleUtils.getConditions(rule.RULE_EXPRESSION.CONDITIONS);
    this.attributeAssignments(rule.RESULT_EXPRESSION?.ASSIGNMENTS);
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

  get ruleDialogTitle(): string {
    return `${this.ruleDialogMode === RuleDialogMode.CREATE ? 'Create' : 'Edit'} Rule`;
  }

  submit() {
    if (this.requiredFields().some((value) => isEmpty(value))) {
      return;
    }

    const rule: Rule = this.createRuleObject();

    switch (this.ruleDialogMode) {
      case RuleDialogMode.CREATE:
        this.ruleService.createRule(rule).then((response) => this.validateResponse(response));
        break;
      case RuleDialogMode.EDIT:
        this.ruleService.updateRule(rule).then((response) => this.validateResponse(response));
        break;
    }
  }

  private createRuleObject(): Rule {
    return {
      ...(this.ruleDialogMode === RuleDialogMode.EDIT && { DYNAMIC_RULE_ID: this.ruleId }),
      RULE_NAME: this.name,
      RULE_DESCRIPTION: this.description,
      RULE_TABLE: this.resource,
      TABLE_OPERATIONS: this.updateType,
      RULE_EXPRESSION: {
        CONDITIONS: DynamicRuleUtils.createConditions(this.conditions),
      },
      RESULT_EXPRESSION: {
        ASSIGNMENTS: [
          DynamicRuleUtils.createAssignment(Assignment.TOPIC, this.topic),
          DynamicRuleUtils.createAssignment(Assignment.HEADER, this.header),
          DynamicRuleUtils.createAssignment(Assignment.BODY, this.message),
          DynamicRuleUtils.createAssignment(Assignment.NOTIFY_SEVERITY, humanize(this.severity)),
        ],
      },
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

  @volatile
  public get validateRequiredFields(): boolean {
    return this.requiredFields().some((value) => isEmpty(value));
  }
}
