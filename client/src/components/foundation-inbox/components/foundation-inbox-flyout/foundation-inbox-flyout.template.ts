import { html, ref } from '@genesislcap/web-core';
import { FoundationInboxTemplate } from '../../inbox.template';
import type { FoundationInboxFlyout } from './foundation-inbox-flyout';

export const FoundationInboxFlyoutTemplate = html<FoundationInboxFlyout>`
  <rapid-flyout
    ${ref('flyout')}
    position="right"
    @closed=${(x) => (x.flyoutClosed = true)}
    :closed=${(x) => x.flyoutClosed}
    displayHeader=${false}
  >
    <div class="inbox-header">
      <div class="inbox-header-title">Alerts Center</div>
      <rapid-button class="inbox-header-close" @click=${(x) => x.close()}>
        <rapid-icon part="icon" name="xmark" size="xl"></rapid-icon>
      </rapid-button>
    </div>

    ${FoundationInboxTemplate}
  </rapid-flyout>
`;
