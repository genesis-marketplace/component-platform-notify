import { attr, customElement, GenesisElement, observable } from '@genesislcap/web-core';
import {
  PARAM_SOURCE_TYPE_DEFINED_GROUP,
  PARAM_SOURCE_TYPE_USER_TEXT,
} from '../../../../foundation-notification-dashboard/types/param-source-type';
import { PARAM_TYPE_BOOLEAN } from '../../../../foundation-notification-dashboard/types/param-type';
import { RuleParameterEntity } from '../../../inbox.types';
import { RuleParameterStyles } from './rule-parameter.styles';
import { RuleParameterTemplate } from './rule-parameter.template';

@customElement({
  name: 'rule-parameter',
  template: RuleParameterTemplate,
  styles: RuleParameterStyles,
})
export class RuleParameter extends GenesisElement {
  @attr @observable parameter: RuleParameterEntity;
  @attr isFirstElement: boolean = false;
  @attr operator: string;
  @observable inputValue: string = '';

  connectedCallback() {
    super.connectedCallback();

    switch (this.parameter.PARAM_SOURCE_TYPE) {
      case PARAM_SOURCE_TYPE_DEFINED_GROUP:
        this.inputValue = this.parameter.PARAM_SOURCE.split(',')[0];
        break;
      case PARAM_SOURCE_TYPE_USER_TEXT:
        switch (this.parameter.PARAM_TYPE) {
          case PARAM_TYPE_BOOLEAN:
            this.inputValue = 'true';
            break;
          default:
            this.inputValue = '';
            break;
        }
        break;
      default:
        break;
    }
  }

  disconnectedCallback() {
    super.disconnectedCallback();
  }

  parameterEdit(inputValue) {
    this.inputValue = inputValue;
    this.$emit('parameterEdited', {
      parameter: this.parameter,
      valueTyped: inputValue,
    } as any);
  }
}
