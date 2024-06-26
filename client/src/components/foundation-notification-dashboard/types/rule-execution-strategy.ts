export const RULE_EXECUTION_UNLIMITED = 'UNLIMITED';
export const RULE_EXECUTION = 'ONCE_ONLY';

export const ALL_RULE_EXECUTION_STRATEGIES = [RULE_EXECUTION_UNLIMITED, RULE_EXECUTION] as const;

export type RuleExecutionStrategy = (typeof ALL_RULE_EXECUTION_STRATEGIES)[number];
