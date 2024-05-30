import { html, when } from '@genesislcap/web-core';
import type { FoundationInboxCounter } from './foundation-inbox-counter';

const MAX_NUMBER_TO_DISPLAY = 99;

export const FoundationInboxCounterTemplate = html<FoundationInboxCounter>`
  ${when(
    (x) => x.value > 0,
    html`
      ${(x) => (x.value > MAX_NUMBER_TO_DISPLAY ? `${MAX_NUMBER_TO_DISPLAY}+` : x.value)}
    `,
  )}
`;
