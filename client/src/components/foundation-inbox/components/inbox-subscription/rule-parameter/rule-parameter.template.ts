import { sync } from '@genesislcap/foundation-utils';
import { html, repeat, when } from '@genesislcap/web-core';
import { humanize } from '../../../../../utils';
import {
  PARAM_SOURCE_TYPE_DEFINED_GROUP,
  PARAM_SOURCE_TYPE_REQ_REP,
  PARAM_SOURCE_TYPE_USER_TEXT,
} from '../../../../foundation-notification-dashboard/types/param-source-type';
import {
  PARAM_TYPE_BOOLEAN,
  PARAM_TYPE_NUMBER,
  PARAM_TYPE_STRING,
} from '../../../../foundation-notification-dashboard/types/param-type';
import type { RuleParameter } from './rule-parameter';

export const RuleParameterTemplate = html<RuleParameter>`
  <div class="control">
    <label>
      <span style="font-weight: bold;">${(x) => x.parameter.PARAM_LABEL}</span>
      ${(x, c) => humanize(x.operator).toLowerCase()}
    </label>

    <!-- USER_TEXT -->
    ${when(
      (x) => x.parameter.PARAM_SOURCE_TYPE === PARAM_SOURCE_TYPE_USER_TEXT,
      html`
        ${when(
          (x) => x.parameter.PARAM_TYPE === PARAM_TYPE_STRING,
          html`
            <rapid-text-field
              autofocus=${(x) => x.isFirstElement}
              class="input-field"
              :value=${sync((x) => x.inputValue)}
              @input=${(x, c) => x.parameterEdit((c.event.target as HTMLInputElement).value)}
            ></rapid-text-field>
          `,
        )}
        ${when(
          (x) => x.parameter.PARAM_TYPE === PARAM_TYPE_NUMBER,
          html`
            <rapid-number-field
              hide-step
              autofocus=${(x) => x.isFirstElement}
              class="input-field"
              :value=${sync((x) => x.inputValue)}
              @input=${(x, c) => x.parameterEdit((c.event.target as HTMLInputElement).value)}
            ></rapid-number-field>
          `,
        )}

        <!-- TODO: Implement
        ${when(
          (x) => x.parameter.PARAM_TYPE === PARAM_TYPE_BOOLEAN,
          html`
            <rapid-select
              autofocus=${(x) => x.isFirstElement}
              class="input-field"
              :value=${sync((x) => x.inputValue)}
            >
              <rapid-option value="true">true</rapid-option>
              <rapid-option value="false">false</rapid-option>
            </rapid-select>
          `,
        )}
        -->
      `,
    )}

    <!-- DEFINED_GROUP -->
    ${when(
      (x) => x.parameter.PARAM_SOURCE_TYPE === PARAM_SOURCE_TYPE_DEFINED_GROUP,
      html`
        <rapid-select
          autofocus=${(x) => x.isFirstElement}
          class="input-field"
          :value=${sync((x) => x.inputValue)}
        >
          ${repeat(
            (x) => x.parameter.PARAM_SOURCE.split(','),
            html`
              <rapid-option value=${(x) => x}>${(x) => x}</rapid-option>
            `,
          )}
        </rapid-select>
      `,
    )}

    <!-- TODO: REQ_REP -->
    ${when(
      (x) => x.parameter.PARAM_SOURCE_TYPE === PARAM_SOURCE_TYPE_REQ_REP,
      html`
        <rapid-select :value=${sync((x) => x.inputValue)}></rapid-select>
      `,
    )}
  </div>
`;
