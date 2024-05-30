import { attr, customElement, GenesisElement, observable } from '@genesislcap/web-core';
import { RuleService } from '../../../../services/rule.service';
import { EXPRESSION_TYPE_PLACEHOLDER } from '../../../foundation-notification-dashboard/types/expression-type';
import { PARAM_SOURCE_TYPE_DEFINED_GROUP } from '../../../foundation-notification-dashboard/types/param-source-type';
import { RuleParameterEntity } from '../../inbox.types';
import { InboxSubscriptionStyles } from './inbox-subscription.styles';
import { InboxSubscriptionTemplate } from './inbox-subscription.template';

@customElement({
  name: 'inbox-subscription',
  template: InboxSubscriptionTemplate,
  styles: InboxSubscriptionStyles,
})
export class InboxSubscription extends GenesisElement {
  @attr template;
  private parametersDetails = {};
  @observable parametersOperators = {};
  @observable subscribeDisabled = true;
  @RuleService ruleService: RuleService;

  connectedCallback() {
    super.connectedCallback();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.parametersDetails = {};
    this.subscribeDisabled = true;
  }

  templateChanged() {
    const parameters: RuleParameterEntity = this.template.PARAMETERS;

    Object.keys(parameters).forEach((key) => {
      const parameter = parameters[key];

      if (parameter.PARAM_SOURCE_TYPE === PARAM_SOURCE_TYPE_DEFINED_GROUP) {
        // Attribute 1st group value for select fields
        this.parametersDetails[parameter.PARAM_NAME] = parameter.PARAM_SOURCE.split(',')[0];
      }

      this.parametersDetails[parameter.PARAM_NAME] = '';
    });

    this.parametersOperators = this.createParametersOperatorMap(
      this.template.RULE_EXPRESSION.CONDITIONS,
    );

    this.verifySubscribe();
  }

  parameterEdit(value: any) {
    this.parametersDetails[value.parameter.PARAM_NAME] = value.valueTyped;
    this.verifySubscribe();
  }

  verifySubscribe() {
    const parameters = Object.values(this.parametersDetails);
    this.subscribeDisabled = parameters.length > 0 ? !parameters.every((x) => x !== '') : false;
  }

  isFirstElement = (parameter: RuleParameterEntity) =>
    (Object.values(this.template.PARAMETERS) as any).findIndex(
      (p) => p.PARAM_NAME === parameter.PARAM_NAME,
    ) === 0;

  async subscribeTemplate() {
    await this.ruleService.subscribeRule(this.template.DYNAMIC_RULE_ID, this.parametersDetails);
    this.$emit('templateSubscribed');
  }

  createParametersOperatorMap = (conditions) => {
    this.parametersOperators = {};
    conditions.forEach((condition) => {
      if (condition.RIGHT.TYPE == EXPRESSION_TYPE_PLACEHOLDER) {
        this.parametersOperators[condition.RIGHT.KEY] = condition.OPERATION;
      }
    });
    return this.parametersOperators;
  };
}
