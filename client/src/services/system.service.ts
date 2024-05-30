import { Connect } from '@genesislcap/foundation-comms';
import { DI } from '@genesislcap/web-core';

export type Field = {
  FIELD_NAME: string;
  FIELD_TYPE: string;
};

export interface SystemService {
  getResources(): Promise<string[]>;
  getFields(resource: string): Promise<Field[]>;
}

class SystemServiceImpl implements SystemService {
  @Connect private connect: Connect;

  public async getResources(): Promise<string[]> {
    const response = await this.connect.request('NOTIFY_SYSTEM_ENTITY', {
      REQUEST: { ENTITY_NAME: '*' },
    });
    return response.REPLY.map((table) => table.ENTITY_NAME).sort();
  }

  async getFields(resource: string): Promise<Field[]> {
    if (!resource) {
      return [];
    }

    const response = await this.connect.request('SYSTEM_ENTITY_FIELDS', {
      REQUEST: {
        ENTITY_NAME: resource,
      },
    });
    return response.REPLY.sort((a, b) => a.NAME.localeCompare(b.NAME)).map((entity) => ({
      FIELD_NAME: entity.NAME,
      FIELD_TYPE: entity.TYPE,
    }));
  }
}

export const SystemService = DI.createInterface<SystemService>((x) =>
  x.singleton(SystemServiceImpl),
);
