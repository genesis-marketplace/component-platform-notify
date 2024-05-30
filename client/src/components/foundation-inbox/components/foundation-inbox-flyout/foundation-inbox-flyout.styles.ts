import { css } from '@genesislcap/web-core';
import { FoundationInboxStyles } from '../../inbox.styles';

export const FoundationInboxFlyoutStyles = css`
  rapid-flyout::part(flyout) {
    width: 30%;
    min-width: 320px;
    padding: 0;
  }

  rapid-flyout::part(content) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  ${FoundationInboxStyles}
`;
