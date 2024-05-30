import { css } from '@genesislcap/web-core';

const BACKGROUND_COLOR = '#EF5547';

export const FoundationInboxCounterStyles = css`
  :host {
    display: inline-block;
    min-width: 16px;
    height: 16px;
    border-radius: 9px;
    text-align: center;
    color: #fff;
    line-height: 16px;
    font-size: 9px;
    font-weight: 700;
    background: ${BACKGROUND_COLOR};
    pointer-events: none;
  }

  .hidden {
    visibility: hidden;
  }
`;
