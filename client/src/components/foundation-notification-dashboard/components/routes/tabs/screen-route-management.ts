import { UiSchema } from '@genesislcap/foundation-forms';
import { customElement, GenesisElement, html } from '@genesislcap/web-core';
import { managementGridStyles } from '../../../styles/management.styles';

const SCREEN_ROUTE_MANAGEMENT_COLUMNS = [
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
    field: 'EXCLUDE_SENDER',
    headerName: 'Exclude Sender',
    flex: 1,
  },
];

const SCREEN_ROUTE_MANAGEMENT_UI_SCHEMA: UiSchema = {
  type: 'VerticalLayout',
  elements: [
    { type: 'Control', scope: '#/properties/ENTITY_ID', label: 'ENTITY ID' },
    { type: 'Control', scope: '#/properties/ENTITY_ID_TYPE', label: 'ENTITY ID TYPE' },
    { type: 'Control', scope: '#/properties/EXCLUDE_SENDER', label: 'EXCLUDE SENDER' },
    { type: 'Control', scope: '#/properties/GATEWAY_ID', label: 'GATEWAY ID' },
    { type: 'Control', scope: '#/properties/TOPIC_MATCH', label: 'TOPIC MATCH' },
  ],
};

@customElement({
  name: 'screen-route-management',
  template: html<ScreenRouteManagement>`
    <div class="grid">
      <entity-management
        design-system-prefix="rapid"
        resourceName="ALL_SCREEN_ROUTES"
        createEvent="EVENT_SCREEN_NOTIFY_ROUTE_CREATE"
        updateEvent="EVENT_SCREEN_NOTIFY_ROUTE_UPDATE"
        deleteEvent="EVENT_SCREEN_NOTIFY_ROUTE_DELETE"
        :columns=${() => [...SCREEN_ROUTE_MANAGEMENT_COLUMNS]}
        :createFormUiSchema=${(x) => SCREEN_ROUTE_MANAGEMENT_UI_SCHEMA}
        :updateFormUiSchema=${(x) => SCREEN_ROUTE_MANAGEMENT_UI_SCHEMA}
      ></entity-management>
    </div>
  `,
  styles: managementGridStyles,
})
export class ScreenRouteManagement extends GenesisElement {}
