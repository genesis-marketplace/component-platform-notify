export class NotificationDashboardTab {
  id: string;
  name: string;
  component: string;
  right: string;
}

export const NOTIFICATION_DASHBOARD_TABS: NotificationDashboardTab[] = [
  {
    id: 'rules',
    name: 'Rules',
    component: 'rule-management',
    right: 'NotificationRuleView',
  },
  {
    id: 'templates',
    name: 'Templates',
    component: 'template-management',
    right: 'NotificationRuleTemplateView',
  },
  {
    id: 'routes',
    name: 'Routes',
    component: 'route-management',
    right: 'NotificationRouteView',
  },
  /* TODO: Not needed at the moment
  {
    id: 'notify-audit',
    name: 'Notify Audit',
    component: 'notify-audit-management',
    right: 'NotificationAuditView',
  },
  */
];
