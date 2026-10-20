import axios from 'axios';
import { errMsg } from './errorMessage';

const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '/api', withCredentials: true });

export { errMsg };

let districtCache = null;
export const getDistricts = async () => {
  if (!districtCache) districtCache = (await api.get('/districts')).data;
  return districtCache;
};

export default api;
