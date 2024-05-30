import { sync } from '@genesislcap/foundation-utils';
import { html, ref, repeat, when } from '@genesislcap/web-core';
import { NotifyPermission } from '../../utils/notifyPermissions';
import type { FoundationInbox } from './inbox';
import { Alert, AlertStatus, InboxTab } from './inbox.types';
import { getFormattedDate, getIcon, getSeverityColor, getStatus } from './inbox.utils';

const alertsTab = (tab: InboxTab) => html<FoundationInbox>`
  <rapid-tab-panel
    slot="alert-log-tab"
    class="alert-log-tab-panel"
    id=${tab}
    style=${(x) => x.checkActiveTab(tab)}
  >
    <div class="search">
      <rapid-text-field
        class="search-input"
        :value=${sync((x) => x.searchAlertLog)}
        placeholder="Search Alerts..."
        autocomplete="off"
      ></rapid-text-field>
    </div>
    <div class="alert-log-tab-content">
      ${repeat(
        (x) => x.alertsFilter,
        html<Alert, FoundationInbox>`
          <div class="toast" id=${(x) => x.ALERT_ID}>
            <div
              class="toast-severity"
              style="background-color: ${(x) => getSeverityColor(x.NOTIFY_SEVERITY)}"
            ></div>
            <div class="toast-content">
              ${when(
                (x) => x.ALERT_STATUS === AlertStatus.NEW,
                html`
                  <div class="close-icon" @click=${(x, c) => c.parent.dismissAlert(x.ALERT_ID)}>
                    <rapid-icon name="times"></rapid-icon>
                  </div>
                `,
              )}
              <span class="toast-header">${(x) => x.HEADER}</span>
              <span class="toast-message">${(x) => x.MESSAGE}</span>
              <div class="toast-bottom" slot="bottom">
                <span class="toast-date" slot="date">${(x) => getFormattedDate(x.CREATED_AT)}</span>
              </div>
            </div>
          </div>
        `,
      )}
    </div>
  </rapid-tab-panel>
`;

/**
 * @public
 */
export const FoundationInboxTemplate = html<FoundationInbox>`
  <div class="inbox-content">
    <rapid-tabs ${ref('tabs')} appearance="secondary" activeid=${(x) => InboxTab.AlertsNew}>
      <rapid-tab
        slot="alert-log-tab"
        id=${InboxTab.AlertsNew}
        @click=${(x) => x.inboxTabChanged(InboxTab.AlertsNew)}
        appearance="secondary"
      >
        Alerts
      </rapid-tab>
      <rapid-tab
        slot="alert-log-tab"
        id=${InboxTab.AlertHistory}
        @click=${(x) => x.inboxTabChanged(InboxTab.AlertHistory)}
        appearance="secondary"
      >
        Alert History
      </rapid-tab>
      ${when(
        (x) => x.auth.currentUser?.hasPermission(NotifyPermission.NotificationRuleView),
        html`
          <rapid-tab
            slot="my-alerts-tab"
            id=${InboxTab.MyAlerts}
            @click=${(x) => x.inboxTabChanged(InboxTab.MyAlerts)}
            appearance="secondary"
          >
            Subscriptions
          </rapid-tab>
        `,
      )}
      ${when(
        (x) => x.auth.currentUser?.hasPermission(NotifyPermission.NotificationRuleTemplateView),
        html`
          <rapid-tab
            slot="subscribe-tab"
            id=${InboxTab.Subscribe}
            @click=${(x) => x.inboxTabChanged(InboxTab.Subscribe)}
            appearance="secondary"
          >
            Subscribe
          </rapid-tab>
        `,
      )}

      <!-- Alerts -->
      ${(_) => alertsTab(InboxTab.AlertsNew)} ${(_) => alertsTab(InboxTab.AlertHistory)}
      ${when(
        (x) => x.auth.currentUser?.hasPermission(NotifyPermission.NotificationRuleView),
        html`
          <!-- Rules -->
          <rapid-tab-panel
            slot="my-alerts-tab"
            class="my-alerts-tab-panel"
            id=${InboxTab.MyAlerts}
            style=${(x) => x.checkActiveTab(InboxTab.MyAlerts)}
          >
            <div class="my-alerts-tab-content">
              ${repeat(
                (x) => x.rulesFilter,
                html<any>`
                  <div class="rule">
                    <div class="rule-name">${(x) => x.NAME}</div>
                    <div class="rule-expression">${(x) => x.RULE_EXPRESSION}</div>
                    <div class="rule-datetime">${(x, c) => getFormattedDate(x.DATETIME)}</div>
                    <div class="rule-status-actions">
                      <div class="rule-status ${(x) => getStatus(x.RULE_STATUS).toLowerCase()}">
                        ${(x, c) => getStatus(x.RULE_STATUS)}
                      </div>
                      <div class="rule-actions">
                        <!--
                        <span class="rule-action-edit" @click=${(x, c) => c.parent.editAlert(x)}}>
                          <rapid-icon name="gear"></rapid-icon>
                        </span>
                      -->
                        <span
                          class="rule-action-enable-disable"
                          @click=${(x, c) => c.parent.playPauseAlert(x)}
                        >
                          <rapid-icon name=${(x) => getIcon(x.RULE_STATUS)}></rapid-icon>
                        </span>
                        <span
                          class="rule-action-delete"
                          @click=${(x, c) => c.parent.deleteAlert(x)}
                        >
                          <rapid-icon name="trash"></rapid-icon>
                        </span>
                      </div>
                    </div>
                  </div>
                `,
              )}
            </div>
          </rapid-tab-panel>
        `,
      )}
      ${when(
        (x) => x.auth.currentUser?.hasPermission(NotifyPermission.NotificationRuleTemplateView),
        html`
          <!-- Templates -->
          <rapid-tab-panel
            slot="subscribe-tab"
            class="subscribe-tab-panel"
            id=${InboxTab.Subscribe}
            style=${(x) => x.checkActiveTab(InboxTab.Subscribe)}
          >
            <div class="search">
              <rapid-text-field
                class="search-input"
                :value=${sync((x) => x.searchSubscribe)}
                placeholder="Search Templates..."
                autocomplete="off"
              ></rapid-text-field>
            </div>
            <div class="subscribe-tab-content">
              ${repeat(
                (x) => x.templatesFilter,
                html`
                  <div
                    class="template"
                    @click=${(x, c) => c.parent.openTemplateSubscription(x)}
                    id=${(x) => x.ID}
                  >
                    <div class="template-name">${(x) => x.NAME}</div>
                    <div class="template-description">${(x) => x.DESCRIPTION}</div>
                    <div class="template-datetime">${(x, c) => getFormattedDate(x.DATETIME)}</div>
                  </div>
                `,
              )}
            </div>
            ${when(
              (x) => x.ruleTemplateDetails,
              html`
                <inbox-subscription
                  :template=${(x) => x.ruleTemplateDetails}
                  @templateSubscribed=${(x) => (x.ruleTemplateDetails = null)}
                ></inbox-subscription>
              `,
            )}
          </rapid-tab-panel>
        `,
      )}
    </rapid-tabs>
  </div>
`;
