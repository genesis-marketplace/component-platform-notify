import { customElement, GenesisElement, html } from '@genesislcap/web-core';
import { managementGridStyles } from '../../../styles/management.styles';

const MS_TEAMS_ROUTE_MANAGEMENT_COLUMNS = [
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
    field: 'URL',
    headerName: 'Url',
    flex: 1,
  },
];

@customElement({
  name: 'ms-teams-route-management',
  template: html<MSTeamsRouteManagement>`
    <div class="grid">
      <entity-management
        design-system-prefix="rapid"
        resourceName="ALL_MS_TEAMS_ROUTES"
        createEvent="EVENT_MS_TEAMS_ROUTE_CREATE"
        updateEvent="EVENT_MS_TEAMS_ROUTE_UPDATE"
        deleteEvent="EVENT_MS_TEAMS_ROUTE_DELETE"
        :columns=${() => [...MS_TEAMS_ROUTE_MANAGEMENT_COLUMNS]}
      ></entity-management>
    </div>
  `,
  styles: managementGridStyles,
})
export class MSTeamsRouteManagement extends GenesisElement {}
