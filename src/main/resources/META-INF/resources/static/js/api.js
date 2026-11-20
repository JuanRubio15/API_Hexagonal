const portal = {
  logout() {
    sessionStorage.removeItem('portal_token');
    sessionStorage.removeItem('portal_user');
    location.replace('login.html');
  },
  async request(path, method = 'GET', body, accept = 'application/json') {
    const token = sessionStorage.getItem('portal_token');
    if (!token) { this.logout(); throw new Error('Inicia sesión.'); }
    const headers = {Authorization: 'Bearer ' + token, Accept: accept};
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    const response = await fetch(path, {method, headers, body, cache:'no-store', credentials:'omit'});
    if (response.status === 401) { this.logout(); throw new Error('La sesión venció.'); }
    if (response.ok && accept === 'application/pdf') {
      if (!response.headers.get('Content-Type')?.startsWith(accept)) throw new Error('El servidor no devolvió un PDF.');
      return response.blob();
    }
    const data = await response.text();
    let result;
    try { result = data ? JSON.parse(data) : null; }
    catch { throw new Error('Respuesta inesperada del servidor (' + response.status + ').'); }
    if (!response.ok) throw new Error([response.status, result?.error, ...(result?.messages || [])].filter(Boolean).join(' · '));
    return result;
  }
};
if (!sessionStorage.getItem('portal_token')) portal.logout();
window.addEventListener('pageshow', event => {
  if (event.persisted && !sessionStorage.getItem('portal_token')) portal.logout();
});
