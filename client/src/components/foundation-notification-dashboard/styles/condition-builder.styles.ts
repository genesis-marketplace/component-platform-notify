import { css } from '@genesislcap/web-core';

export const ConditionBuilderStyles = css`
  .condition-builder {
    display: flex;
    gap: calc(var(--design-unit) * 2px);
    margin-bottom: calc(var(--design-unit) * 2px);
  }

  rapid-button {
    margin: 0;
  }

  .delete {
    margin: 0;
    display: flex;
    justify-content: center;
  }

  rapid-text-field {
    width: 100%;
  }

  rapid-select {
    min-width: auto;
  }

  rapid-select.left-value::part(control) {
    width: 250px;
  }

  rapid-select.logical-operator::part(control) {
    width: 160px;
  }

  .logical-operator-null-and-blank {
    width: 100%;
  }

  rapid-select.right-criteria::part(control) {
    width: 135px;
  }

  rapid-select.right-criteria-user-entry {
    width: 100%;
  }

  rapid-select.right-criteria-select {
    width: 100%;
    min-width: 0;
  }

  rapid-select::part(listbox) {
    max-height: 160px;
  }
`;
