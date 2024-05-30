import type { Select, TextField } from '@genesislcap/rapid-design-system';
import { attr, customElement, GenesisElement, observable } from '@genesislcap/web-core';
import {
  PARAM_SOURCE_TYPE_DEFINED_GROUP,
  PARAM_SOURCE_TYPE_REQ_REP,
} from '../../../../types/param-source-type';
import { ParameterBuilderEntity } from '../template-dialog.types';
import { ParameterBuilderStyles as styles } from './parameter-builder.styles';
import { ParameterBuilderTemplate as template } from './parameter-builder.template';

const name = 'parameter-builder';

@customElement({
  name,
  template,
  styles,
})
export class ParameterBuilder extends GenesisElement {
  @attr parameter: ParameterBuilderEntity;

  @observable name: string;
  @observable label: string;
  @observable type: string;
  @observable sourceType: string;

  @observable sourceSelected: string;
  @observable sourceTyped: string;

  sourceTextField: TextField;
  sourceSelect: Select;

  reqRepItems = ['REQUEST_1', 'REQUEST_2', 'REQUEST_3'];

  async connectedCallback() {
    super.connectedCallback();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
  }

  deleteParameter() {
    this.$emit('delete', this.parameter as ParameterBuilderEntity);
  }

  // #region Changed Parameters
  parameterChanged() {
    this.name = this.parameter.PARAM_NAME;
    this.label = this.parameter.PARAM_LABEL;
    this.type = this.parameter.PARAM_TYPE;
    const paramTemp = this.parameter.PARAM_SOURCE; // Can be cleaned
    this.sourceType = this.parameter.PARAM_SOURCE_TYPE;

    switch (this.sourceType) {
      case PARAM_SOURCE_TYPE_REQ_REP:
        this.sourceSelected = this.parameter.PARAM_SOURCE ?? paramTemp;
        break;
      case PARAM_SOURCE_TYPE_DEFINED_GROUP:
        this.sourceTyped = this.parameter.PARAM_SOURCE ?? paramTemp;
        break;
      default:
        break;
    }
  }

  nameChanged() {
    this.genericChanged('PARAM_NAME', this.name);
  }

  labelChanged() {
    this.genericChanged('PARAM_LABEL', this.label);
  }

  typeChanged() {
    this.genericChanged('PARAM_TYPE', this.type);
  }

  sourceTypeChanged() {
    this.genericChanged('PARAM_SOURCE_TYPE', this.sourceType);

    switch (this.sourceType) {
      case PARAM_SOURCE_TYPE_REQ_REP:
        this.sourceTyped = '';
        break;
      case PARAM_SOURCE_TYPE_DEFINED_GROUP:
        this.sourceSelected = this.reqRepItems[0];
        break;
      default:
        this.sourceSelected = this.reqRepItems[0];
        this.sourceTyped = '';
        break;
    }
  }

  sourceSelectedChanged() {
    if (this.sourceType !== PARAM_SOURCE_TYPE_REQ_REP) {
      return;
    }

    this.genericChanged('PARAM_SOURCE', this.sourceSelected);
  }

  sourceTypedChanged() {
    if (this.sourceType !== PARAM_SOURCE_TYPE_DEFINED_GROUP) {
      return;
    }

    this.genericChanged('PARAM_SOURCE', this.sourceTyped);
  }
  // #endregion

  private genericChanged(parameterAttribute, value) {
    if (!this.parameter) {
      return;
    }

    if (this.parameter[parameterAttribute] === value) {
      return;
    }

    this.parameter[parameterAttribute] = value;
    this.$emit('edit', this.parameter as ParameterBuilderEntity);
  }
}
