export const PARAM_TYPE_FIELD = 'FIELD';
export const PARAM_TYPE_STRING = 'STRING';
export const PARAM_TYPE_BOOLEAN = 'BOOLEAN';
export const PARAM_TYPE_NUMBER = 'NUMBER';

export const ALL_PARAM_TYPES = [
  // PARAM_TYPE_FIELD,
  PARAM_TYPE_STRING,
  // PARAM_TYPE_BOOLEAN,
  PARAM_TYPE_NUMBER,
] as const;

export type ParamType = (typeof ALL_PARAM_TYPES)[number];
