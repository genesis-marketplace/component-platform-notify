import { customElement, GenesisElement, observable } from '@genesislcap/web-core';
import { FoundationInboxService } from '../../../../services/inbox.service';
import { AlertStatus } from '../../inbox.types';
import { FoundationInboxCounterStyles } from './foundation-inbox-counter.styles';
import { FoundationInboxCounterTemplate } from './foundation-inbox-counter.template';

@customElement({
  name: 'foundation-inbox-counter',
  template: FoundationInboxCounterTemplate,
  styles: FoundationInboxCounterStyles,
})
export class FoundationInboxCounter extends GenesisElement {
  @observable value: number = 0;
  @FoundationInboxService inboxService: FoundationInboxService;
  private subscriptionHandler: (any) => void;

  async connectedCallback() {
    super.connectedCallback();
    this.subscriptionHandler = ({ store }) => {
      this.value = store?.items.filter((alert) => alert.ALERT_STATUS === AlertStatus.NEW).length;
    };
    this.inboxService.subscribe(this.subscriptionHandler);
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.inboxService.unsubscribe(this.subscriptionHandler);
  }
}
