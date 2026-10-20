const fallbackByStatus = (status) => {
  if (status === 401) return 'Your email or password is incorrect, or your session has expired.';
  if (status === 403) return 'You do not have permission to perform this action.';
  if (status === 404) return 'The requested item could not be found.';
  if (status === 409) return 'This change conflicts with the current data. Refresh and try again.';
  if (status >= 500) return 'The server could not complete the request. Please try again shortly.';
  return status ? `The request failed (HTTP ${status}). Please check your information and try again.` : '';
};

const responseMessage = (data) => {
  if (data && typeof data === 'object') {
    const message = data.message ?? data.detail ?? data.error_description ?? data.error ?? data.title;
    if (typeof message === 'string' && message.trim()) return message.trim();
    if (Array.isArray(data.errors)) {
      const messages = data.errors
        .map((item) => (typeof item === 'string' ? item : item?.message))
        .filter((item) => typeof item === 'string' && item.trim());
      if (messages.length) return [...new Set(messages)].join('; ');
    }
  }

  if (typeof data === 'string') {
    const body = data.trim();
    if (!body) return '';
    try {
      const parsed = JSON.parse(body);
      if (parsed !== data) return responseMessage(parsed);
    } catch {
      // Non-JSON response bodies (for example an HTML proxy error) are handled
      // by the HTTP status fallback below instead of being shown to users.
    }
  }
  return '';
};

export const errMsg = (error) => {
  const response = error?.response;
  if (response) {
    return responseMessage(response.data)
      || fallbackByStatus(response.status)
      || 'Something went wrong. Please try again.';
  }
  if (error?.code === 'ECONNABORTED') return 'The request timed out. Please try again.';
  return 'Cannot reach the server. Is the backend running?';
};
