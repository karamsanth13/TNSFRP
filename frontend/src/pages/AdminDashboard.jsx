import { useCallback, useEffect, useState } from 'react';
import { ArcElement, BarElement, CategoryScale, Chart as ChartJS, Legend, LinearScale, Tooltip } from 'chart.js';
import { Bar, Doughnut } from 'react-chartjs-2';
import { CheckCircle2, Clock, Download, Package, Timer } from 'lucide-react';
import api, { errMsg } from '../services/api';
import { Alert, Empty, Spinner, StatCard, StatusBadge, Tabs } from '../components/ui';
import { downloadBlob, fmtDate } from '../services/format';

ChartJS.register(ArcElement, BarElement, CategoryScale, LinearScale, Tooltip, Legend);
const COLORS = ['#2f6b4a', '#c0623f', '#a9b8a0', '#d9a441', '#8d948b'];

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [listings, setListings] = useState([]);
  const [tab, setTab] = useState('overview');
  const [err, setErr] = useState('');

  const load = useCallback(async () => {
    try {
      const [s, u, l] = await Promise.all([api.get('/admin/dashboard'), api.get('/admin/users'), api.get('/admin/listings')]);
      setStats(s.data); setUsers(u.data); setListings(l.data); setErr('');
    } catch (e) { setErr(errMsg(e)); }
  }, []);
  useEffect(() => { load(); }, [load]);

  const approve = async (id, approved) => {
    try { await api.patch(`/admin/users/${id}/approval`, { approved }); load(); } catch (e) { setErr(errMsg(e)); }
  };
  const exportCsv = async () => {
    try { const r = await api.get('/admin/reports/export', { responseType: 'blob' }); downloadBlob(r.data, 'donations.csv'); }
    catch (e) { setErr(errMsg(e)); }
  };

  if (!stats) return err ? <div className="container section"><Alert>{err}</Alert></div> : <Spinner />;
  const pending = users.filter((u) => !u.approved);
  const statusKeys = Object.keys(stats.byStatus);
  const districts = Object.entries(stats.byDistrict);

  return (
    <div className="container section">
      <div className="page-head">
        <div><h1>Admin dashboard</h1><p className="muted">{stats.donors} donors · {stats.ngos} NGOs · {stats.pendingApprovals} pending</p></div>
        <button className="btn btn-primary" onClick={exportCsv}><Download size={16} /> Export CSV</button>
      </div>
      <Alert>{err}</Alert>
      <div className="grid-4 stats">
        <StatCard icon={Package} label="Total donations" value={stats.totalDonations} />
        <StatCard icon={CheckCircle2} label="Completed" value={stats.completedDonations} />
        <StatCard icon={Clock} label="Active listings" value={stats.activeListings} tone="terra" />
        <StatCard icon={Timer} label="Expired listings" value={stats.expiredListings} tone="sage" />
      </div>
      <Tabs value={tab} onChange={setTab} tabs={[
        { id: 'overview', label: 'Overview' }, { id: 'users', label: 'Users', count: pending.length ? `${pending.length} pending` : users.length },
        { id: 'donations', label: 'Donations', count: listings.length }]} />

      {tab === 'overview' && (
        <div className="grid-2 charts">
          <div className="card"><h3>Donations by status</h3>
            {stats.totalDonations === 0 ? <Empty title="No data yet" text="Charts appear once donations exist." /> :
              <Doughnut data={{ labels: statusKeys, datasets: [{ data: statusKeys.map((k) => stats.byStatus[k]), backgroundColor: COLORS }] }} />}</div>
          <div className="card"><h3>District-wise donations</h3>
            {districts.length === 0 ? <Empty title="No data yet" text="District totals appear here." /> :
              <Bar options={{ plugins: { legend: { display: false } }, scales: { y: { ticks: { precision: 0 } } } }}
                data={{ labels: districts.map(([d]) => d), datasets: [{ data: districts.map(([, n]) => n), backgroundColor: '#2f6b4a', borderRadius: 6 }] }} />}</div>
        </div>
      )}

      {tab === 'users' && (users.length === 0 ? <Empty title="No registrations yet" text="Donors and NGOs appear here after they sign up." /> :
        <div className="card table-wrap"><table>
          <thead><tr><th>Name</th><th>Role</th><th>Email</th><th>District</th><th>Status</th><th /></tr></thead>
          <tbody>{users.map((u) => (
            <tr key={u.id}><td>{u.fullName}</td><td>{u.role}</td><td>{u.email}</td><td>{u.district}</td>
              <td><span className={u.approved ? 'badge badge-COMPLETED' : 'badge badge-ACCEPTED'}>{u.approved ? 'APPROVED' : 'PENDING'}</span></td>
              <td>{u.approved
                ? <button className="btn btn-ghost btn-sm" onClick={() => approve(u.id, false)}>Revoke</button>
                : <button className="btn btn-primary btn-sm" onClick={() => approve(u.id, true)}>Approve</button>}</td></tr>))}</tbody>
        </table></div>)}

      {tab === 'donations' && (listings.length === 0 ? <Empty title="No donations yet" text="Donation records appear here." /> :
        <div className="card table-wrap"><table>
          <thead><tr><th>Food</th><th>Qty</th><th>Donor</th><th>District</th><th>Status</th><th>Expiry</th></tr></thead>
          <tbody>{listings.map((l) => (
            <tr key={l.id}><td>{l.foodTitle}</td><td>{l.quantity} {l.unit}</td><td>{l.donorName}</td><td>{l.district}</td>
              <td><StatusBadge status={l.status} /></td><td>{fmtDate(l.expiryAt)}</td></tr>))}</tbody>
        </table></div>)}
    </div>
  );
}
