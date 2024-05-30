import { Auth } from '@genesislcap/foundation-comms';
import { html, repeat, when } from '@genesislcap/web-core';
import type { FoundationNotificationDashboard } from './notification-dashboard';
import {
  NOTIFICATION_DASHBOARD_TABS,
  NotificationDashboardTab,
} from './notification-dashboard.tabs';

export const FoundationNotificationDashboardTemplate = html<FoundationNotificationDashboard>`
  <div class="notification-dashboard">
    <rapid-tabs class="tabs">
      <!-- Tabs -->
      ${repeat(
        (_) => NOTIFICATION_DASHBOARD_TABS,
        html<NotificationDashboardTab>`
          ${when(
            (x, c) => (c.parent.auth as Auth).currentUser.hasPermission(x.right),
            html<NotificationDashboardTab>`
              <rapid-tab slot="${(x) => x.id}-tab">${(x) => x.name}</rapid-tab>
            `,
          )}
        `,
      )}

      <!-- Panels -->
      ${repeat(
        (_) => NOTIFICATION_DASHBOARD_TABS,
        html<NotificationDashboardTab>`
          ${when(
            (x, c) => (c.parent.auth as Auth).currentUser.hasPermission(x.right),
            html`
              ${
                /* HTML Render */
                (x) =>
                  html<NotificationDashboardTab>`
                  <rapid-tab-panel slot="${x.id}-tab" id="${x.id}-tab-panel">
                    <${x.component}></${x.component}>
                  </rapid-tab-panel>
                `
              }
            `,
          )}
        `,
      )}
    </rapid-tabs>
  </div>
`;
