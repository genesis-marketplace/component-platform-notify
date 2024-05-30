import { ExecutionContext } from '@genesislcap/web-core';

export function eventDetail<T = any>(ctx: ExecutionContext) {
  return (ctx.event as CustomEvent).detail as T;
}
