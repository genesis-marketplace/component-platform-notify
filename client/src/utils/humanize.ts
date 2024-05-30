export const humanize = (s: string) => {
  if (!s) {
    return '';
  }

  let i;
  const frags = s.split('_');

  for (i = 0; i < frags.length; i += 1) {
    frags[i] = frags[i].charAt(0).toUpperCase() + frags[i].slice(1).toLowerCase();
  }

  return frags.join(' ');
};
