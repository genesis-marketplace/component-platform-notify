import { sync } from '@genesislcap/foundation-utils';
import { html, ref, repeat, when } from '@genesislcap/web-core';
import {
  ALL_PARAM_SOURCE_TYPES,
  PARAM_SOURCE_TYPE_DEFINED_GROUP,
  PARAM_SOURCE_TYPE_REQ_REP,
  PARAM_SOURCE_TYPE_USER_TEXT,
} from '../../../../types/param-source-type';
import { ALL_PARAM_TYPES } from '../../../../types/param-type';
import type { ParameterBuilder } from './parameter-builder';

export const ParameterBuilderTemplate = html<ParameterBuilder>`
  <div class="parameter-builder">
    <rapid-text-field
      id="param-name"
      placeholder="Name"
      :value=${sync((x) => x.name)}
    ></rapid-text-field>

    <rapid-text-field
      id="param-label"
      placeholder="Label"
      :value=${sync((x) => x.label)}
    ></rapid-text-field>

    <rapid-select
      id="param-source-type"
      :value=${sync((x) => x.sourceType)}
      class="param-source-type"
    >
      ${repeat(
        (x) => ALL_PARAM_SOURCE_TYPES,
        html`
          <rapid-option value=${(x) => x}>${(x) => x}</rapid-option>
        `,
      )}
    </rapid-select>

    ${when(
      (x) => x.sourceType === PARAM_SOURCE_TYPE_USER_TEXT,
      html<ParameterBuilder>`
        <rapid-text-field class="param-source" disabled></rapid-text-field>
      `,
    )}
    ${when(
      (x) => x.sourceType === PARAM_SOURCE_TYPE_REQ_REP,
      html<ParameterBuilder>`
        <rapid-select
          ${ref('sourceSelect')}
          id="param-source"
          :value=${sync((x) => x.sourceSelected)}
          class="param-source"
          ?disabled=${(x) => x.sourceType !== PARAM_SOURCE_TYPE_REQ_REP}
        >
          ${repeat(
            (x) => x.reqRepItems,
            html`
              <rapid-option value=${(x) => x}>${(x) => x}</rapid-option>
            `,
          )}
        </rapid-select>
      `,
    )}
    ${when(
      (x) => x.sourceType === PARAM_SOURCE_TYPE_DEFINED_GROUP,
      html<ParameterBuilder>`
        <rapid-text-field
          ${ref('sourceTextField')}
          class="param-source"
          placeholder="item1,item2,item3"
          :value=${sync((x) => x.sourceTyped)}
        ></rapid-text-field>
      `,
    )}

    <rapid-select id="param-type" :value=${sync((x) => x.type)} class="param-type">
      ${repeat(
        (x) => ALL_PARAM_TYPES,
        html`
          <rapid-option value=${(x) => x}>${(x) => x}</rapid-option>
        `,
      )}
    </rapid-select>

    <rapid-button appearance="icon" class="delete" @click=${(x) => x.deleteParameter()}>
      <rapid-icon name="trash" part="icon"></rapid-icon>
    </rapid-button>
  </div>
`;
