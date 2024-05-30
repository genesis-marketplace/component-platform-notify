import { Connect, Message } from '@genesislcap/foundation-comms';
import { DI } from '@genesislcap/web-core';
import type { Rule } from '../components/foundation-notification-dashboard/components/rules/rule-dialog/rule-dialog.types';

export interface RuleService {
  subscribeRule(dynamicRuleId: string, parameterDetails: any);
  unsubscribeRule(dynamicRuleId: string);
  enableRule(dynamicRuleId: string);
  disableRule(dynamicRuleId: string);
  getRules(): Promise<any>;
  getRuleDetails(ruleId: string): Promise<Rule>;
  createRule(rule: Rule): Promise<Message>;
  updateRule(rule: Rule): Promise<Message>;
  deleteRule(ruleId: string): void;
}

class RuleServiceImpl implements RuleService {
  @Connect private connect: Connect;

  public async getRules() {
    const response = await this.connect.request('REQ_NOTIFICATION_RULE', { REQUEST: true });
    return response.REPLY;
  }

  public async getRuleDetails(ruleId: string): Promise<Rule> {
    const response = await this.connect.request('REQ_NOTIFICATION_RULE_DETAILS', {
      REQUEST: {
        ID: ruleId,
      },
    });

    if (!response.REPLY) {
      return;
    }

    return response.REPLY[0];
  }

  async createRule(rule: Rule): Promise<Message> {
    return this.connect.commitEvent('EVENT_NOTIFICATION_RULE_CREATE', {
      DETAILS: rule,
      IGNORE_WARNINGS: true,
      VALIDATE: false,
    });
  }

  async updateRule(rule: Rule): Promise<Message> {
    return this.connect.commitEvent('EVENT_NOTIFICATION_RULE_UPDATE', {
      DETAILS: rule,
      IGNORE_WARNINGS: true,
      VALIDATE: false,
    });
  }

  deleteRule(ruleId: string) {
    this.connect.commitEvent('EVENT_NOTIFICATION_RULE_DELETE', {
      DETAILS: {
        ID: ruleId,
      },
      IGNORE_WARNINGS: true,
      VALIDATE: false,
    });
  }

  public async subscribeRule(dynamicRuleId: string, parameterDetails: unknown) {
    if (!dynamicRuleId) {
      console.error('Invalid dynamicRuleId');
      return;
    }

    return this.connect.commitEvent('EVENT_NOTIFICATION_RULE_SUBSCRIBE', {
      DETAILS: {
        DYNAMIC_RULE_ID: dynamicRuleId,
        PARAMETER_DETAILS: parameterDetails,
      },
      IGNORE_WARNINGS: true,
      VALIDATE: false,
    });
  }

  public async unsubscribeRule(dynamicRuleId: string) {
    if (!dynamicRuleId) {
      console.error('Invalid dynamicRuleId');
      return;
    }

    return this.connect.commitEvent(
      'EVENT_NOTIFICATION_RULE_UNSUBSCRIBE',
      this.createDynamicRuleIDParams(dynamicRuleId),
    );
  }

  public async enableRule(dynamicRuleId: string) {
    if (!dynamicRuleId) {
      console.error('Invalid dynamicRuleId');
      return;
    }

    return this.connect.commitEvent(
      'EVENT_NOTIFICATION_RULE_ENABLE',
      this.createDynamicRuleIDParams(dynamicRuleId),
    );
  }

  public async disableRule(dynamicRuleId: string) {
    if (!dynamicRuleId) {
      console.error('Invalid dynamicRuleId');
      return;
    }

    return this.connect
      .commitEvent('EVENT_NOTIFICATION_RULE_DISABLE', this.createDynamicRuleIDParams(dynamicRuleId))
      .then((data) => {
        console.log(data);
      });
  }

  private createDynamicRuleIDParams(dynamicRuleId: string) {
    return {
      DETAILS: {
        ID: dynamicRuleId,
      },
      IGNORE_WARNINGS: true,
      VALIDATE: false,
    };
  }
}

export const RuleService = DI.createInterface<RuleService>((x) => x.singleton(RuleServiceImpl));
