import { Connect } from '@genesislcap/foundation-comms';
import { DI } from '@genesislcap/web-core';
import type { Template } from '../components/foundation-notification-dashboard/components/templates/template-dialog/template-dialog.types';

export interface TemplateService {
  createRuleTemplate(ruleTemplate: Template): Promise<any>;
  editRuleTemplate(ruleTemplate: Template): Promise<any>;
  getRuleTemplates();
  getRuleTemplateDetails(dynamicRuleId: string);
  deleteRuleTemplate(dynamicRuleId: string);
}

class TemplateServiceImpl implements TemplateService {
  @Connect private connect: Connect;

  async createRuleTemplate(ruleTemplate: Template): Promise<any> {
    return this.connect.commitEvent('EVENT_NOTIFICATION_RULE_TEMPLATE_CREATE', {
      DETAILS: ruleTemplate,
      IGNORE_WARNINGS: true,
      VALIDATE: false,
    });
  }

  async editRuleTemplate(ruleTemplate: Template): Promise<any> {
    return this.connect.commitEvent('EVENT_NOTIFICATION_RULE_TEMPLATE_UPDATE', {
      DETAILS: ruleTemplate,
      IGNORE_WARNINGS: true,
      VALIDATE: false,
    });
  }

  public async getRuleTemplates() {
    let ruleTemplates = [];
    await this.connect.request('REQ_NOTIFICATION_RULE_TEMPLATE', { REQUEST: true }).then((data) => {
      ruleTemplates = data.REPLY;
    });
    return ruleTemplates;
  }

  public async getRuleTemplateDetails(dynamicRuleId: string) {
    let ruleTemplateDetails = {};
    await this.connect
      .request('REQ_NOTIFICATION_RULE_TEMPLATE_DETAILS', {
        REQUEST: {
          ID: dynamicRuleId,
        },
      })
      .then((data) => {
        if (!data.REPLY) {
          return;
        }

        ruleTemplateDetails = data.REPLY[0];
      });
    return ruleTemplateDetails;
  }

  deleteRuleTemplate(dynamicRuleId: string) {
    return this.connect.commitEvent('EVENT_NOTIFICATION_RULE_TEMPLATE_DELETE', {
      DETAILS: {
        ID: dynamicRuleId,
      },
      IGNORE_WARNINGS: true,
      VALIDATE: false,
    });
  }
}

export const TemplateService = DI.createInterface<TemplateService>((x) =>
  x.singleton(TemplateServiceImpl),
);
