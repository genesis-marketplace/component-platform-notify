import { css } from '@genesislcap/web-core';

export const InboxSubscriptionStyles = css`
  .inbox-subscription {
    display: flex;
    flex-direction: column;
  }

  .inbox-subscription-header {
    border-top: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
    border-bottom: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
    padding: calc(var(--design-unit) * 2px);
  }

  .inbox-subscription-content {
    display: flex;
    flex-direction: column;
    gap: calc(var(--design-unit) * 2px);
    overflow: auto;
    max-height: calc(var(--design-unit) * 80px);
    border: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
    margin: calc(var(--design-unit) * 2px);
    padding: calc(var(--design-unit) * 2px);
    border-radius: calc(var(--control-corner-radius) * 1px);
  }

  .inbox-subscription-footer {
    padding: calc(var(--design-unit) * 2px);
    border-top: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
  }

  rapid-button.subscribe {
    display: flex;
    margin: 0;
    width: 100%;
  }
`;
