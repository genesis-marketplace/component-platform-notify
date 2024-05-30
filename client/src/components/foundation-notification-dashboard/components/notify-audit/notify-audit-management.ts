import { css, customElement, GenesisElement, html } from '@genesislcap/web-core';
import { managementGridStyles } from '../../styles/management.styles';

const NOTIFY_AUDIT_MANAGEMENT_COLUMNS = [
  {
    field: 'ALERT_ID',
    headerName: 'Alert',
    flex: 1,
  },
  {
    field: 'MESSAGE',
    headerName: 'Message',
    flex: 1,
  },
  {
    field: 'HEADER',
    headerName: 'Header',
    flex: 1,
  },
  {
    field: 'USER_NAME',
    headerName: 'User Name',
    flex: 1,
  },
  {
    field: 'EXPIRY',
    headerName: 'Expiry',
    flex: 1,
  },
  {
    field: 'NOTIFY_SEVERITY',
    headerName: 'Notify Serverity',
    flex: 1,
  },
  {
    field: 'CREATED_AT',
    headerName: 'Created At',
    flex: 1,
  },
  {
    field: 'TABLE_ENTITY_ID',
    headerName: 'Table Entity Id',
    flex: 1,
  },
  {
    field: 'TOPIC',
    headerName: 'Topic',
    flex: 1,
  },
  {
    field: 'NOTIFY_ALERT_AUDIT_ID',
    headerName: 'Table Entity Id',
    flex: 1,
  },
  {
    field: 'AUDIT_EVENT_TYPE',
    headerName: 'Audit Event Type',
    flex: 1,
  },
  {
    field: 'AUDIT_EVENT_DATETIME',
    headerName: 'Audit Event Datetime',
    flex: 1,
  },
  {
    field: 'AUDIT_EVENT_TEXT',
    headerName: 'Audit Event Text',
    flex: 1,
  },
  {
    field: 'AUDIT_EVENT_USER',
    headerName: 'Audit Event User',
    flex: 1,
  },
];

@customElement({
  name: 'notify-audit-management',
  template: html<NotifyAuditManagement>`
    <div class="grid">
      <entity-management
        design-system-prefix="rapid"
        resourceName="ALL_NOTIFY_AUDIT"
        :columns=${() => [...NOTIFY_AUDIT_MANAGEMENT_COLUMNS]}
      ></entity-management>
    </div>
  `,
  styles: css`
    ${managementGridStyles}

    entity-management::part(header) {
      display: none;
    }
  `,
})
export class NotifyAuditManagement extends GenesisElement {}
