import { AlertCircle, Inbox, Loader2 } from 'lucide-react';

export const StatusBadge = ({ status }) => (
  <span className={`badge badge-${status}`}>{status.replace('_', ' ')}</span>
);

export const Spinner = ({ label = 'Loading…' }) => (
  <div className="center-box" role="status"><Loader2 className="spin" size={22} /><span>{label}</span></div>
);

export const Empty = ({ title, text, children }) => (
  <div className="empty"><Inbox size={30} /><h3>{title}</h3><p>{text}</p>{children}</div>
);

export const StatCard = ({ icon: Icon, label, value, tone = '' }) => (
  <div className={`stat ${tone}`}>
    <div className="stat-icon"><Icon size={20} /></div>
    <div><div className="stat-value">{value}</div><div className="stat-label">{label}</div></div>
  </div>
);

export const Alert = ({ type = 'error', children }) =>
  children ? <div className={`alert alert-${type}`} role="alert"><AlertCircle size={16} />{children}</div> : null;

export const Tabs = ({ tabs, value, onChange }) => (
  <div className="tabs" role="tablist">
    {tabs.map((t) => (
      <button key={t.id} role="tab" aria-selected={value === t.id} className={value === t.id ? 'tab active' : 'tab'} onClick={() => onChange(t.id)}>
        {t.label}{t.count !== undefined && <span className="tab-count">{t.count}</span>}
      </button>
    ))}
  </div>
);
