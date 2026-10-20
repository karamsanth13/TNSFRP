import { useCallback, useEffect, useState } from 'react';
import { CheckCircle2, MapPin, Navigation, Truck } from 'lucide-react';
import api, { errMsg } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Alert, Empty, Spinner, StatCard, Tabs } from '../components/ui';
import ListingCard from '../components/ListingCard';

export default function NgoDashboard() {
  const { user, ngo } = useAuth();
  const [nearby, setNearby] = useState(null);
  const [history, setHistory] = useState([]);
  const [stats, setStats] = useState(null);
  const [tab, setTab] = useState('nearby');
  const [err, setErr] = useState('');
  const [busyId, setBusyId] = useState('');

  const load = useCallback(async () => {
    try {
      const [n, h, s] = await Promise.all([api.get('/ngo/listings/nearby'), api.get('/ngo/history'), api.get('/ngo/dashboard')]);
      setNearby(n.data); setHistory(h.data); setStats(s.data); setErr('');
    } catch (e) { setErr(errMsg(e)); setNearby([]); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const act = async (id, action) => {
    setBusyId(id); setErr('');
    try { await api.post(`/ngo/listings/${id}/${action}`); await load(); }
    catch (e) { setErr(errMsg(e)); await load(); }
    finally { setBusyId(''); }
  };

  if (!nearby) return <Spinner />;
  const mine = history.filter((l) => ['ACCEPTED', 'PICKED_UP'].includes(l.status));
  const done = history.filter((l) => l.status === 'COMPLETED');

  return (
    <div className="container section">
      <div className="page-head">
        <div><h1>{ngo?.ngoName || 'NGO'} dashboard</h1>
          <p className="muted"><MapPin size={14} /> Service radius {ngo?.serviceRadiusKm} km · {user.district}</p></div>
      </div>
      {!user.approved && <Alert type="warn">Your NGO is awaiting admin verification. You can browse donations but cannot accept them yet.</Alert>}
      <Alert>{err}</Alert>
      <div className="grid-4 stats">
        <StatCard icon={Navigation} label="Available nearby" value={stats?.nearbyAvailable ?? 0} tone="terra" />
        <StatCard icon={Truck} label="To collect" value={(stats?.accepted ?? 0) + (stats?.pickedUp ?? 0)} tone="sage" />
        <StatCard icon={CheckCircle2} label="Completed" value={stats?.completed ?? 0} />
      </div>
      <Tabs value={tab} onChange={setTab} tabs={[
        { id: 'nearby', label: 'Nearby', count: nearby.length }, { id: 'mine', label: 'My pickups', count: mine.length }, { id: 'done', label: 'Completed', count: done.length }]} />

      {tab === 'nearby' && (nearby.length === 0
        ? <Empty title="Nothing nearby right now" text="New donations within your service radius will show up here." />
        : <div className="grid-3">{nearby.map(({ listing, distanceKm }) => (
            <ListingCard key={listing.id} listing={listing} distanceKm={distanceKm}>
              <button className="btn btn-primary btn-sm" disabled={!user.approved || busyId === listing.id} onClick={() => act(listing.id, 'accept')}>
                <CheckCircle2 size={15} /> {busyId === listing.id ? 'Accepting…' : 'Accept donation'}</button>
            </ListingCard>))}</div>)}

      {tab === 'mine' && (mine.length === 0
        ? <Empty title="No pickups in progress" text="Accept a donation to see it here." />
        : <div className="grid-3">{mine.map((l) => (
            <ListingCard key={l.id} listing={l}>
              {l.status === 'ACCEPTED'
                ? <button className="btn btn-accent btn-sm" disabled={busyId === l.id} onClick={() => act(l.id, 'pickup')}><Truck size={15} /> Confirm pickup</button>
                : <small className="muted">Waiting for the donor to confirm handover.</small>}
            </ListingCard>))}</div>)}

      {tab === 'done' && (done.length === 0
        ? <Empty title="No completed donations yet" text="Finished pickups are listed here." />
        : <div className="grid-3">{done.map((l) => <ListingCard key={l.id} listing={l} />)}</div>)}
    </div>
  );
}
