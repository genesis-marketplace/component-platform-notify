import { sync } from '@genesislcap/foundation-utils';
import { html, ref, repeat } from '@genesislcap/web-core';
import { ConditionBuilderEntity, Severity, UpdateType } from '../../../../../notify.types';
import { eventDetail } from '../../../../../utils/eventDetail';
import type { TemplateDialog } from './template-dialog';

export const TemplateDialogTemplate = html<TemplateDialog>`
  <rapid-modal ${ref('dialog')} class="dialog" :onCloseCallback=${(x) => () => x.close()}>
    <div slot="top" class="dialog-top">${(x) => x.templateDialogTitle}</div>

    <div class="dialog-content">
      <div class="content-row">
        <div class="control">
          <rapid-text-field :value=${sync((x) => x.name)} autofocus>Name</rapid-text-field>
        </div>

        <div class="control">
          <rapid-text-field :value=${sync((x) => x.description)}>Description</rapid-text-field>
        </div>
      </div>

      <div class="content-row">
        <div class="control">
          <label>Resource</label>
          <rapid-select :value=${sync((x) => x.resource)} position="below">
            ${repeat(
              (x) => x.resources,
              html`
                <rapid-option value=${(x) => x}>${(x) => x}</rapid-option>
              `,
            )}
          </rapid-select>
        </div>

        <div class="control">
          <label>Topic</label>
          <rapid-select :value=${sync((x) => x.topic)}>
            ${repeat(
              (x) => x.topics,
              html`
                <rapid-option value=${(x) => x}>${(x) => x}</rapid-option>
              `,
            )}
          </rapid-select>
        </div>

        <div class="control">
          <label>Severity</label>
          <rapid-select :value=${sync((x) => x.severity)}>
            <options-datasource
              value-field="value"
              label-field="value"
              option-element="rapid-option"
              :data=${(_) => Object.values(Severity).map((value) => ({ value }))}
            ></options-datasource>
          </rapid-select>
        </div>

        <div class="control">
          <label>Update Type</label>
          <rapid-multiselect
            :selectedOptions=${(x) => x.updateType}
            @selectionChange=${(x, c) => {
              x.updateType = (c.event as CustomEvent).detail;
            }}
            search="false"
            all="false"
          >
            <multiselect-datasource
              value-field="value"
              label-field="value"
              :data=${() => Object.values(UpdateType).map((value) => ({ value, label: value }))}
            ></multiselect-datasource>
          </rapid-multiselect>
        </div>
      </div>

      <div class="content-row">
        <div class="control">
          <rapid-text-field :value=${sync((x) => x.header)}>Header</rapid-text-field>
        </div>

        <div class="control">
          <rapid-text-field :value=${sync((x) => x.message)}>Message</rapid-text-field>
        </div>
      </div>

      <div class="control">
        <label>Parameter(s)</label>
        <div class="items">
          <div class="content-row item">
            ${repeat(
              (x) => x.parameters,
              html`
                <parameter-builder
                  :parameter=${(x) => x}
                  @edit=${(x, c) => c.parent.editParameter(eventDetail(c))}
                  @delete=${(x, c) => c.parent.deleteParameter(eventDetail(c))}
                ></parameter-builder>
              `,
            )}
          </div>

          <div class="content-row">
            <rapid-button @click=${(x) => x.newParameter()}>+ Parameter</rapid-button>
          </div>
        </div>
      </div>

      <div class="control">
        <label>Condition(s)</label>
        <div class="items">
          <div class="content-row item">
            ${repeat(
              (x) => x.conditions,
              html<ConditionBuilderEntity>`
                <template-condition-builder
                  :fields=${(x, c) => c.parent.fields}
                  :parameters=${(x, c) => c.parent.parameters}
                  :condition=${(x) => x}
                  @edit=${(x, c) => c.parent.editCondition(eventDetail(c))}
                  @delete=${(x, c) => c.parent.deleteCondition(eventDetail(c))}
                ></template-condition-builder>
              `,
            )}
          </div>

          <div class="content-row">
            <rapid-button @click=${(x) => x.newCondition()}>+ Condition</rapid-button>
          </div>
        </div>
      </div>
    </div>

    <div slot="bottom" class="dialog-bottom">
      <rapid-button
        appearance="accent"
        @click=${(x) => x.submit()}
      >
        Submit
      </rapid-button>
    </div>
  </rapid-modal>
`;
