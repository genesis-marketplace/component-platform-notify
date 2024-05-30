import { customElement, GenesisElement, html } from '@genesislcap/web-core';
import { managementGridStyles } from '../../../styles/management.styles';

const EMAIL_USER_ROUTE_COLUMNS = [
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
    field: 'ENTITY_ID',
    headerName: 'Entity Id',
    flex: 1,
  },
  {
    field: 'ENTITY_ID_TYPE',
    headerName: 'Entity Id Type',
    flex: 1,
  },
  {
    field: 'RIGHT_CODE',
    headerName: 'Right Code',
    flex: 1,
  },
  {
    field: 'AUTH_CACHE_NAME',
    headerName: 'Auth Cache Name',
    flex: 1,
  },
  {
    field: 'EXCLUDE_SENDER',
    headerName: 'Exclude Sender',
    flex: 1,
  },
];

@customElement({
  name: 'email-user-route-management',
  template: html<EmailUserRouteManagement>`
    <div class="grid">
      <entity-management
        design-system-prefix="rapid"
        resourceName="ALL_EMAIL_USER_ROUTES"
        createEvent="EVENT_EMAIL_USER_ROUTE_CREATE"
        updateEvent="EVENT_EMAIL_USER_ROUTE_UPDATE"
        deleteEvent="EVENT_EMAIL_USER_ROUTE_DELETE"
        :columns=${() => [...EMAIL_USER_ROUTE_COLUMNS]}
      ></entity-management>
    </div>
  `,
  styles: managementGridStyles,
})
export class EmailUserRouteManagement extends GenesisElement {}
