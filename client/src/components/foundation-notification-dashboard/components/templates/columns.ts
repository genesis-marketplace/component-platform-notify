import { buttonCellRenderer } from '@genesislcap/foundation-entity-management';
import { deleteIcon, editIcon } from '../../../../utils';
import type { TemplateManagement } from './template-management';

export const TEMPLATE_MANAGEMENT_COLUMNS = (context: TemplateManagement) => [
  {
    field: 'NAME',
    headerName: 'Name',
    flex: 1,
  },
  {
    field: 'DESCRIPTION',
    headerName: 'Description',
    flex: 1,
  },
  {
    field: 'USER_NAME',
    headerName: 'Username',
    flex: 1,
  },
  {
    field: 'RULE_TABLE',
    headerName: 'Table',
    flex: 1,
  },
  {
    field: 'RULE_STATUS',
    headerName: 'Status',
    flex: 1,
  },
  {
    field: 'RULE_EXPRESSION',
    headerName: 'Expression',
    flex: 1,
  },
  {
    field: 'RESULT_EXPRESSION',
    headerName: 'Result',
    flex: 1,
  },
  {
    field: 'RULE_EXECUTION_STRATEGY',
    headerName: 'Execution Strategy',
    flex: 1,
  },
  buttonCellRenderer('Edit', (e) => context.editTemplate(e), editIcon),
  buttonCellRenderer('Delete', (e) => context.deleteTemplate(e), deleteIcon),
];
