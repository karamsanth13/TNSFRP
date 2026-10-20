export const dashPath = (role) => (role === 'ADMIN' ? '/admin' : role === 'NGO' ? '/ngo' : '/donor');

export const fmtDate = (iso) =>
  iso ? new Date(iso).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }) : '—';

export const timeLeft = (iso) => {
  const ms = new Date(iso) - new Date();
  if (ms <= 0) return 'Expired';
  const m = Math.floor(ms / 60000);
  if (m < 60) return `${m} min left`;
  const h = Math.floor(m / 60);
  return h < 24 ? `${h}h ${m % 60}m left` : `${Math.floor(h / 24)}d left`;
};

export const toLocalInput = (d) => {
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
};

export const downloadBlob = (blob, name) => {
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = name; a.click();
  URL.revokeObjectURL(url);
};
