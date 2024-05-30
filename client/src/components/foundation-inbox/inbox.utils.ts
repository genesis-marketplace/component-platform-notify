import {
  SEVERITY_CRITICAL,
  SEVERITY_INFORMATION,
  SEVERITY_SERIOUS,
  SEVERITY_WARNING,
} from '../foundation-notification-dashboard/types/severity';

export const getStatus = (ruleStatus) => (ruleStatus == 'ENABLED' ? 'Active' : 'Paused');

export const getIcon = (ruleStatus) => (ruleStatus === 'ENABLED' ? 'pause' : 'play');

export const getFormattedDate = (dateTimeInMills) => {
  const dateTime = new Date(dateTimeInMills);
  return `${dateTime.toLocaleDateString()} ${dateTime.toLocaleTimeString()}`;
};

export function getSeverityColor(severity: string) {
  switch (severity.toUpperCase()) {
    case SEVERITY_INFORMATION:
      return 'var(--accent-fill-rest)';
    case SEVERITY_CRITICAL:
      return 'var(--warning-color)';
    case SEVERITY_SERIOUS:
      return 'var(--error-color)';
    case SEVERITY_WARNING:
      return 'var(--sell-color)';
    default:
      console.error('Unexpected severity:', severity);
      return 'var(--neutral-foreground-rest)';
  }
}
