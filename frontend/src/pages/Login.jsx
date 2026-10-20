import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { LogIn } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { errMsg } from '../services/api';
import { dashPath } from '../services/format';
import { Alert } from '../components/ui';

export default function Login() {
  const { login } = useAuth();
  const nav = useNavigate();
  const { state } = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [err, setErr] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setErr(''); setBusy(true);
    try { const u = await login(email, password); nav(dashPath(u.role)); }
    catch (e2) { setErr(errMsg(e2)); }
    finally { setBusy(false); }
  };

  return (
    <section className="auth-wrap">
      <form className="card auth-card" onSubmit={submit}>
        <h1>Welcome back</h1>
        <p className="muted">Log in to your TNSFRP account.</p>
        {state?.registered && <Alert type="success">Registered! You can log in now. An admin must approve your account before you can donate or accept food.</Alert>}
        <Alert>{err}</Alert>
        <div className="field"><label htmlFor="email">Email</label><input id="email" type="email" required autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} /></div>
        <div className="field"><label htmlFor="pw">Password</label><input id="pw" type="password" required autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} /></div>
        <button className="btn btn-primary btn-block" disabled={busy}><LogIn size={16} /> {busy ? 'Logging in…' : 'Log in'}</button>
        <p className="muted center">New here? <Link to="/register">Create an account</Link></p>
      </form>
    </section>
  );
}
