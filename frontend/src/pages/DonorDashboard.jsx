import { useCallback, useEffect, useState } from 'react';
import { CheckCircle2, Clock, Package, Plus, Truck } from 'lucide-react';
import api, { errMsg } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Alert, Empty, Spinner, StatCard, Tabs } from '../components/ui';
import ListingCard from '../components/ListingCard';
import DonationForm from '../components/DonationForm';

export default function DonorDashboard() {
  const { user } = useAuth();
  const [listings, setListings] = useState(null);
  const [stats, setStats] = useState(null);
  const [tab, setTab] = useState('active');
  const [showForm, setShowForm] = useState(false);
  const [err, setErr] = useState('');

  const load = useCallback(async () => {
    try {
      const [l, s] = await Promise.all([api.get('/donor/listings'), api.get('/donor/dashboard')]);
      setListings(l.data); setStats(s.data); setErr('');
    } catch (e) { setErr(errMsg(e)); setListings([]); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const complete = async (id) => {
    try { await api.post(`/donor/listings/${id}/complete`); load(); } catch (e) { setErr(errMsg(e)); }
  };

  if (!listings) return <Spinner />;
  const active = listings.filter((l) => ['POSTED', 'ACCEPTED', 'PICKED_UP'].includes(l.status));
  const history = listings.filter((l) => ['COMPLETED', 'EXPIRED'].includes(l.status));
  const shown = tab === 'active' ? active : history;
  const s = stats?.byStatus || {};

  return (
    <div className="container section">
      <div className="page-head">
        <div><h1>Donor dashboard</h1><p className="muted">Hello, {user.fullName}</p></div>
        <button className="btn btn-primary" disabled={!user.approved} onClick={() => setShowForm(true)}><Plus size={16} /> Create donation</button>
      </div>
      {!user.approved && <Alert type="warn">Your account is awaiting admin approval. You can post donations once it is approved.</Alert>}
      <Alert>{err}</Alert>
      <div className="grid-4 stats">
        <StatCard icon={Package} label="Total donations" value={stats?.total ?? 0} />
        <StatCard icon={Clock} label="Waiting for NGO" value={s.POSTED ?? 0} tone="terra" />
        <StatCard icon={Truck} label="In progress" value={(s.ACCEPTED ?? 0) + (s.PICKED_UP ?? 0)} tone="sage" />
        <StatCard icon={CheckCircle2} label="Completed" value={s.COMPLETED ?? 0} />
      </div>
      {showForm && <DonationForm onCancel={() => setShowForm(false)} onCreated={() => { setShowForm(false); load(); }} />}
      <Tabs value={tab} onChange={setTab} tabs={[{ id: 'active', label: 'Active', count: active.length }, { id: 'history', label: 'History', count: history.length }]} />
      {shown.length === 0 ? (
        <Empty title={tab === 'active' ? 'No active donations' : 'No history yet'} text={tab === 'active' ? 'Post surplus food to get started.' : 'Completed and expired donations appear here.'} />
      ) : (
        <div className="grid-3">
          {shown.map((l) => (
            <ListingCard key={l.id} listing={l}>
              {l.status === 'PICKED_UP' && <button className="btn btn-primary btn-sm" onClick={() => complete(l.id)}><CheckCircle2 size={15} /> Confirm handover</button>}
            </ListingCard>
          ))}
        </div>
      )}
    </div>
  );
}
