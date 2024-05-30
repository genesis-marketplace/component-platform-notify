export const updateArray = (newData: Array<any>, oldData: Array<any>): Array<any> => {
  if (newData && oldData && oldData.length > 0) {
    newData.forEach((row) => {
      const eventData = row;

      if (eventData && eventData.DETAILS) {
        const ref = eventData.DETAILS.ROW_REF;
        const type = eventData.DETAILS.OPERATION;

        if (type === 'INSERT') {
          if (!oldData.find((x) => x.DETAILS?.ROW_REF === ref)) {
            oldData.unshift(eventData);
          }
        }

        if (type === 'DELETE') {
          oldData = oldData.filter((x) => row.DETAILS?.ROW_REF !== x.DETAILS?.ROW_REF);
        }

        if (type === 'MODIFY') {
          oldData = oldData.map((item: any) =>
            item.DETAILS?.ROW_REF === ref ? { ...item, ...eventData } : item,
          );
        }
      }
    });
  } else if (newData && (!oldData || oldData.length === 0)) {
    oldData = [...new Set(newData)] as any[];
  }

  return oldData;
};
