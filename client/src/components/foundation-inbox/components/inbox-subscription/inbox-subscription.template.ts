import { html, repeat, when } from '@genesislcap/web-core';
import { eventDetail } from '../../../../utils';
import { RuleParameterEntity } from '../../inbox.types';
import type { InboxSubscription } from './inbox-subscription';

export const InboxSubscriptionTemplate = html<InboxSubscription>`
  <div class="inbox-subscription">
    <div class="inbox-subscription-header">
      Subscribe to
      <span style="font-weight: bold;">${(x) => x.template.RULE_NAME}</span>
    </div>

    ${when(
      (x) => Object.values(x.template.PARAMETERS).length === 0,
      html`
        <p style="padding-left: 10px;">The selected template doesn't have parameters.</p>
      `,
    )}
    ${when(
      (x) => Object.values(x.template.PARAMETERS).length > 0,
      html`
        <div class="inbox-subscription-content">
          ${repeat(
            // @ts-ignore
            (x) => Object.values(x.template.PARAMETERS),
            html<RuleParameterEntity>`
              <rule-parameter
                :parameter=${(x) => x}
                :isFirstElement=${(x, c) => c.parent.isFirstElement(x)}
                :operator=${(x, c) => c.parent.parametersOperators[x.PARAM_NAME]}
                @parameterEdited=${(x, c) => c.parent.parameterEdit(eventDetail(c))}
              ></rule-parameter>
            `,
          )}
        </div>
      `,
    )}

    <div class="inbox-subscription-footer">
      <rapid-button
        class="subscribe"
        @click=${(x) => x.subscribeTemplate()}
        ?disabled=${(x) => x.subscribeDisabled}
      >
        Subscribe
      </rapid-button>
    </div>
  </div>
`;
