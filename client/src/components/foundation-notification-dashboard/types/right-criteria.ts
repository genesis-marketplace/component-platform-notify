export const RIGHT_CRITERIA_VALUE = 'VALUE';
export const RIGHT_CRITERIA_USER_ENTRY = 'USER_ENTRY';

export const ALL_RIGHT_CRITERIAS = [RIGHT_CRITERIA_VALUE, RIGHT_CRITERIA_USER_ENTRY] as const;

export type RightCriteria = (typeof ALL_RIGHT_CRITERIAS)[number];
