import type { Flyout } from '@genesislcap/rapid-design-system';
import { observable, customElement } from '@genesislcap/web-core';
import { loadRemotes } from '../../../components';
import { FoundationInboxBase } from '../../inbox-base/inbox-base';
import { FoundationInboxFlyoutStyles } from './foundation-inbox-flyout.styles';
import { FoundationInboxFlyoutTemplate } from './foundation-inbox-flyout.template';

@customElement({
  name: 'foundation-inbox-flyout',
  template: FoundationInboxFlyoutTemplate,
  styles: FoundationInboxFlyoutStyles,
})
export class FoundationInboxFlyout extends FoundationInboxBase {
  @observable ready: boolean = false;

  flyout: Flyout;
  @observable flyoutClosed: boolean = true;

  async connectedCallback() {
    super.connectedCallback();
    await this.loadRemotes();
  }

  disconnectedCallback(): void {
    super.disconnectedCallback();
  }

  public async open() {
    this.flyoutClosed = false;
    this.rules = await this.ruleService.getRules();
    this.templates = await this.templateService.getRuleTemplates();
  }

  public close() {
    this.ruleTemplateDetails = null;
    this.flyout.closeFlyout();
  }

  async loadRemotes() {
    const remoteComponents = await loadRemotes();
    this.ready = true;
  }
}
