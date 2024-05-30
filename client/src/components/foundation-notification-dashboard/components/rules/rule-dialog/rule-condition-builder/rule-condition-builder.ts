import { customElement, GenesisElement, observable } from '@genesislcap/web-core';
import {
  ConditionBuilderEntity,
  LogicalOperator,
  nullAndBlankLogicalOperatorValues,
} from '../../../../../../notify.types';
import { Field } from '../../../../../../services/system.service';
import { logger } from '../../../../../../utils';
import { ConditionBuilderStyles } from '../../../../styles/condition-builder.styles';
import { RuleConditionBuilderTemplate } from './rule-condition-builder.template';
import { RightCriteria } from './rule-condition-builder.types';

@customElement({
  name: 'rule-condition-builder',
  template: RuleConditionBuilderTemplate,
  styles: ConditionBuilderStyles,
})
export class RuleConditionBuilder extends GenesisElement {
  @observable condition: ConditionBuilderEntity;
  @observable fields: Field[];

  @observable leftValue: string;
  @observable leftValueItems: Array<any> = [];

  @observable logicalOperator: string;

  @observable rightCriteria: string = RightCriteria.VALUE;
  @observable rightValueText: string;
  @observable rightValueSelect: string;

  deleteCondition() {
    this.$emit('delete', this.condition as ConditionBuilderEntity);
  }

  async conditionChanged() {
    if (this.fields) {
      this.leftValueItems = this.fields;
    }

    this.assignLeftValue();
    this.logicalOperator = this.condition.LOGICAL_OPERATOR;
    this.rightCriteria = this.condition.RIGHT_CRITERIA;
    this.assignRightValue();

    this.$emit('edit', this.condition as ConditionBuilderEntity);
  }

  fieldsChanged() {
    if (!Array.isArray(this.fields) || !this.fields.length) {
      return;
    }

    this.leftValueItems = this.fields;
    this.leftValue = JSON.stringify(this.leftValueItems[0]);
  }

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

  assignRightValue() {
    if (this.condition.RIGHT_CRITERIA === RightCriteria.VALUE) {
      this.rightValueText = this.condition.RIGHT_VALUE;
      return;
    }

    this.rightValueSelect = null;
  }

  leftValueChanged() {
    if (!this.condition) {
      return;
    }

    this.condition.LEFT_VALUE = JSON.parse(this.leftValue);
    this.$emit('edit', this.condition as ConditionBuilderEntity);
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

  rightCriteriaChanged() {
    if (!this.condition) {
      return;
    }

    this.condition.RIGHT_CRITERIA = this.rightCriteria;

    switch (this.rightCriteria) {
      case RightCriteria.VALUE:
        this.clearRightValueText();
        break;
      case RightCriteria.FIELD:
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
    this.rightValueSelect = this.fields[0].FIELD_NAME;
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
}
