/* Login y registro.
   - Login: usa el endpoint EXISTENTE POST /login (sin cambios en el backend). Guarda el token en
     sessionStorage con la misma clave que panel.html ('portal_token').
   - Registro: solo visual en esta etapa (el backend aún no tiene registro). */
(function(){
  const $ = (s,r=document)=>r.querySelector(s);
  const toast = (m,t='') => { const e=document.createElement('div'); e.className='toast '+t; e.textContent=m; $('#toasts').appendChild(e); setTimeout(()=>e.remove(),3800); };

  // Iconos
  document.querySelectorAll('[data-brand]').forEach(el => el.outerHTML = BRAND[el.dataset.brand]);
  const soc = $('#socials');
  if(soc) soc.innerHTML = ['facebook','twitter','instagram','linkedin'].map(n=>`<a href="#" data-deco aria-label="${n}">${BRAND[n]}</a>`).join('');
  document.querySelectorAll('[data-eye]').forEach(b => {
    b.innerHTML = ICON('eyeoff');
    b.addEventListener('click', () => {
      const inp = b.parentElement.querySelector('input'), ver = inp.type === 'password';
      inp.type = ver ? 'text' : 'password'; b.innerHTML = ICON(ver ? 'eye' : 'eyeoff');
      b.setAttribute('aria-label', ver ? 'Ocultar contraseña' : 'Mostrar contraseña');
    });
  });
  // Elementos decorativos (Google y redes): sin autenticación externa todavía
  document.addEventListener('click', e => { if(e.target.closest('[data-deco]')){ e.preventDefault(); toast('Disponible en una siguiente etapa.'); } });

  // Registro (visual)
  const reg = $('#registro-form');
  if(reg) reg.addEventListener('submit', e => { e.preventDefault(); toast('El registro de usuarios se activará en la siguiente etapa.'); });

  // Login real contra /login
  const form = $('#login-form');
  if(!form) return;
  const errores = $('#login-errores');
  const mostrar = l => { errores.replaceChildren(...l.map(message => { const li=document.createElement('li'); li.textContent=message; return li; })); errores.hidden = !(l&&l.length); };
  form.addEventListener('submit', async e => {
    e.preventDefault(); mostrar([]);
    const usuario = form.usuario.value.trim(), password = form.password.value;
    if(!usuario || !password){ mostrar(['Escribe tu usuario y tu contraseña.']); return; }
    const btn = $('#login-btn'); btn.disabled = true; btn.textContent = 'Entrando…';
    try{
      const res = await fetch('/login', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({usuario, password})});
      const data = await res.json().catch(()=>({}));
      if(!res.ok){ mostrar([data.error || 'No se pudo iniciar sesión.']); return; }
      if (!data.token) throw new Error('Falta el token');
      sessionStorage.setItem('portal_token', data.token);
      sessionStorage.setItem('portal_user', usuario);
      sessionStorage.setItem('portal_role', data.rol || '');
      location.href = 'dashboard.html';
    }catch(_){
      mostrar(['No se pudo conectar con el servidor. Abre esta página desde Quarkus.']);
    }finally{ btn.disabled = false; btn.textContent = 'Iniciar sesión'; }
  });
})();
