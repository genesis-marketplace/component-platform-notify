import { buttonCellRenderer } from '@genesislcap/foundation-entity-management';
import { deleteIcon, editIcon } from '../../../../utils';
import type { RuleManagement } from './rule-management';

export const RULE_MANAGEMENT_COLUMNS = (context: RuleManagement) => [
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
  buttonCellRenderer('Edit', (e) => context.editRule(e), editIcon),
  buttonCellRenderer('Delete', (e) => context.deleteRule(e), deleteIcon),
];
