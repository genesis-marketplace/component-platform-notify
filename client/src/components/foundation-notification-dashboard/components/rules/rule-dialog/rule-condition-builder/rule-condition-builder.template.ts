import { sync } from '@genesislcap/foundation-utils';
import { classNames, html, repeat, when } from '@genesislcap/web-core';
import { LogicalOperator, nullAndBlankLogicalOperatorValues } from '../../../../../../notify.types';
import type { RuleConditionBuilder } from './rule-condition-builder';
import { RightCriteria } from './rule-condition-builder.types';

export const RuleConditionBuilderTemplate = html<RuleConditionBuilder>`
  <div class="condition-builder">
    <rapid-select
      :value=${sync((x) => x.leftValue)}
      :initialValue=${sync((x) => x.leftValue)}
      class="left-value"
    >
      ${repeat(
        (x) => x.fields,
        html`
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
            (x) => Object.values(RightCriteria),
            html`
              <rapid-option value=${(x) => x}>${(x) => x}</rapid-option>
            `,
          )}
        </rapid-select>
        ${when(
          (x) => x.rightCriteria === RightCriteria.VALUE,
          html`
            <rapid-text-field
              class="right-criteria-text"
              :value=${sync((x) => x.rightValueText)}
              placeholder="Value"
              autocomplete="off"
            ></rapid-text-field>
          `,
        )}
        ${when(
          (x) => x.rightCriteria === RightCriteria.FIELD,
          html`
            <rapid-select class="right-criteria-select" :value=${sync((x) => x.rightValueSelect)}>
              ${repeat(
                (x) => x.fields,
                html`
                  <rapid-option value=${(x) => x.FIELD_NAME}>${(x) => x.FIELD_NAME}</rapid-option>
                `,
              )}
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
