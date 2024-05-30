import { Auth } from '@genesislcap/foundation-comms';
import { customElement, GenesisElement, observable } from '@genesislcap/web-core';
import { loadRemotes } from '../components';
import { FoundationNotificationDashboardStyles as styles } from './notification-dashboard.styles';
import { FoundationNotificationDashboardTemplate as template } from './notification-dashboard.template';

/**
 * @public
 */
@customElement({
  name: 'foundation-notification-dashboard',
  template,
  styles,
})
export class FoundationNotificationDashboard extends GenesisElement {
  @Auth auth: Auth;
  @observable ready: boolean = false;

  async connectedCallback() {
    super.connectedCallback();
    await this.loadRemotes();
  }

  /**
   * Load remote components
   * @remarks With regards to module federation
   * @internal
   */
  async loadRemotes() {
    const remoteComponents = await loadRemotes();
    this.ready = true;
  }
}
