import { attr, customElement, GenesisElement, observable } from '@genesislcap/web-core';
import {
  ConditionBuilderEntity,
  LogicalOperator,
  RightCriteria,
  nullAndBlankLogicalOperatorValues,
} from '../../../../../../notify.types';
import { logger } from '../../../../../../utils';
import { ConditionBuilderStyles } from '../../../../styles/condition-builder.styles';
import { ParameterBuilderEntity } from '../template-dialog.types';
import { TemplateConditionBuilderTemplate } from './template-condition-builder.template';

@customElement({
  name: 'template-condition-builder',
  template: TemplateConditionBuilderTemplate,
  styles: ConditionBuilderStyles,
})
export class TemplateConditionBuilder extends GenesisElement {
  @attr condition: ConditionBuilderEntity;
  @attr parameters: Array<ParameterBuilderEntity>;
  @attr fields: Array<string>;

  @observable leftValue: any;
  @observable leftValueItems: Array<any> = [];

  @observable logicalOperator: string;
  @observable rightCriteria: string = RightCriteria.VALUE;
  @observable rightValueText: string;
  @observable rightValueSelect: string;
  @observable rightValueItems: Array<any> = [];

  deleteCondition() {
    this.$emit('delete', this.condition as ConditionBuilderEntity);
  }

  async conditionChanged() {
    if (this.fields) {
      this.leftValueItems = this.fields;
    }

    this.assignLeftValue();

    this.logicalOperator = this.condition.LOGICAL_OPERATOR;

    const rightValue = this.condition.RIGHT_VALUE;
    this.rightCriteria = this.condition.RIGHT_CRITERIA;
    this.condition.RIGHT_VALUE = rightValue;
    this.assignRightValue();

    this.$emit('edit', this.condition as ConditionBuilderEntity);
  }

  parametersChanged() {
    if (!Array.isArray(this.parameters) || !this.parameters) {
      return;
    }

    this.rightValueItems = this.parameters;
  }

  fieldsChanged() {
    if (!Array.isArray(this.fields) || !this.fields.length) {
      return;
    }

    this.leftValueItems = this.fields;
    this.leftValue = JSON.stringify(this.leftValueItems[0]);
  }

  logicalOperatorChanged() {
    if (!this.condition) {
      return;
    }

    if (nullAndBlankLogicalOperatorValues.includes(LogicalOperator[this.logicalOperator])) {
      this.rightCriteria = RightCriteria.VALUE;
      this.clearRightValueText();
    }

    this.condition.LOGICAL_OPERATOR = this.logicalOperator;
    this.$emit('edit', this.condition as ConditionBuilderEntity);
  }

  // #region LeftValue
  private assignLeftValue() {
    // Add (fresh new; assign the 1st element)
    if (!this.condition.LEFT_VALUE) {
      this.leftValue = JSON.stringify(this.leftValueItems[0]);
      return;
    }

    // Add (value changed)
    if (typeof this.condition.LEFT_VALUE !== 'string') {
      this.leftValue = JSON.stringify(this.condition.LEFT_VALUE);
      return;
    }

    // Edit
    this.leftValue = JSON.stringify(
      this.leftValueItems.find((item) => item.FIELD_NAME === this.condition.LEFT_VALUE),
    );
  }

  leftValueChanged() {
    if (!this.condition) {
      return;
    }

    this.condition.LEFT_VALUE = JSON.parse(this.leftValue);
    this.$emit('edit', this.condition as ConditionBuilderEntity);
  }
  // #endregion

  // #region RightValue
  private assignRightValue() {
    if (this.condition.RIGHT_CRITERIA === RightCriteria.VALUE) {
      this.rightValueText = this.condition.RIGHT_VALUE;
      return;
    }

    if (this.parameters?.length > 0) {
      this.rightValueSelect = this.condition.RIGHT_VALUE;
      return;
    }

    this.rightValueSelect = null;
  }

  rightCriteriaChanged() {
    if (!this.condition) {
      return;
    }

    this.condition.RIGHT_CRITERIA = this.rightCriteria;

    switch (this.rightCriteria) {
      case RightCriteria.VALUE:
        this.clearRightValueText();
        break;
      case RightCriteria.USER_ENTRY:
        this.clearRightValueSelect();
        break;
      default:
        logger.error(`Unknown RightCriteria: ${this.rightCriteria}`);
        break;
    }

    this.$emit('edit', this.condition as ConditionBuilderEntity);
  }

  private clearRightValueText() {
    this.rightValueText = '';
  }

  private clearRightValueSelect() {
    this.rightValueSelect = this.parameters?.length > 0 ? this.parameters[0].UUID : null;
  }

  rightValueTextChanged() {
    if (!this.condition) {
      return;
    }

    this.condition.RIGHT_VALUE = this.rightValueText;
    this.$emit('edit', this.condition as ConditionBuilderEntity);
  }

  rightValueSelectChanged() {
    if (!this.condition) {
      return;
    }

    this.condition.RIGHT_VALUE = this.rightValueSelect;
    this.$emit('edit', this.condition as ConditionBuilderEntity);
  }
  // #endregion
}
