import { css } from '@genesislcap/web-core';

export const DynamicRuleManagementStyles = css`
  .container {
    display: flex;
    height: 100%;
    flex-direction: column;
  }

  .header {
    display: flex;
    flex-direction: row-reverse;
  }

  .grid {
    height: 100%;
  }
`;

// #region Dialog
const modalWidthPx = 940;

const top = css`
  rapid-modal::part(top) {
    padding: calc(var(--design-unit) * 4px);
    border-bottom: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
    margin: 0;
  }

  .dialog-top {
    font-weight: bold;
    align-self: center;
  }
`;

const bottom = css`
  rapid-modal::part(bottom) {
    padding: calc(var(--design-unit) * 4px);
    border-top: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
    margin: 0;
  }

  .dialog-bottom {
    display: flex;
  }
`;

const textField = css`
  rapid-text-field {
    display: flex;
    flex-direction: column;
    gap: calc(var(--design-unit) * 2px);
  }

  rapid-text-field::part(label) {
    margin: 0;
    color: var(--neutral-foreground-rest);
  }

  rapid-text-field::part(label)::after {
    content: '*';
  }
`;

const select = css`
  rapid-select,
  rapid-multiselect::part(root) {
    min-width: 0;
  }

  rapid-select::part(listbox) {
    max-height: 160px;
  }

  rapid-multiselect {
    max-height: 40px;
  }
`;

export const DynamicRuleDialogStyles = css`
  ${top}
  ${bottom}

  rapid-modal::part(dialog) {
    padding: 0;
    background: var(--neutral-layer-4);
    border: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
  }

  .dialog-content {
    display: flex;
    flex-direction: column;
    width: ${String(modalWidthPx)}px;
    max-height: 500px;
    overflow-y: scroll;
    gap: calc(var(--design-unit) * 3px);
    padding: calc(var(--design-unit) * 4px);
  }

  .content-row {
    display: flex;
    gap: calc(var(--design-unit) * 3px);
  }

  .control {
    display: flex;
    flex-direction: column;
    width: 100%;
    gap: calc(var(--design-unit) * 2px);
  }

  .items {
    border: calc(var(--stroke-width) * 1px) solid var(--neutral-stroke-divider-rest);
    border-radius: calc(var(--control-corner-radius) * 1px);
    display: flex;
    padding: calc(var(--design-unit) * 2px);
    flex-direction: column;
  }

  .item {
    gap: 0;
    flex-direction: column;
    margin-bottom: 0;
  }

  ${textField}
  ${select}

  rapid-button {
    margin: 0;
  }

  rapid-select {
    min-width: 0;
  }

  rapid-select::part(listbox) {
    max-height: 160px;
  }

  .horizontal {
    flex-direction: row;
  }

  .destination {
    align-self: center;
  }

  .topic {
    min-width: revert-layer;
  }

  ::-webkit-scrollbar {
    width: calc((var(--base-height-multiplier) + var(--design-unit)) * 1px);
  }

  ::-webkit-scrollbar-track {
    background: var(--neutral-layer-1);
  }

  ::-webkit-scrollbar-thumb {
    background: var(--neutral-fill-rest);
  }
`;
// #endregion
