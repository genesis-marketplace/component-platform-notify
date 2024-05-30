import { Connect } from '@genesislcap/foundation-comms';
import { DI } from '@genesislcap/web-core';

export interface NotifyService {
  getNotifyRouteTopics();
}

class NotifyServiceImpl implements NotifyService {
  @Connect private connect: Connect;

  public async getNotifyRouteTopics() {
    const data = await this.connect.request('REQ_NOTIFY_ROUTE_TOPICS', {
      REQUEST: { NOTIFY_ROUTE_ID: '' },
    });
    return data.REPLY ? data.REPLY[0].TOPICS : [];
  }
}

export const NotifyService = DI.createInterface<NotifyService>((x) =>
  x.singleton(NotifyServiceImpl),
);
