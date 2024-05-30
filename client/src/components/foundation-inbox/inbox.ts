import { customElement } from '@genesislcap/web-core';
import { FoundationInboxBase } from './inbox-base/inbox-base';
import { FoundationInboxStyles } from './inbox.styles';
import { FoundationInboxTemplate } from './inbox.template';

/**
 * Foundation inbox component for displaying notifies list.
 * Allow searching, filtering and deleting notifies
 * @beta
 */
@customElement({
  name: 'foundation-inbox',
  template: FoundationInboxTemplate,
  styles: FoundationInboxStyles,
})
export class FoundationInbox extends FoundationInboxBase {}
