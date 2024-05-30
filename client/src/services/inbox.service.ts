import { Auth, Connect, SocketObservable } from '@genesislcap/foundation-comms';
import { DI } from '@genesislcap/web-core';
import { logger } from '../utils';

export interface FoundationInboxServiceStore {
  items: Array<any>;
}

export type SubscribeCallback = ({ store }: { store: FoundationInboxServiceStore }) => any;

export interface FoundationInboxService {
  subscribe(callback: SubscribeCallback): void;
  unsubscribe(callback: SubscribeCallback): void;
}

class FoundationInboxServiceImpl implements FoundationInboxService {
  @Connect private connect: Connect;
  @Auth private auth: Auth;

  private stream: SocketObservable<any> | null;
  private sourceRef: string;
  private subscriptions: SubscribeCallback[] = [];
  public store: FoundationInboxServiceStore = {
    items: [],
  };

  private getStream(): SocketObservable<any> {
    return this.connect.stream(
      'ALL_NOTIFY_ALERT_RECORDS',
      (result) => {
        this.sourceRef = result.SOURCE_REF;

        if (!result?.ROW) {
          this.store.items = [];
          return;
        }

        if (result.SEQUENCE_ID === 1) {
          this.store.items = result.ROW;
        } else {
          const details: any = result?.ROW[0];

          switch (details?.DETAILS.OPERATION) {
            case 'INSERT':
              this.store.items.push(details);
              break;
            case 'MODIFY':
              this.store.items = this.store.items.map((alert) => this.modifyAlert(alert, details));
              break;
            case 'DELETE':
              // Implement if there's a case that a NOTIFY_ALERT record is deleted
              break;
            default:
              logger.error('Unexpected operation');
          }
        }

        this.subscriptions.forEach((callback) => callback({ store: this.store }));
      },
      (response) => logger.error(response),
    ) as SocketObservable<any>;
  }

  private modifyAlert = (alert, details) =>
    alert.DETAILS?.ROW_REF === details?.DETAILS.ROW_REF ? { ...alert, ...details } : alert;

  public subscribe(callback: SubscribeCallback): void {
    this.subscriptions.push(callback);

    if (this.stream) {
      callback({ store: this.store });
      return;
    }

    this.stream = this.getStream();
    this.stream.subscribe();
  }

  public unsubscribe(callback: SubscribeCallback): void {
    const index = this.subscriptions.indexOf(callback);

    if (index >= 0) {
      this.subscriptions.splice(index, 1);
    }

    this.dataLogoff();
  }

  private dataLogoff() {
    if (this.sourceRef && this.auth.isLoggedIn) {
      this.connect.dataLogoff(this.sourceRef);
    }

    this.stream = undefined;
    this.store = { items: [] };
    this.sourceRef = undefined;
  }
}

export const FoundationInboxService = DI.createInterface<FoundationInboxService>((x) =>
  x.singleton(FoundationInboxServiceImpl),
);
