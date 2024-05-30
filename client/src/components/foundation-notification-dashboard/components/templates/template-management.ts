import { showNotificationDialog } from '@genesislcap/foundation-notifications';
import { customElement, GenesisElement, html, ref, repeat } from '@genesislcap/web-core';
import { TemplateService } from '../../../../services/template.service';
import { DynamicRuleManagementStyles } from '../../styles/dynamic-rule.styles';
import { TEMPLATE_MANAGEMENT_COLUMNS } from './columns';
import type { TemplateDialog } from './template-dialog/template-dialog';
import { TemplateDialogMode } from './template-dialog/template-dialog.types';

@customElement({
  name: 'template-management',
  template: html<TemplateManagement>`
    <div class="container">
      <div class="header">
        <rapid-button class="add" @click=${(x) => x.openTemplateDialog()}>
          <rapid-icon name="plus" size="lg" variant="solid"></rapid-icon>
          Add
        </rapid-button>
      </div>

      <div class="grid">
        <rapid-grid-pro only-template-col-defs>
          <grid-pro-genesis-datasource resource-name="ALL_RULE_TEMPLATES">
            ${repeat(
              (x) => TEMPLATE_MANAGEMENT_COLUMNS(x),
              html`
                <grid-pro-column :definition=${(x) => x} />
              `,
            )}
          </grid-pro-genesis-datasource>
        </rapid-grid-pro>
      </div>
    </div>

    <template-dialog ${ref('templateDialog')}></template-dialog>
  `,
  styles: DynamicRuleManagementStyles,
})
export class TemplateManagement extends GenesisElement {
  @TemplateService templateService: TemplateService;
  templateDialog: TemplateDialog;

  openTemplateDialog() {
    this.templateDialog.openDialog({ mode: TemplateDialogMode.CREATE });
  }

  async editTemplate(row) {
    const template = await this.templateService.getRuleTemplateDetails(row.ID);
    this.templateDialog.openDialog({ mode: TemplateDialogMode.EDIT, data: template });
  }

  deleteTemplate(template) {
    showNotificationDialog(
      {
        title: 'Confirm Delete',
        body: 'Do you really want to delete this template?',
        dialog: {
          dismissingAction: {
            label: 'Dismiss',
            action: () => null,
          },
          confirmingActions: [
            {
              label: 'Confirm',
              action: () => this.templateService.deleteRuleTemplate(template.ID),
            },
          ],
        },
      },
      'rapid',
    );
  }
}
