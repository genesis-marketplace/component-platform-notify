import { customElement, GenesisElement, html } from '@genesislcap/web-core';
import { managementGridStyles } from '../../../styles/management.styles';

const LOG_ROUTE_MANAGEMENT_COLUMNS = [
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
];

@customElement({
  name: 'log-route-management',
  template: html<LogRouteManagement>`
    <div class="grid">
      <entity-management
        design-system-prefix="rapid"
        resourceName="ALL_LOG_ROUTES"
        createEvent="EVENT_LOG_ROUTE_CREATE"
        updateEvent="EVENT_LOG_ROUTE_UPDATE"
        deleteEvent="EVENT_LOG_ROUTE_DELETE"
        :columns=${() => [...LOG_ROUTE_MANAGEMENT_COLUMNS]}
      ></entity-management>
    </div>
  `,
  styles: managementGridStyles,
})
export class LogRouteManagement extends GenesisElement {}
