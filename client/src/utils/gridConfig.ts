export const hideColumn = {
  hide: true,
};

export const suppressColumn = {
  ...hideColumn,
  suppressColumnsToolPanel: true,
};

export const hideColumns = (fieldsToSuppress: any) =>
  fieldsToSuppress.map((columnName) => ({
    field: columnName,
    ...suppressColumn,
  }));
