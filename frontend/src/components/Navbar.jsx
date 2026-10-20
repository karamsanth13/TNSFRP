import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { LogOut, Menu, Sprout, X } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { dashPath } from '../services/format';

export default function Navbar() {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const nav = useNavigate();
  const close = () => setOpen(false);
  const out = async () => { await logout(); close(); nav('/'); };

  return (
    <header className="nav">
      <div className="container nav-inner">
        <Link to="/" className="brand" onClick={close}><Sprout size={22} /> TNSFRP</Link>
        <button className="icon-btn nav-toggle" aria-label="Toggle menu" aria-expanded={open} onClick={() => setOpen(!open)}>
          {open ? <X size={22} /> : <Menu size={22} />}
        </button>
        <nav className={open ? 'nav-links open' : 'nav-links'}>
          <NavLink to="/" end onClick={close}>Home</NavLink>
          {user ? (
            <>
              <NavLink to={dashPath(user.role)} onClick={close}>Dashboard</NavLink>
              <span className="nav-user">{user.fullName}</span>
              <button className="btn btn-ghost btn-sm" onClick={out}><LogOut size={15} /> Logout</button>
            </>
          ) : (
            <>
              <NavLink to="/login" onClick={close}>Log in</NavLink>
              <Link to="/register" className="btn btn-primary btn-sm" onClick={close}>Get started</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
