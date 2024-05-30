import { css } from '@genesislcap/web-core';

const PARAM_SOURCE_WIDTH = '200';

export const ParameterBuilderStyles = css`
  .parameter-builder {
    display: flex;
    gap: calc(var(--design-unit) * 2px);
    margin-bottom: calc(var(--design-unit) * 2px);
  }

  rapid-button {
    margin: 0;
  }

  .delete {
    margin: 0;
    background-color: var(--neutral-layer-1);
    display: flex;
    justify-content: center;
  }

  .delete:hover {
    background-color: color-mix(in srgb, var(--neutral-foreground-rest), transparent 89%);
  }

  rapid-text-field {
    width: 100%;
  }

  rapid-select {
    min-width: auto;
  }

  rapid-select.param-type::part(control) {
    width: 115px;
  }

  rapid-select.param-source-type::part(control) {
    width: 160px;
  }

  rapid-select.param-source::part(control) {
    width: ${PARAM_SOURCE_WIDTH}px;
  }

  rapid-text-field.param-source::part(root) {
    width: calc(${PARAM_SOURCE_WIDTH}px + 2px);
  }

  rapid-select::part(listbox) {
    max-height: 160px;
  }
`;
