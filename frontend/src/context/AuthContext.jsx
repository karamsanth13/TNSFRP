import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import api from '../services/api';

const Ctx = createContext(null);
export const useAuth = () => useContext(Ctx);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [ngo, setNgo] = useState(null);
  const [loading, setLoading] = useState(true);

  const apply = (d) => { setUser(d?.user || null); setNgo(d?.ngoProfile || null); };

  const refresh = useCallback(async () => {
    try { apply((await api.get('/auth/me')).data); }
    catch { apply(null); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { refresh(); }, [refresh]);

  const login = async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password });
    apply(data);
    return data.user;
  };
  const logout = async () => { try { await api.post('/auth/logout'); } catch { /* ignore */ } apply(null); };

  return <Ctx.Provider value={{ user, ngo, loading, login, logout, refresh }}>{children}</Ctx.Provider>;
}
