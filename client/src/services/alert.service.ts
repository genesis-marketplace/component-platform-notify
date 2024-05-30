import { Connect, Message, SocketObservable } from '@genesislcap/foundation-comms';
import { DI } from '@genesislcap/web-core';

export interface AlertService {
  getAlerts(): SocketObservable<Message<any>>;
  dismissNotifyAlert(alertId: string);
}

class AlertServiceImpl implements AlertService {
  @Connect private connect: Connect;

  public getAlerts(): SocketObservable<Message<any>> {
    return this.connect.stream(
      'ALL_NOTIFY_ALERT_RECORDS',
      (streamMessage: Message) => {
        () => {};
      },
      (x) => {},
    );
  }

  public async dismissNotifyAlert(alertId: string) {
    return this.connect.commitEvent('EVENT_DISMISS_NOTIFY_ALERT', {
      DETAILS: {
        ALERT_ID: alertId,
      },
    });
  }
}

export const AlertService = DI.createInterface<AlertService>((x) => x.singleton(AlertServiceImpl));
