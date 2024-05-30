import { css, customElement, GenesisElement, html } from '@genesislcap/web-core';

@customElement({
  name: 'route-management',
  template: html<RouteManagement>`
    <rapid-tabs appearance="secondary">
      <rapid-tab appearance="secondary">Screen Routes</rapid-tab>
      <rapid-tab appearance="secondary">Email Distribution Routes</rapid-tab>
      <rapid-tab appearance="secondary">Email User Routes</rapid-tab>
      <rapid-tab appearance="secondary">Log Routes</rapid-tab>
      <rapid-tab appearance="secondary">MS Teams Routes</rapid-tab>

      <rapid-tab-panel>
        <screen-route-management></screen-route-management>
      </rapid-tab-panel>
      <rapid-tab-panel>
        <email-distribution-route-management></email-distribution-route-management>
      </rapid-tab-panel>
      <rapid-tab-panel>
        <email-user-route-management></email-user-route-management>
      </rapid-tab-panel>
      <rapid-tab-panel>
        <log-route-management></log-route-management>
      </rapid-tab-panel>
      <rapid-tab-panel>
        <ms-teams-route-management></ms-teams-route-management>
      </rapid-tab-panel>
    </rapid-tabs>
  `,
  styles: css`
    rapid-tabs {
      display: flex;
      flex-direction: column;
      width: 100%;
    }

    rapid-tabs::part(tablist) {
      grid-template-columns: none;
      flex: 0;
      align-self: start;
    }

    rapid-tabs::part(tabpanel) {
      display: flex;
      flex-direction: column;
      flex: 1;
      min-height: 0;
    }

    rapid-tabs,
    rapid-tab-panel {
      height: 100%;
    }
  `,
})
export class RouteManagement extends GenesisElement {}
