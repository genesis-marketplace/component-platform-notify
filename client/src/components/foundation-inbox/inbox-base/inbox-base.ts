import { Auth } from '@genesislcap/foundation-comms';
import type { Tabs } from '@genesislcap/foundation-ui';
import { GenesisElement, observable } from '@genesislcap/web-core';
import { AlertService } from '../../../services/alert.service';
import { FoundationInboxService } from '../../../services/inbox.service';
import { RuleService } from '../../../services/rule.service';
import { TemplateService } from '../../../services/template.service';
import { logger } from '../../../utils';
import {
  Alert,
  AlertStatus,
  InboxTab,
  NotificationRuleTemplateReply,
  Rule,
  RuleStatus,
  RuleTemplate,
} from '../inbox.types';

export class FoundationInboxBase extends GenesisElement {
  @Auth auth: Auth;

  @AlertService alertService: AlertService;
  @FoundationInboxService inboxService: FoundationInboxService;
  private alertSubscription: (any) => void;
  @observable private alerts: Alert[] = [];
  @observable alertsFilter: Alert[] = [];

  @RuleService ruleService: RuleService;
  @observable rules: Rule[] = [];
  @observable rulesFilter: Rule[] = [];

  @TemplateService templateService: TemplateService;
  @observable templates: RuleTemplate[] = [];
  @observable templatesFilter: RuleTemplate[] = [];

  @observable searchSubscribe: string = '';
  @observable ruleTemplateDetails: NotificationRuleTemplateReply = null;
  @observable selectedTab: InboxTab = InboxTab.AlertsNew;

  public tabs: Tabs;

  async connectedCallback() {
    super.connectedCallback();
    this.alertSubscription = ({ store }) => {
      this.alerts = store?.items || [];
      this.filterAlerts();
    };
    this.inboxService.subscribe(this.alertSubscription);
    this.rules = await this.ruleService.getRules();
    this.templates = await this.templateService.getRuleTemplates();
  }

  disconnectedCallback() {
    super.disconnectedCallback();
    this.inboxService.unsubscribe(this.alertSubscription);
  }

  // #region Tabs
  checkActiveTab(inboxTab: InboxTab) {
    return this.tabs.activeid === inboxTab.toString() ? '' : 'display: none;';
  }

  async inboxTabChanged(inboxTab: InboxTab) {
    this.selectedTab = inboxTab;
    this.ruleTemplateDetails = null;

    switch (inboxTab) {
      case InboxTab.AlertsNew:
      case InboxTab.AlertHistory:
        this.filterAlerts();
        break;
      case InboxTab.MyAlerts:
        this.rules = await this.ruleService.getRules();
        break;
      case InboxTab.Subscribe:
        this.templates = await this.templateService.getRuleTemplates();
        break;
      default:
        logger.error('Unexpected inboxTab:', inboxTab);
        break;
    }
  }
  // #endregion

  // #region Changed Events
  alertsChanged = () => (this.alertsFilter = this.alerts);

  rulesChanged = () => (this.rulesFilter = this.rules);

  templatesChanged = () => (this.templatesFilter = this.templates);

  @observable searchAlertLog: string = '';
  searchAlertLogChanged = () => {
    this.filterAlerts();
  };

  private filterAlerts() {
    this.alertsFilter = this.alerts.filter(
      (alert) =>
        (!this.searchAlertLog ||
          alert.HEADER.toLowerCase().startsWith(this.searchAlertLog.toLowerCase())) &&
        ((this.selectedTab === InboxTab.AlertsNew && alert.ALERT_STATUS === AlertStatus.NEW) ||
          (this.selectedTab === InboxTab.AlertHistory && alert.ALERT_STATUS !== AlertStatus.NEW)),
    );
  }

  searchSubscribeChanged = () => {
    if (!this.searchSubscribe) {
      this.templatesFilter = this.templates;
      return;
    }

    this.templatesFilter = this.templates.filter((item) =>
      item.NAME.toLowerCase().startsWith(this.searchSubscribe.toLowerCase()),
    );
  };

  // #endregion

  // #region New and History
  dismissAlert(alertId: string) {
    this.alertService.dismissNotifyAlert(alertId);
  }
  // #endregion

  // #region Subscriptions
  // TODO:
  editAlert() {}

  async playPauseAlert(ruleClicked: Rule) {
    switch (ruleClicked.RULE_STATUS) {
      case RuleStatus.ENABLED:
        await this.ruleService.disableRule(ruleClicked.ID);
        break;
      case RuleStatus.DISABLED:
        await this.ruleService.enableRule(ruleClicked.ID);
        break;
      default:
        logger.error('Unexpected ruleStatus:', ruleClicked.RULE_STATUS);
        break;
    }

    this.rules = await this.ruleService.getRules();
  }

  async deleteAlert(ruleClicked: Rule) {
    await this.ruleService.unsubscribeRule(ruleClicked.ID);
    this.rules = await this.ruleService.getRules();
  }
  // #endregion

  // #region Subscribe
  async openTemplateSubscription(ruleTemplate: any) {
    if (this.ruleTemplateDetails) {
      // Close
      this.ruleTemplateDetails = null;
      return;
    }

    // Open
    this.ruleTemplateDetails = await this.templateService.getRuleTemplateDetails(ruleTemplate.ID);
  }
  // #endregion
}
