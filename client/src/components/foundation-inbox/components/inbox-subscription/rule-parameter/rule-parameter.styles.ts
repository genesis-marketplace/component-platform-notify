import { css } from '@genesislcap/web-core';

export const RuleParameterStyles = css`
  .control {
    display: flex;
    flex-direction: column;
    width: 100%;
  }

  label {
    margin-bottom: calc(var(--design-unit) * 2px);
  }

  .input-field {
    display: flex;
  }

  .input-field::part(root) {
    width: 100%;
  }

  rapid-select::part(listbox) {
    max-height: 160px;
  }
`;
