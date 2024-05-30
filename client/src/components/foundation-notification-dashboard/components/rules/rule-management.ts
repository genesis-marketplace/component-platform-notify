import { showNotificationDialog } from '@genesislcap/foundation-notifications';
import { customElement, GenesisElement, html, ref, repeat } from '@genesislcap/web-core';
import { RuleService } from '../../../../services/rule.service';
import { DynamicRuleManagementStyles } from '../../styles/dynamic-rule.styles';
import { RULE_MANAGEMENT_COLUMNS } from './columns';
import { RuleDialog } from './rule-dialog/rule-dialog';
import { RuleDialogMode } from './rule-dialog/rule-dialog.types';

@customElement({
  name: 'rule-management',
  template: html<RuleManagement>`
    <div class="container">
      <div class="header">
        <rapid-button class="add" @click=${(x) => x.openRuleDialog()}>
          <rapid-icon name="plus" size="lg" variant="solid"></rapid-icon>
          Add
        </rapid-button>
      </div>

      <div class="grid">
        <rapid-grid-pro only-template-col-defs>
          <grid-pro-genesis-datasource resource-name="ALL_DYNAMIC_NOTIFY_RULES">
            ${repeat(
              (x) => RULE_MANAGEMENT_COLUMNS(x),
              html`
                <grid-pro-column :definition=${(x) => x} />
              `,
            )}
          </grid-pro-genesis-datasource>
        </rapid-grid-pro>
      </div>
    </div>

    <rule-dialog ${ref('ruleDialog')}></rule-dialog>
  `,
  styles: DynamicRuleManagementStyles,
})
export class RuleManagement extends GenesisElement {
  @RuleService ruleService: RuleService;
  ruleDialog: RuleDialog;

  openRuleDialog() {
    this.ruleDialog.openDialog({ mode: RuleDialogMode.CREATE });
  }

  async editRule(row) {
    const rule = await this.ruleService.getRuleDetails(row.ID);
    this.ruleDialog.openDialog({ mode: RuleDialogMode.EDIT, data: rule });
  }

  deleteRule(rule) {
    showNotificationDialog(
      {
        title: 'Confirm Delete',
        body: 'Do you really want to delete this rule?',
        dialog: {
          dismissingAction: {
            label: 'Dismiss',
            action: () => null,
          },
          confirmingActions: [
            {
              label: 'Confirm',
              action: () => this.ruleService.deleteRule(rule.ID),
            },
          ],
        },
      },
      'rapid',
    );
  }
}
