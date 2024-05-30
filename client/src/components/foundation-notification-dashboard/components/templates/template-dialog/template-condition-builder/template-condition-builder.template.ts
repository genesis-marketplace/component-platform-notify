import { sync } from '@genesislcap/foundation-utils';
import { classNames, html, repeat, when } from '@genesislcap/web-core';
import {
  LogicalOperator,
  RightCriteria,
  nullAndBlankLogicalOperatorValues,
} from '../../../../../../notify.types';
import { Field } from '../../../../../../services/system.service';
import type { TemplateConditionBuilder } from './template-condition-builder';

export const TemplateConditionBuilderTemplate = html<TemplateConditionBuilder>`
  <div class="condition-builder">
    <rapid-select
      :value=${sync((x) => x.leftValue)}
      :initialValue=${sync((x) => x.leftValue)}
      class="left-value"
    >
      ${repeat(
        (x) => x.leftValueItems,
        html<Field>`
          <rapid-option value=${(x) => JSON.stringify(x)}>${(x) => x.FIELD_NAME}</rapid-option>
        `,
      )}
    </rapid-select>

    <rapid-select
      class="logical-operator${(x) =>
        classNames([
          '-null-and-blank',
          nullAndBlankLogicalOperatorValues.includes(LogicalOperator[x.logicalOperator]),
        ])}"
      :value=${sync((x) => x.logicalOperator)}
    >
      ${repeat(
        (_) => Object.values(LogicalOperator),
        html`
          <rapid-option value=${(x) => x}>${(x) => x}</rapid-option>
        `,
      )}
    </rapid-select>

    ${when(
      (x) => !nullAndBlankLogicalOperatorValues.includes(LogicalOperator[x.logicalOperator]),
      html`
        <rapid-select class="right-criteria" :value=${sync((x) => x.rightCriteria)}>
          ${repeat(
            (_) => Object.values(RightCriteria),
            html`
              <rapid-option value=${(x) => x}>${(x) => x}</rapid-option>
            `,
          )}
        </rapid-select>

        ${when(
          (x) => x.rightCriteria === RightCriteria.VALUE,
          html<TemplateConditionBuilder>`
            <rapid-text-field
              class="right-criteria-text"
              :value=${sync((x) => x.rightValueText)}
              placeholder="Value"
              autocomplete="off"
            ></rapid-text-field>
          `,
        )}
        ${when(
          (x) => x.rightCriteria === RightCriteria.USER_ENTRY,
          html<TemplateConditionBuilder>`
            <rapid-select class="right-criteria-select" :value=${sync((x) => x.rightValueSelect)}>
              ${(x) => rightValueItemsOptions(x.rightValueItems)}
            </rapid-select>
          `,
        )}
      `,
    )}

    <rapid-button appearance="icon" class="delete" @click=${(x) => x.deleteCondition()}>
      <rapid-icon
        style="opacity: 0.5"
        variant="regular"
        name="trash-alt"
        size="lg"
        part="icon"
      ></rapid-icon>
    </rapid-button>
  </div>
`;

const rightValueItemsOptions = (rightValueItems) => html`
  ${repeat(
    (x) => rightValueItems,
    html`
      <rapid-option value="${(x) => x.UUID ?? ''}">${(x) => x.PARAM_NAME ?? ''}</rapid-option>
    `,
  )}
`;
