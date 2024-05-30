import { customElement, GenesisElement, html } from '@genesislcap/web-core';
import { managementGridStyles } from '../../../styles/management.styles';

const EMAIL_DISTRIBUTION_ROUTE_MANAGEMENT_COLUMNS = [
  {
    field: 'NOTIFY_ROUTE_ID',
    headerName: 'Notify Route Id',
    flex: 1,
  },
  {
    field: 'TOPIC_MATCH',
    headerName: 'Topic Match',
    flex: 1,
  },
  {
    field: 'GATEWAY_ID',
    headerName: 'Gateway Id',
    flex: 1,
  },
  {
    field: 'EMAIL_TO',
    headerName: 'Email To',
    flex: 1,
  },
  {
    field: 'EMAIL_CC',
    headerName: 'Email Cc',
    flex: 1,
  },
  {
    field: 'EMAIL_BCC',
    headerName: 'Email Bcc',
    flex: 1,
  },
];

@customElement({
  name: 'email-distribution-route-management',
  template: html<EmailDistributionRouteManagement>`
    <div class="grid">
      <entity-management
        design-system-prefix="rapid"
        resourceName="ALL_EMAIL_DISTRIBUTION_ROUTES"
        createEvent="EVENT_EMAIL_DISTRIBUTION_ROUTE_CREATE"
        updateEvent="EVENT_DYNAMIC_RULE_NOTIFY_UPDATE"
        deleteEvent="EVENT_DYNAMIC_RULE_NOTIFY_DELETE"
        :columns=${() => [...EMAIL_DISTRIBUTION_ROUTE_MANAGEMENT_COLUMNS]}
      ></entity-management>
    </div>
  `,
  styles: managementGridStyles,
})
export class EmailDistributionRouteManagement extends GenesisElement {}
