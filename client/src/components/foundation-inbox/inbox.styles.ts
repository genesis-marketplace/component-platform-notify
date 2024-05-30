import { css } from '@genesislcap/web-core';
/**
 * @public
 */
export const FoundationInboxStyles = css`
  .inbox {
    width: 100%;
    height: 100%;
    background: var(--neutral-layer-4);
    display: flex;
    flex-direction: column;
  }

  /* Tabs */
  rapid-tabs {
    display: flex;
    flex-direction: column;
    width: 100%;
  }

  rapid-tabs::part(tablist) {
    grid-template-columns: none;
    flex: 0;
    width: 100%;
  }

  rapid-tabs::part(tabpanel) {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
  }

  /* Search */
  .search {
    padding: calc(var(--design-unit) * 3px) calc(var(--design-unit) * 3px);
    border-bottom: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
    flex: 0;
  }

  .search-input {
    display: flex;
  }

  .search-input::part(root) {
    width: 100%;
  }

  /* Inbox Header */
  .inbox-header {
    display: flex;
    flex: 0;
    align-items: center;
    justify-content: space-between;
    padding: calc(var(--design-unit) * 3px) calc(var(--design-unit) * 3px);
    border-bottom: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
  }

  .inbox-header-title {
    font-size: calc(var(--design-unit) * 4px);
    font-weight: 700;
    padding: 0;
    margin: 0 auto 0 0;
  }

  .inbox-header-close {
    text-align: center;
    min-width: 30px;
    height: 30px;
  }

  .inbox-header-close::part(control) {
    padding: 0;
    margin: 0;
  }

  .inbox-header-close::part(start) {
    margin-inline-end: 0;
  }

  /* Inbox Content */
  .inbox-content {
    flex: 1;
    min-height: 0;
    display: flex;
    height: calc(100% - 54px);
  }

  /* Alert Log */
  .alert-log-tab-panel {
    display: flex;
    flex-direction: column;
    min-height: 0;
    flex: 1;
  }

  .alert-log-tab-content {
    flex: 1;
    overflow: auto;
  }

  .toast {
    display: flex;
    flex-direction: row;
    border-bottom: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
  }

  .toast-severity {
    width: calc(var(--design-unit) * 1px);
    background-color: white;
    margin: calc(var(--design-unit) * 2px) calc(var(--design-unit) * 0px)
      calc(var(--design-unit) * 2px) calc(var(--design-unit) * 2px);
  }

  .toast-header {
    font-weight: bold;
  }

  .toast-bottom {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .toast-content {
    display: flex;
    position: relative;
    flex-direction: column;
    width: 100%;
    margin: calc(var(--design-unit) * 2px) calc(var(--design-unit) * 0px)
      calc(var(--design-unit) * 2px) calc(var(--design-unit) * 2px);
  }

  .toast-date {
    color: var(--neutral-foreground-hint);
    font-size: calc(var(--design-unit) * 3px);
  }

  /* My Alerts */
  .my-alerts-tab-panel {
    display: flex;
    flex-direction: column;
    min-height: 0;
    flex: 1;
  }

  .my-alerts-tab-content {
    flex: 1;
    overflow: auto;
  }

  /* Subscribe */
  .subscribe-tab-panel {
    display: flex;
    flex-direction: column;
    min-height: 0;
    flex: 1;
  }

  .subscribe-tab-content {
    flex: 1;
    overflow: auto;
  }

  /* Rule and Template */
  .template {
    cursor: pointer;
  }

  .rule,
  .template {
    border-bottom: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
    padding: calc(var(--design-unit) * 2px);
  }

  .rule-status-actions {
    display: flex;
    flex-direction: row;
    gap: calc(var(--design-unit) * 2px);
  }

  .rule:hover,
  .template:hover {
    background-color: #2e3034;
  }

  .rule-actions {
    margin-left: auto;
    display: flex;
    gap: calc(var(--design-unit) * 3px);
    justify-content: space-between;
    visibility: hidden;
  }

  .rule:hover > .rule-status-actions > .rule-actions {
    visibility: visible;
  }

  .active {
    color: #7acc79;
  }

  .paused {
    color: #ffb660;
  }

  .rule-datetime,
  .template-datetime {
    color: #879ba6;
  }

  .rule-action-edit,
  .rule-action-enable-disable,
  .rule-action-delete {
    cursor: pointer;
  }

  .close-icon {
    position: absolute;
    right: calc(var(--design-unit) * 2px);
    cursor: pointer;
  }
`;
