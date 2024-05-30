import { foundationLayoutComponents } from '@genesislcap/foundation-layout';
import * as rapidDesignSystem from '@genesislcap/rapid-design-system';
import { FoundationInboxCounter } from './foundation-inbox/components/foundation-inbox-counter/foundation-inbox-counter';
import { InboxSubscription } from './foundation-inbox/components/inbox-subscription/inbox-subscription';
import { RuleParameter } from './foundation-inbox/components/inbox-subscription/rule-parameter/rule-parameter';
import { NotifyAuditManagement } from './foundation-notification-dashboard/components/notify-audit/notify-audit-management';
import { RouteManagement } from './foundation-notification-dashboard/components/routes/route-management';
import { EmailDistributionRouteManagement } from './foundation-notification-dashboard/components/routes/tabs/email-distribution-route-management';
import { EmailUserRouteManagement } from './foundation-notification-dashboard/components/routes/tabs/email-user-route-management';
import { LogRouteManagement } from './foundation-notification-dashboard/components/routes/tabs/log-route-management';
import { MSTeamsRouteManagement } from './foundation-notification-dashboard/components/routes/tabs/ms-teams-route-management';
import { ScreenRouteManagement } from './foundation-notification-dashboard/components/routes/tabs/screen-route-management';
import { RuleConditionBuilder } from './foundation-notification-dashboard/components/rules/rule-dialog/rule-condition-builder/rule-condition-builder';
import { RuleDialog } from './foundation-notification-dashboard/components/rules/rule-dialog/rule-dialog';
import { RuleManagement } from './foundation-notification-dashboard/components/rules/rule-management';
import { ParameterBuilder } from './foundation-notification-dashboard/components/templates/template-dialog/parameter-builder/parameter-builder';
import { TemplateConditionBuilder } from './foundation-notification-dashboard/components/templates/template-dialog/template-condition-builder/template-condition-builder';
import { TemplateDialog } from './foundation-notification-dashboard/components/templates/template-dialog/template-dialog';
import { TemplateManagement } from './foundation-notification-dashboard/components/templates/template-management';

// Components

// #region Inbox
FoundationInboxCounter;
InboxSubscription;
RuleParameter;
// #endregion Inbox

// #region Notification Dashboard
RuleManagement;
RuleDialog;
RuleConditionBuilder;

TemplateManagement;
TemplateDialog;
ParameterBuilder;
TemplateConditionBuilder;

RouteManagement;
ScreenRouteManagement;
EmailDistributionRouteManagement;
EmailUserRouteManagement;
LogRouteManagement;
MSTeamsRouteManagement;

NotifyAuditManagement;
// #endregion Notification Dashboard

export async function loadRemotes() {
  rapidDesignSystem
    .provideDesignSystem()
    .register(rapidDesignSystem.baseComponents, foundationLayoutComponents);
}
